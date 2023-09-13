package com.bloombook.backend

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.bloombook.models.Entries
import com.bloombook.models.Journals
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

const val USERS_COLLECTION_REF = "users"
const val ENTRIES_COLLECTION_REF = "entries"

class EntriesRepo {

    val db = FirebaseFirestore.getInstance()
    var snapshotStateListener: ListenerRegistration? = null

    fun user() = Firebase.auth.currentUser
    fun getUserId(): String = Firebase.auth.currentUser?.uid.orEmpty()


    // Reference to cloud storage for uploading and downloading large image files
    private val storage = FirebaseStorage.getInstance().reference
    private val imagesRef = storage.child("user_photos")
    fun storageRef() = storage

    private fun getJournalRef() =
        db.collection("$USERS_COLLECTION_REF/${getUserId()}/$JOURNALS_COLLECTION_REF")
    private fun getEntryRef(journalId: String) =
        getJournalRef().document(journalId).collection(ENTRIES_COLLECTION_REF)


    fun getJournalEntries(
        journalId: String
    ): Flow<Resources<List<Entries>>> = callbackFlow {

        try {
            snapshotStateListener = getEntryRef(journalId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    val response = if (snapshot != null) {
                        val entries = snapshot.toObjects(Entries::class.java)
                        Resources.Success(data = entries)
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
    fun getEntry(
        journalId: String,
        entryId: String,
        onError:(Throwable?) -> Unit,
        onSuccess:(Entries?) -> Unit
    ) {
        getEntryRef(journalId)
            .document(entryId)
            .get()
            .addOnSuccessListener {
                onSuccess.invoke(it?.toObject(Entries::class.java))
            }
            .addOnFailureListener{result ->
                onError.invoke(result.cause)
            }
    }

    fun addEntry(
        journalId: String,
        temperature: String,
        selectedOption: String,
        location: String,
        customLocation: String,
        observations: String,
        imageList: List<Map<String, String>>,
        timestamp: Timestamp,
        onComplete: (Boolean) -> Unit
    ) {
        val documentId = getEntryRef(journalId).document().id

        imageList.forEach { pair ->
            val uri = pair["value"]

            if (uri != null) {

                val imagesRef = storage.child("user_photos/${uri}")
                imagesRef.putFile(uri.toUri())

                saveImageForUser(uri) {
                    Log.d("saving user's image", "Success")
                }
            }
        }

        val entry = Entries(
            journalId,
            temperature,
            selectedOption,
            location,
            customLocation,
            observations,
            documentId,
            imageList,
            timestamp
        )
        getEntryRef(journalId)
            .document(documentId)
            .set(entry)
            .addOnCompleteListener {result ->
                onComplete.invoke(result.isSuccessful)
            }


    }

    fun deleteEntry(
        journalId: String,
        entryId: String,
        onComplete: (Boolean) -> Unit) {
        getEntryRef(journalId)
            .document(entryId)
            .delete()
            .addOnCompleteListener{
                onComplete.invoke(it.isSuccessful)
            }
    }

    fun updateEntry(
        journalId: String,
        temperature: String,
        selectedOption: String,
        location: String,
        customLocation: String,
        observations: String,
        imageList: List<Map<String, String>>,
        entryId: String,
        onResult: (Boolean) -> Unit
    ) {

        imageList.forEach { pair ->
            val uri = pair["value"]

            if (uri != null) {

                val imagesRef = storage.child("user_photos/${uri}")
                imagesRef.putFile(uri.toUri())

                saveImageForUser(uri) {
                    Log.d("saving user's image", "Success")
                }
            }
        }

        val updateData = hashMapOf<String,Any>(
            "temperature" to temperature,
            "selectedOption" to selectedOption,
            "location" to location,
            "customLocation" to customLocation,
            "observations" to observations,
            "imageList" to imageList,

            // We can eventually put an edited time stamp if necessary
            //"timestamp" to Timestamp.now(),
        )

        getEntryRef(journalId)
            .document(entryId)
            .update(updateData)
            .addOnCompleteListener {
                onResult(it.isSuccessful)
            }
    }

    private fun saveImageForUser(uri: String, onComplete: (Boolean) -> Unit) {
        val userRef = db.document("users/${getUserId()}")
        userRef.update("imageList", FieldValue.arrayUnion(uri))
            .addOnCompleteListener {result ->
                onComplete.invoke(result.isSuccessful)
            }
    }


}

