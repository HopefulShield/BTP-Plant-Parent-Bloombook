package com.bloombook.screens.journal.editEntry

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.backend.Camera.CameraScreen
import com.bloombook.backend.Camera.Permissions
import com.bloombook.common.composables.CustomField2
import com.bloombook.common.composables.InteractiveImage
import com.bloombook.common.modifier.biggerTextField
import com.bloombook.common.modifier.textField
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

/**
 * This file houses the journal entry creation/edit screen for the growth journal
 * @param journalId - the journal's firestore document id
 * @param entryId - can be empty, which signifies that a user is adding an entry, otherwise editing
 * @param navController - the navigation object to navigate
 * @param editEntryViewModel - view model that handles UI logic and backend communication with firestore
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun EditEntryScreen(
    journalId: String,
    entryId: String,
    navController: NavController,
    editEntryViewModel: EditEntryViewModel = viewModel(),
) {


    val editEntryUiState = editEntryViewModel.editEntryUiState

    // reference to the cloud storage so that each image can retrieve its cloud url
    val storage = editEntryViewModel.storage()

    val isFormsNotBlank = editEntryUiState.observations.isNotBlank()

    // State for displaying snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    // scroll state for screen
    val scrollState = rememberScrollState()

    // list state for the LazyRow of images
    val imageListState = rememberLazyListState()

    // dropdown expanded state
    var expanded by remember { mutableStateOf(false) }

    var isCameraVisible by remember { mutableStateOf(false) }

    // Permissions for the camera
    val activity = LocalContext.current as ComponentActivity



    // Launcher for photo library picker
    val pickMedia= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            editEntryViewModel.addImage(uri.toString())
        }
        else {
            Log.d("PhotoPicker", "Selected URI: $uri")
        }
    }


    // Launcher for permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        if (isGranted) {
            isCameraVisible = true
        }
    }

    // The actual permission object to request permission for camera
    val permissions = remember {
        Permissions(
            activity,
            permissionLauncher,
            onRequestPermissionsResult = {isGranted ->
                if (isGranted) {
                    // Open the camera screen
                    isCameraVisible = true
                }
            }
        )
    }


    // if id is blank, we have to create a new entry
    // if not blank, we have to update an entry
    val isEntryIdNotBlank = entryId.isNotBlank()
    LaunchedEffect(key1 = Unit) {
        if (isEntryIdNotBlank) {
            editEntryViewModel.getEntry(journalId, entryId)
        } else {
            editEntryViewModel.resetState()
        }
    }

    LaunchedEffect(
        key1 = editEntryUiState.entryAddedStatus,
        key2 = editEntryUiState.updateEntryStatus
    ) {
        if (editEntryUiState.entryAddedStatus) {
            snackbarHostState.showSnackbar("Added successfully")
            editEntryViewModel?.resetEntryAddedStatus()
            navController.popBackStack()
        }

        if (editEntryUiState.updateEntryStatus) {
            snackbarHostState.showSnackbar("Updated successfully")
            editEntryViewModel.resetEntryAddedStatus()
            navController.popBackStack()
        }
    }


    if (isCameraVisible) {
        CameraScreen(
            onCancel = {
                isCameraVisible = false
            },
            onImageFile = { uri ->

                // we capture the returned image file (Uri) by using the
                // the viewModel's method which will update the display Image
                editEntryViewModel.addImage(uri.toString())

                // hide the camera composable
                isCameraVisible = false
            }
        )
    }
    else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            text = "Details",
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
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .animateContentSize(animationSpec = tween(100))
            ) {

                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )  {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp)
                            .height(50.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            expanded = true
                        }
                    ) {
                        Text(
                            text = "Add Image",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start
                        )
                    }


                    // Hidden Dropdown menu that appears when the above MoreVert Icon is clicked
                    DropdownMenu(
                        expanded  = expanded,
                        modifier = Modifier
                            .width(180.dp)
                            .height(110.dp)
                            .background(Color(0xFF738376)),
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        // Options for camera or photo library
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.AddAPhoto,
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            },
                            text = {Text("Open Camera", color = Color.White)},
                            onClick = {

                                expanded = false
                                if (!permissions.hasCameraPermission()) {
                                    permissions.requestPermission()
                                }
                                else {
                                    isCameraVisible = true
                                }

                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.PhotoLibrary,
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            },
                            text = { Text("Photo Library", color = Color.White) },
                            onClick = {

                                // Launch the photo library to pick ONE image
                                expanded = false
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                    }
                }


                // Display the
                if (editEntryUiState.imageList.isNotEmpty()) {
                    LazyRow(
                        state = imageListState,
                        contentPadding = PaddingValues(start = 16.dp, end = 32.dp)
                    ) {


                        items(editEntryUiState.imageList) { map ->
                            val id = map["key"]!!
                            val imageUri = map["value"]!!

                            Box (
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(120.dp)
                                    .padding(end = 16.dp)
                                    .animateItemPlacement(),
                                contentAlignment = Alignment.Center

                            ){
                                EntryImage(
                                    imageUri = imageUri,
                                    storage = storage,
                                    onDelete = { editEntryViewModel.deleteImage(id)}
                                )
                            }

                        }

                    }
                }


                // If images are added, display them as image cards of size(100.dp) otherwise
                // just use a spacer
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Temperature",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Left
                )

                CustomField2(
                    placeholder = "Temperature",
                    value = editEntryUiState.temperature,
                    onNewValue = editEntryViewModel::onTempChange,
                    modifier = Modifier.textField(),
                    keyboardType = KeyboardType.Number,
                    maxLines = 1,
                    shape = RoundedCornerShape(24)
                )

                Spacer(modifier = Modifier.padding(top = 20.dp))

                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Left
                )


                val radioOptions = listOf("Indoors", "Outdoors", "Custom")

                val (isCustomTextVisible, setCustomTextVisibility) =
                    remember { mutableStateOf(editEntryUiState.selectedOption == "Custom") }

                Column (modifier = Modifier
                    .animateContentSize(animationSpec = tween(100))
                ) {

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Column (
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        ){
                            radioOptions.forEach { text ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (text == editEntryUiState.selectedOption),
                                            onClick = {

                                                editEntryViewModel.onSelectedOptionChange(text)

                                                // if custom option selected, show the text field
                                                // and reset the regular location
                                                if (text == "Custom") {
                                                    editEntryViewModel.onLocationChange("")
                                                    setCustomTextVisibility(true)
                                                }

                                                // otherwise hide the custom text field, and
                                                // set the location to the current option chosen
                                                else {
                                                    setCustomTextVisibility(false)
                                                    editEntryViewModel.onLocationChange(text)
                                                    editEntryViewModel.onCustomLocationChange("")

                                                }
                                            }
                                        )
                                ) {
                                    RadioButton(
                                        selected = (text == editEntryUiState.selectedOption),
                                        onClick = {

                                            editEntryViewModel.onSelectedOptionChange(text)

                                            if (text == "Custom") {
                                                editEntryViewModel.onLocationChange("")
                                                setCustomTextVisibility(true)
                                            }
                                            else {
                                                setCustomTextVisibility(false)
                                                editEntryViewModel.onLocationChange(text)
                                                editEntryViewModel.onCustomLocationChange("")
                                            }

                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color.Gray,
                                            unselectedColor = Color.Gray
                                        ),
                                        modifier = Modifier.align(CenterVertically)
                                    )

                                    Text(
                                        text = text,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(CenterVertically)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(CenterHorizontally),
                        visible = (editEntryUiState.selectedOption == "Custom"),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CustomField2(
                            placeholder = "Write a custom location",
                            value = editEntryUiState.customLocation,

                            // update the custom location field
                            onNewValue = editEntryViewModel::onCustomLocationChange ,
                            modifier = Modifier.textField(),
                            keyboardType = KeyboardType.Text,
                            maxLines = 1,
                            shape = RoundedCornerShape(8)
                        )
                    }
                }



                Spacer(modifier = Modifier.padding(top = 20.dp))

                Text(
                    text = "Observations",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Left
                )
                CustomField2(
                    placeholder = "Record your observations (required)",
                    value = editEntryUiState.observations,
                    onNewValue = editEntryViewModel::onObservationChange,
                    modifier = Modifier.biggerTextField(),
                    keyboardType = KeyboardType.Text,
                    maxLines = 7,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.padding(top = 30.dp))


                // Done button
                AnimatedVisibility(
                    modifier = Modifier.align(CenterHorizontally),
                    visible = isFormsNotBlank,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF738376)),
                        modifier = Modifier.padding(0.dp, 28.dp),
                        onClick = {
                            if (isEntryIdNotBlank) {
                                editEntryViewModel.updateEntry(journalId, entryId)
                            } else {
                                editEntryViewModel.addEntry(journalId)
                            }
                        }) {
                        Text("Done", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(100.dp))

                }
            }
        }
    }
}


/*
 * Creates a rounded image with a badge icon to delete the image
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryImage(
    imageUri: String,
    storage: StorageReference,
    onDelete: () -> Unit
) {

    Box(
        modifier = Modifier.size(110.dp)
    ) {

        var uri = imageUri
        storage.child("user_photos/${imageUri}").downloadUrl.addOnSuccessListener { downloadURL ->
            uri = downloadURL.toString()
            Log.d("cloud storage", "Retrieved download URL for entry image")

        }.addOnFailureListener { error ->
            Log.d("cloud storage", "could not get download URL for entry image ${uri}: ${error.message}")
        }

        AsyncImage(
            model = uri,
            contentDescription = "Cover Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .graphicsLayer(
                    clip = true,
                    shape = RoundedCornerShape(20.dp)
                )
                .fillMaxSize()
        )

        Box(
            modifier = Modifier
                .offset(x = 8.dp, y = (-8).dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFAEC2B2), RoundedCornerShape(20.dp))
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
            ) {
                IconButton(
                    onClick = { onDelete() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = "Cancel",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

    }
}