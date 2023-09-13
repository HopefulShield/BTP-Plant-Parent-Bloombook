package com.bloombook.backend

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.bloombook.models.Journals
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.firestore.FieldValue

const val JOURNALS_COLLECTION_REF = "journals"

class JournalRepo {

    private val db = FirebaseFirestore.getInstance()
    var snapshotStateListener: ListenerRegistration? = null

    // Reference to cloud storage for uploading and downloading large image files
    private val storage = FirebaseStorage.getInstance().reference
    private val imagesRef = storage.child("user_photos")

    // Reference to the user's journals collection
    private val journalsRef: CollectionReference = db
        .collection("users/${getUserId()}/${JOURNALS_COLLECTION_REF}")


    // Getter methods to return information about the user and other references
    fun user() = Firebase.auth.currentUser
    fun hasUser(): Boolean = Firebase.auth.currentUser != null

    fun getUserId(): String = Firebase.auth.currentUser?.uid.orEmpty()

    fun storageRef() = storage


    fun getJournal(
        journalId: String,
        onError:(Throwable?) -> Unit,
        onSuccess:(Journals?) -> Unit
    ) {
        journalsRef
            .document(journalId)
            .get()
            .addOnSuccessListener {document ->
                val journal = document?.toObject(Journals::class.java)

                // Get the downloadable URL for display image from the images ref in the cloud storage
                storage.child("user_photos/${journal?.displayImageUri}").downloadUrl.addOnSuccessListener { downloadUrL ->

                    val updatedJournal = journal?.copy(displayImageUri = downloadUrL.toString())
                    onSuccess.invoke(updatedJournal)

                }.addOnFailureListener { storageError ->
                    Log.d("storage error", storageError.toString() )
                    onSuccess.invoke(document?.toObject(Journals::class.java))
                }
            }
            .addOnFailureListener {result ->
                onError.invoke(result.cause)
            }
    }

    fun addJournal(
        commonName: String,
        nickName: String,
        displayImageUri: String,
        timestamp: Timestamp,
        onComplete: (Boolean) -> Unit
    ) {

        val documentId = journalsRef.document().id

        if (displayImageUri != null) {

            val imagesRef = storage.child("user_photos/${displayImageUri}")
            imagesRef.putFile(displayImageUri.toUri())

            saveImageForUser(displayImageUri) {
                Log.d("saving user's image", "Success")
            }
        }

        val journal = Journals(
            commonName,
            nickName,
            displayImageUri,
            documentId,
            timestamp
        )
        journalsRef
            .document(documentId)
            .set(journal)
            .addOnCompleteListener {result ->
                onComplete.invoke(result.isSuccessful)
            }

    }

    fun updateJournal(
        journalId: String,
        commonName: String,
        nickName: String,
        displayImageUri: String,
        onResult: (Boolean) -> Unit
    ) {
        val updateData = hashMapOf<String,Any> (
            "commonName" to commonName,
            "nickName" to nickName,
            "displayImageUri" to displayImageUri
        )

        if (displayImageUri != null) {

            val imagesRef = storage.child("user_photos/${displayImageUri}")
            imagesRef.putFile(displayImageUri.toUri())

            saveImageForUser(displayImageUri) {
                Log.d("saving user's image", "Success")
            }
        }

        // update the journal document
        journalsRef
            .document(journalId)
            .update(updateData)
            .addOnCompleteListener { result ->
                // update any reminders that use this journal as a tag
                var data = hashMapOf<String, Any>()
                data["journalId"] = journalId
                data["commonName"] = commonName
                data["nickName"] = nickName
                data["imageUri"] = displayImageUri

                FirebaseFunctions.getInstance().getHttpsCallable("updateReminderTagList").call(data)
                onResult.invoke(result.isSuccessful)
            }
    }


    private fun saveImageForUser(uri: String, onComplete: (Boolean) -> Unit) {
        val userRef = db.document("users/${getUserId()}")
        userRef.update("imageList", FieldValue.arrayUnion(uri))
            .addOnCompleteListener {result ->
                onComplete.invoke(result.isSuccessful)
            }
    }

    fun getUserJournals(): Flow<Resources<List<Journals>>> = callbackFlow {

        try {
            snapshotStateListener = journalsRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener {snapshot, e ->

                val response = if (snapshot != null) {
                    val journals = snapshot.toObjects(Journals::class.java)
                    Resources.Success(data = journals)
                } else {
                    Resources.Error(throwable = e?.cause)
                }
                trySend(response)
            }
        } catch (e: Exception) {
            trySend(Resources.Error(e.cause))
            e.printStackTrace()
        }
        awaitClose {
            snapshotStateListener?.remove()
        }
    }

    fun deleteJournal(plantId: String): Task<HttpsCallableResult> {
        var data = hashMapOf<String, Any>()
        data["plantId"] = plantId


        if (hasUser()) {
            Log.d(" user id: ", user()!!.uid)
        }
        else {
            Log.d("Journal deletion", "Journal deletion cannot be invoked since user is not signed in")
        }
        return FirebaseFunctions.getInstance().getHttpsCallable("deletePlant").call(data)
    }

    /*
    suspend fun deleteJournal(journalId: String)  {

        val journalDoc = journalsRef.document(journalId)
        val entriesRef = journalDoc.collection("entries")

        try {
            val snapshot = entriesRef.get().await()
            val batch = db.batch()
            snapshot.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            journalDoc.delete()
        }
        catch(error: Error) {
            Log.d("Delete Journal", error.message.toString())
        }

    }

     */
}










