package com.bloombook.screens.journal.editEntry

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bloombook.backend.EntriesRepo
import com.bloombook.models.Entries
import com.bloombook.models.Journals
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

/**
 * This class houses the methods to update the UI and make calls to the firestore
 */
class EditEntryViewModel (
    private val repository: EntriesRepo = EntriesRepo(),
): ViewModel() {

    var editEntryUiState by mutableStateOf(EditEntryUiState())
        private set

    fun storage() = repository.storageRef()

    fun onTempChange(temperature: String) {
        editEntryUiState = editEntryUiState.copy(temperature = temperature)
    }

    fun onSelectedOptionChange(option: String) {
        editEntryUiState = editEntryUiState.copy(selectedOption = option)

    }
    fun onLocationChange(location: String) {
        editEntryUiState = editEntryUiState.copy(location = location)
    }
    fun onCustomLocationChange(custom: String) {
        editEntryUiState = editEntryUiState.copy(customLocation = custom)
    }

    fun onObservationChange(observations: String) {
        editEntryUiState = editEntryUiState.copy(observations = observations)
    }

    fun addImage(imageUri: String) {
        val uniqueKey = UUID.randomUUID().toString()

        val newImagePair = mapOf("key" to uniqueKey, "value" to imageUri)
        val newImageList = editEntryUiState.imageList.toMutableList()

        newImageList.add(newImagePair)

        editEntryUiState = editEntryUiState.copy(imageList = newImageList)

        Log.d("edit screen adding image with key: ", uniqueKey)
        Log.d("edit screen image list: ", editEntryUiState.imageList.toString() )


    }
    fun deleteImage(key: String) {

        val newImageList = editEntryUiState.imageList.filterNot { it["key"] == key }

        // Update the UI state with the new image list
        editEntryUiState = editEntryUiState.copy(imageList = newImageList)

        Log.d("edit screen deleting image with key: ", key)
        Log.d("edit screen image list: ", editEntryUiState.imageList.toString() )
    }

    fun addEntry(journalId: String) {

        repository.addEntry(
            journalId= journalId,
            temperature = editEntryUiState.temperature,
            selectedOption = editEntryUiState.selectedOption,
            location = editEntryUiState.location,
            customLocation = editEntryUiState.customLocation,
            observations = editEntryUiState.observations,
            imageList = editEntryUiState.imageList,
            timestamp = Timestamp.now()
        ) {
            editEntryUiState = editEntryUiState.copy(entryAddedStatus = it)
        }
    }



    fun setEditFields(entry: Entries) {


        editEntryUiState = editEntryUiState.copy(
            imageList = entry.imageList,
            temperature = entry.temperature,
            selectedOption = entry.selectedOption,
            customLocation = entry.customLocation,
            location = entry.location,
            observations = entry.observations
        )

        Log.d("setEditFields new Ui state", editEntryUiState.toString() )
    }

    val storage = repository.storageRef()



    /*  dummy code for updating image URIs but this is scuffed
    //val newList = mutableListOf<Map<String, String>>()

    val newList = entry.imageList.map { map ->
        map.toMutableMap().apply {
            storage.child("user_photos/${this["value"]}").downloadUrl.addOnSuccessListener { downloadURL ->
                this["value"] = downloadURL.toString()
                Log.d("cloud storage", "Retrdownload URL for entry image list: ${downloadURL.toString()}")

            }.addOnFailureListener { error ->
                Log.d("cloud storage", "could not get download URL for entry image list: ${error.message}")

            }
        }

    }
    */

    /*
    entry.imageList.forEach { map ->


        storage.child("images/${map["value"]}").downloadUrl.addOnSuccessListener { downloadURL ->

            // If possible, replace the uri with the downloadable url on the cloud
            val pair = mapOf("key" to map["key"], "value" to downloadURL.toString())
            newList.add(pair as Map<String, String>)

            Log.d("cloud storage", "Retreived download URL for entry image list: https://firebasestorage.googleapis.com/v0/b/bloom-book-8b2fe.appspot.com/o/images%2Fcontent%3A%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F1012?alt=media&token=d711a760-454a-477b-9e2d-c82e5b4fd07c")


        }.addOnFailureListener { error ->
            // Otherwise, we just use the journal's default uri which may or not point to
            // the image resource on their device
            newList.add(map)
            Log.d("cloud storage", "could not get download URL for entry image list: ${error.message}")
        }

     */

    fun getEntry(
        journalId: String,
        entryId: String
    ) {
        repository.getEntry(
            journalId = journalId,
            entryId = entryId,
            onError = {}
        ) {entry ->
            //editEntryUiState = editEntryUiState.copy(selectedEntry = entry)
            //editEntryUiState.selectedEntry?.let { setEditFields(it)}
            entry?.let { setEditFields(it) }
        }
    }

    fun updateEntry(
        journalId: String,
        entryId: String
    ) {
        repository.updateEntry(
            journalId = journalId,
            temperature = editEntryUiState.temperature,
            selectedOption = editEntryUiState.selectedOption,
            location = editEntryUiState.location,
            customLocation = editEntryUiState.customLocation,
            observations = editEntryUiState.observations,
            imageList = editEntryUiState.imageList,
            entryId = entryId
        ) {
            editEntryUiState = editEntryUiState.copy(updateEntryStatus = it)
        }
    }

    fun resetEntryAddedStatus() {
        editEntryUiState = editEntryUiState.copy(
            entryAddedStatus = false,
            updateEntryStatus = false
        )
    }

    fun resetState() {
        editEntryUiState = EditEntryUiState()
    }

}


data class EditEntryUiState(
    val temperature: String = "",

    // the selected option for the locations
    val selectedOption: String = "",
    val location: String = "",
    val customLocation: String = "",

    val observations: String = "",


    val entryAddedStatus: Boolean = false,
    val updateEntryStatus: Boolean = false,

    /* Users might upload duplicates so to handle duplicates while
     * preserving order, we use a linked hash map which behaves like
     * a regular list but each element has a unique identifier
     */
    val imageList: List<Map<String, String>> = emptyList()
)