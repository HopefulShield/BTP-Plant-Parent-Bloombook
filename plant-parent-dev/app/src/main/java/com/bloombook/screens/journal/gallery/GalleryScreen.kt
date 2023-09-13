package com.bloombook.screens.journal.gallery

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.bloombook.models.Entries
import com.bloombook.screens.journal.editEntry.EditEntryViewModel
import com.bloombook.screens.journal.editEntry.EntryImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen (
    journalId: String,
    entryId: String,
    navController: NavController,
) {

    // list state for the LazyColumn of images
    val imageListState = rememberLazyListState()

    // reference to the cloud storage so that each image can retrieve its cloud url
    val storage = FirebaseStorage.getInstance().reference

    // image list of selected entry
    var imageList by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    LaunchedEffect(journalId, entryId) {
        getImageList(journalId, entryId) {fetchedImageList ->
            Log.d("cloud storage", "Fetched image list: $fetchedImageList")
            Log.d("cloud storage", "Image list before fetching: $imageList")
            imageList = fetchedImageList
            Log.d("cloud storage", "Image list after fetching: $imageList")
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back Button",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Gallery",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF738376))
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .background(color = Color(0xFFAEC2B2))
                .fillMaxSize()
                .animateContentSize(animationSpec = tween(100))
        ) {
            if (imageList.isNotEmpty()) {
                LazyColumn(
                    state = imageListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp),
                    contentPadding = PaddingValues(top = 90.dp, bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(imageList) {map ->
                        val id = map["key"]!!
                        val imageUri = map["value"]!!
                        Log.d("cloud storage", "key: $id")
                        Log.d("cloud storage", "value: $imageUri")

                        Box(
                            modifier = Modifier
                                .size(350.dp)
                                .padding(18.dp)
                        ) {
                            GalleryImage (
                                imageUri = imageUri,
                                storage = storage,
                            )
                        }
                    }
                }
            }
        }
    }
}


// displays a single gallery image
@Composable
fun GalleryImage(
    imageUri: String,
    storage: StorageReference
) {
    Box(modifier = Modifier.fillMaxSize()
    ) {
        var uri = imageUri
        storage.child("user_photos/${imageUri}").downloadUrl.addOnSuccessListener { downloadURL ->
            uri = downloadURL.toString()
        }.addOnFailureListener { error ->
            Log.d("cloud storage", "could not get download URL for entry image ${uri}: ${error.message}")
        }

        AsyncImage(
            model = uri,
            contentDescription = "Gallery Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .graphicsLayer(
                    clip = true,
                    shape = RoundedCornerShape(20.dp)
                )
                .fillMaxSize()
        )
    }
}


// function to retrieve imageList from a specific entry
fun getImageList(journalId: String, entryId: String, callback: (List<Map<String, String>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userId = Firebase.auth.currentUser?.uid.orEmpty()

    // construct the document path
    val entryRef = db.collection("users")
        .document(userId)
        .collection("journals")
        .document(journalId)
        .collection("entries")
        .document(entryId)

    // retrieve the document snapshot
    entryRef.get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val entryData = document.toObject(Entries::class.java)
                    Log.d("cloud storage", "Entry data: $entryData")
                    if (entryData != null) {
                        val imageList = entryData.imageList
                        Log.d("cloud storage", "Entry image list: $imageList")
                        callback(imageList)
                    }
                }
            }
        }
}