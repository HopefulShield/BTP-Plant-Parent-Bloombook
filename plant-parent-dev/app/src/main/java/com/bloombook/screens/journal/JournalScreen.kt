package com.bloombook.screens.journal

import android.media.Image
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.backend.Camera.CameraScreen
import com.bloombook.backend.Camera.Permissions
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.common.composables.NameField
import com.bloombook.common.modifier.textField
import com.bloombook.models.Journals
import com.bloombook.screens.MainNav
import com.bloombook.ui.theme.RobotoFamily
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JournalScreen (
    drawerState: DrawerState,
    navController: NavController,
    openModal: String,
    journalsViewModel: JournalsViewModel = viewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val journalsUiState by journalsViewModel.uiState.collectAsState()
    val storage = journalsViewModel.storage()

    val snackbarHostState = remember { SnackbarHostState() }


    // Modal visibility states
    var showAddModal by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState()

    var showEditModal by remember { mutableStateOf(false) }
    val editSheetState = rememberModalBottomSheetState()

    var isCameraVisible by remember { mutableStateOf(false) }

    // Permissions for the camera
    val activity = LocalContext.current as ComponentActivity

    // dialog to confirm user deletion of a journal card
    var isDeleting by remember {
        mutableStateOf(false)
    }

    var openDialog by remember {
        mutableStateOf(false)
    }



    // Launcher for photo library picker
    val pickMedia= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            journalsViewModel.addImage(uri.toString())
            Log.d("Journal View Model", "Selected URI: ${journalsUiState.displayImageUri}")
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



    // Grab the journals and display on screen
    LaunchedEffect(key1 = Unit) {
        journalsViewModel.getJournals()

        if(openModal == "true") {
            showAddModal = true
        }
    }



    LaunchedEffect(
        key1 = journalsUiState.deleteStatus,
    ) {
        if (journalsUiState.deleteStatus == "Successfully Deleted") {
            journalsViewModel.resetDeleteStatus()
            openDialog = false
            snackbarHostState.showSnackbar("Deleted successfully!")
        }
        else if (journalsUiState.deleteStatus.isNotEmpty()) {
            snackbarHostState.showSnackbar(journalsUiState.deleteStatus)
            journalsViewModel.resetDeleteStatus()
        }
    }

    DisposableEffect(openDialog) {
        onDispose {
            isDeleting = false
        }
    }

    // Show camera if triggered
    if (isCameraVisible) {
        CameraScreen(
            onCancel = {
                isCameraVisible = false
            },
            onImageFile = { uri ->
                // we capture the returned image file (Uri) by using the
                // the viewModel's method which will update the display Image
                journalsViewModel.addImage(uri.toString())
                Log.d("Journal View Model", "Camera URI: ${journalsUiState.displayImageUri}")

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

                    },
                    title = {
                        Text(
                            text = "Growth Journal",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    // toggle drawer to open
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF738376))
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    containerColor = Color.hsl(0f, 0f, 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {showAddModal = true}
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Open Modal", tint=Color.Black)
                }
            }

        ) { padding ->

            if (showAddModal) {
                AddPlantModal(
                    storage,
                    permissions,
                    pickMedia,
                    journalsUiState,
                    journalsViewModel,
                    addSheetState,
                    dismissModal = {
                        showAddModal = false
                    },
                    showCamera = {
                        isCameraVisible = true
                    },
                    scrollToTop = {
                        coroutineScope.launch{
                            listState.animateScrollToItem(index = 0)
                        }
                    }
                )
            }

            if (showEditModal) {
                EditPlantModal(
                    storage,
                    permissions,
                    pickMedia,
                    journalsUiState,
                    journalsViewModel,
                    editSheetState,
                    dismissModal = {
                        showEditModal = false
                    },
                    showCamera = {
                        isCameraVisible = true
                    }
                )
            }


            AnimatedVisibility(visible = openDialog) {
                AlertDialog(
                    icon = {
                        Icon(
                            Icons.Outlined.Delete,
                            tint = Color.Gray,
                            contentDescription = null
                        )
                    },
                    containerColor = Color.White,
                    onDismissRequest = {openDialog = false},
                    title = {
                        Text(
                            text = "Delete Plant?",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                         ) },
                    text = {
                        Text(
                            text = "Entries for this plant will be removed and you will no longer" +
                                    " track this plant in any reminders.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    },
                    confirmButton = {

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(0f, 0f, 0.9f) ),
                            onClick = {
                                isDeleting = true
                                journalsViewModel.deleteJournal()
                            }) {

                            if (isDeleting) {
                                LoadingSpinner()
                            }
                            else {
                                Text("Delete", color = Color.Black, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    },
                    dismissButton = {

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent ),
                            onClick = {
                                openDialog = false
                            }
                        ) {
                            Text("Cancel", color = Color.Red, style = MaterialTheme.typography.bodyLarge)

                        }

                    }
                )
            }

            Column (
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color(0xFFAEC2B2))
            ){
                LazyColumn(
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ){

                    // Add some space at the top of the column
                    item {
                        Box(modifier = Modifier.height(90.dp))
                    }

                    items(
                        items = journalsUiState.journalsList,
                        key = { it.documentId }
                    ) { journal->

                        Row(
                            modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            PlantJournalCard(
                                storage,
                                journal,
                                journalsViewModel,
                                navController,
                                showEditModal = {
                                    showEditModal = true
                                },
                                openDialog = {
                                    openDialog = true
                                }
                            )
                        }
                    }

                    // Add some space at the bottom of the column
                    item {
                        Box(modifier = Modifier.height(150.dp))
                    }
                }
            }

        }
    }





}


/*
 * The modal to add a plant. Includes fields to add a common name,
 * nick name, and a cover image. Interacts with the system's cameraX API
 * and Photo Picker Library to enable image creation & uploading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantModal(
    storage: StorageReference,
    permissions: Permissions,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    journalsUiState: JournalsUiState,
    journalsViewModel: JournalsViewModel,
    addSheetState: SheetState,
    dismissModal: () -> Unit,
    showCamera: () -> Unit,
    scrollToTop: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // dropdown expanded state
    var expanded by remember { mutableStateOf(false) }


    ModalBottomSheet(
        containerColor = Color.White,
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = {
            dismissModal()
        },
        sheetState = addSheetState
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text ="Add a New Plant",
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.W500,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )


            SearchDropdown(journalsViewModel = journalsViewModel, journalsUiState = journalsUiState)
            /*
            NameField(
                placeholder = "Common Name",
                value = journalsUiState.commonName,
                onNewValue = journalsViewModel::onCommonNameChange,
                modifier = Modifier.textField()
            )

             */

            //Maybe we can make this field mandatory
            NameField(
                placeholder = "Nickname",
                value = journalsUiState.nickName,
                onNewValue = journalsViewModel::onNickNameChange ,
                modifier = Modifier.textField()
            )


            Box (
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(16.dp, 8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 8.dp)
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {

                        // Open prompt to open camera or upload image file
                        expanded = true
                    }
                ) {
                    Text(
                        text = "Add an Image",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start
                    )
                }
                // Hidden Dropdown menu for camera and photo library
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
                                showCamera()
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
                        text = {Text("Photo Library", color = Color.White)},
                        onClick = {

                            // Launch the photo library to pick ONE image
                            expanded = false
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }

            }

            Row(
                modifier = Modifier.padding(top = 20.dp, start = 8.dp)
            ) {
                if (journalsUiState.displayImageUri.isNotEmpty()) {
                    ModalImage(
                        imageUri = journalsUiState.displayImageUri,
                        storage,
                        onDelete = {
                            journalsViewModel.deleteImage()
                        }
                    )
                }
                else if (journalsUiState.displayImageUri.isEmpty()){
                    Spacer(
                        modifier = Modifier.size(180.dp)
                    )
                }
            }


            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF738376)),
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    coroutineScope.launch {
                        if(journalsViewModel.isNamesNonEmpty()) {
                            journalsViewModel.addJournal()
                            addSheetState.hide()
                            scrollToTop()
                        }
                    }.invokeOnCompletion {
                        if (!addSheetState.isVisible) {
                            dismissModal()
                        }
                    }
                }) {
                    Text("Add Plant", color = Color.White)
                }



        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantModal(
    storage: StorageReference,
    permissions: Permissions,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    journalsUiState: JournalsUiState,
    journalsViewModel: JournalsViewModel,
    editSheetState: SheetState,
    dismissModal: () -> Unit,
    showCamera: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    // dropdown expanded state
    var expanded by remember { mutableStateOf(false) }


    ModalBottomSheet(
        containerColor = Color.White,
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = {
            journalsViewModel.resetModalInfo()
            dismissModal()
        },
        sheetState = editSheetState
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text ="Edit Plant",
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.W500,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            /*
            NameField(
                placeholder = "Common Name",
                value = journalsUiState.commonName,
                onNewValue = journalsViewModel::onCommonNameChange,
                modifier = Modifier.textField()
            )

             */

            SearchDropdown(journalsViewModel = journalsViewModel, journalsUiState = journalsUiState)

            NameField(
                placeholder = "Nickname",
                value = journalsUiState.nickName,
                onNewValue = journalsViewModel::onNickNameChange ,
                modifier = Modifier.textField()
            )



            Box (
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
                    .padding(16.dp, 8.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 8.dp)
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {

                        // Open prompt to open camera or upload image file
                        expanded = true
                    }
                ) {
                    Text(
                        text = "Change Image",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start
                    )
                }
                // Hidden Dropdown menu for camera and photo library
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
                                showCamera()
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
                        text = {Text("Photo Library", color = Color.White)},
                        onClick = {

                            // Launch the photo library to pick ONE image
                            expanded = false
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }

            }


            Row(
                modifier = Modifier.padding(top = 20.dp, start = 8.dp)
            ) {
                if (journalsUiState.displayImageUri.isNotEmpty()) {


                    ModalImage(
                        imageUri = journalsUiState.displayImageUri,
                        storage,
                        onDelete = {
                            journalsViewModel.deleteImage()
                        }
                    )

                }
                else if (journalsUiState.displayImageUri.isEmpty()){
                    Spacer(
                        modifier = Modifier.size(180.dp)
                    )
                }
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF738376)),
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    coroutineScope.launch {
                        if(journalsViewModel.isNamesNonEmpty()) {
                            journalsViewModel.updateJournal()
                            editSheetState.hide()
                        }
                    }.invokeOnCompletion {
                        if (!editSheetState.isVisible) {
                            dismissModal()
                        }
                    }
                }) {
                Text("Done", color = Color.White)
            }
        }
    }
}


@Composable
fun PlantJournalCard(
    storage: StorageReference,
    journal: Journals,
    journalsViewModel: JournalsViewModel,
    navController: NavController,
    showEditModal: () -> Unit,
    openDialog: () -> Unit
) {
    val customGray = Color(0xFF717171) // -> Light Gray

    // for calculating relative time since this journal's creation
    val prettyTime = PrettyTime(Locale.getDefault())

    // state for dropdown meu when clicking on options
    var expanded by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    var isCommonNameOverFlowed by remember { mutableStateOf(false) }

    var uri = journal.displayImageUri
    storage.child("user_photos/${uri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for plant journal card: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for plant journal card ${uri}: ${error.message}")
    }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(372.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .shadow(4.dp, shape = RoundedCornerShape(24.dp))
            .animateContentSize(animationSpec = tween(100)),
        shape = RoundedCornerShape(24.dp),

        ) {
        // Actual content inside the card
        Box(
            // Think of this as setting the background color
            modifier = Modifier
                .height(350.dp)
                .background(color = Color.hsl(0f, 0f, 0.9f)),
            contentAlignment = Alignment.BottomCenter,

            ) {

           // place image in upper half of card!
            if (journal.displayImageUri.isNotEmpty()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Text Content in lower half of card
            Box(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .background(Color.White)
                    .animateContentSize(animationSpec = tween(100)),
                contentAlignment = Alignment.TopStart
            ) {
                Column (
                    modifier = Modifier.padding(8.dp)
                ){

                    Column(modifier = Modifier
                        .animateContentSize(animationSpec = tween(100))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showMore =
                            if (isCommonNameOverFlowed) { !showMore }
                            else { showMore }
                        }
                    ) {

                        /* If showMore is true, the Text will expand
                         * Otherwise, Text will be restricted to 3 Lines of display
                         */
                        if (showMore) {
                            Text(
                                text = journal.commonName,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                            )
                        }
                        else {
                            Text(
                                text = journal.commonName,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                onTextLayout = { layout ->
                                    if (layout.hasVisualOverflow) {
                                        isCommonNameOverFlowed = true
                                    }
                                }
                            )
                        }
                    }

                    Text(
                        text = journal.nickName,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AnimatedVisibility(
                        visible = !showMore,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row (
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            // put space between the button and ellipse icon
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // View Entries Button
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =  Color.hsl(0f, 0f, 0.9f)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                onClick = {
                                    navController.navigate("${MainNav.JournalEntriesScreen}/${journal.documentId}")
                                }
                            ){
                                Text(
                                    text = "View Entries",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelLarge)
                            }

                            Text(
                                text = prettyTime.format(journal.timestamp!!.toDate()),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )


                            // Wrapper to hold icon and dropdown menu
                            Box (
                                //modifier = Modifier.fillMaxSize()
                            ) {
                                // Dropdown ellipse icon to open more options
                                IconButton(
                                    onClick = {
                                        expanded = true
                                        journalsViewModel.setSelectedJournal(journal)
                                    },
                                    modifier = Modifier.size(42.dp),
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = "Menu",
                                            tint = Color.Black,
                                        )
                                    }
                                )

                                // Hidden Dropdown menu that appears when the above MoreVert Icon is clicked
                                DropdownMenu(
                                    expanded  = expanded,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(120.dp)
                                        .zIndex(1f)
                                        .background(Color(0xFF738376)),
                                    onDismissRequest = {
                                        expanded = false
                                    }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Edit,
                                                tint = Color.White,
                                                contentDescription = null
                                            )
                                        },
                                        text = {Text("Edit", color = Color.White)},
                                        onClick = {
                                            // Close dropdown & open bottom sheet with card fields
                                            expanded = false
                                            journalsViewModel.updateEditModal()
                                            showEditModal()
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                tint = Color.White,
                                                contentDescription = null
                                            )
                                        },
                                        text = {Text("Delete", color = Color.White)},
                                        onClick = {

                                            // open dialog to confirm deletion
                                            openDialog()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// the cover image that's displayed on the add or edit modals when the
// user takes or selects a photo on their device
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalImage(
    imageUri: String,
    storage: StorageReference,
    onDelete: () -> Unit
) {

    var uri = imageUri
    storage.child("user_photos/${imageUri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for journal modal image: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for journal modal image ${uri}: ${error.message}")
    }

    BadgedBox(badge = {
        Badge(
            containerColor = Color.White,
            modifier = Modifier
                .size(36.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .offset(-20.dp, 10.dp)
        ) {
            IconButton(
                onClick = { onDelete() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = "Cancel",
                    tint = Color.LightGray,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

    }) {

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(Color.Gray, RoundedCornerShape(20.dp)),

            ){
            AsyncImage(
                model = uri,
                contentDescription = "Cover Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .graphicsLayer(
                        clip = true,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }

    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SearchDropdown(
    journalsViewModel: JournalsViewModel,
    journalsUiState: JournalsUiState
) {
    val focusManager = LocalFocusManager.current

    var searchText by remember { mutableStateOf(journalsUiState.commonName) }

    val plants by journalsViewModel.plants.collectAsState(emptyList())

    Column {
        val keyboardController = LocalSoftwareKeyboardController.current

        NameField(
            placeholder = "Common Name of Plant",
            value = searchText,
            onNewValue = {
                searchText = it
                journalsViewModel.onCommonNameChange(it)
                journalsViewModel.onSearchTextChange(it)
                if (it == "") {
                    journalsViewModel.clearPlantSuggestions()
                }
            },
            modifier = Modifier
                .textField()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        journalsViewModel.clearPlantSuggestions()
                    }
                }
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp) {
                        when (event.key) {
                            Key.Tab, Key.Enter -> {
                                journalsViewModel.clearPlantSuggestions()
                                focusManager.clearFocus()
                                true // Consume the event
                            }

                            else -> false // Don't consume other events
                        }
                    } else {
                        false
                    }
                }


        )





        if (plants.isNotEmpty()) {
            BoxWithConstraints {
                val dropdownHeight = 150.dp

                Popup(
                    alignment = Alignment.TopStart,

                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dropdownHeight)
                            .background(Color.White)
                            .align(Alignment.Center)
                    ) {
                        items(plants) { plant ->
                            DropdownListItem(
                                text = plant,
                                onItemClick = {
                                    searchText = plant
                                    journalsViewModel.onCommonNameChange(plant)
                                    journalsViewModel.clearPlantSuggestions()
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DropdownListItem(
    text: String,
    onItemClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(16.dp)
    ) {
        Text(text)
    }
}

