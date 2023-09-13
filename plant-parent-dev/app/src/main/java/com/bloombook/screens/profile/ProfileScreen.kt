package com.bloombook.screens.profile

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.backend.Camera.CameraScreen
import com.bloombook.backend.Camera.Permissions
import com.bloombook.common.composables.EmailField
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.common.composables.NameField
import com.bloombook.common.modifier.textField
import com.bloombook.screens.launch.LaunchNav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Generates the UI elements of the Profile Screen
 * @param drawerState - the current state of the drawer, opened or closed
 * @param userID - the unique ID of the current user
 * @param profileViewModel - the ViewModel of the Profile Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    drawerState: DrawerState,
    navController: NavController,
    setNavSelection: (String) -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
    ) {
    val profileUiState by profileViewModel.uiState.collectAsState()

    val storage = profileViewModel.storage()

    var isCameraVisible by remember { mutableStateOf(false) }

    // Permissions for the camera
    val activity = LocalContext.current as ComponentActivity

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // dialog to confirm deletion of user
    var openDialog by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    // Launcher for photo library picker
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null) {
            profileViewModel.addImage(uri.toString())
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

    LaunchedEffect(key1 = Unit) {
        profileViewModel.getUserInfo()
    }


    LaunchedEffect(
        key1 = profileUiState.deleteStatus,
        key2 = profileUiState.updatedSuccessfully
    ) {
        if (profileUiState.deleteStatus) {
            openDialog = false
            snackbarHostState.showSnackbar("Account has been deleted")
            setNavSelection("Home")
            navController.navigate(LaunchNav.LoginScreen.name)
        }
        if (profileUiState.updatedSuccessfully) {
            snackbarHostState.showSnackbar("Profile changes have been saved")
            profileViewModel.getUserInfo()
            profileViewModel.resetUpdateStatus()
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
                Log.d("camera", "${uri.toString()}")
                // we capture the returned image file (Uri) by using the
                // the viewModel's method which will update the display Image
                profileViewModel.addImage(uri.toString())
                // hide the camera composable
                isCameraVisible = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                                 // TODO: Center the profile title and add a back icon here
                },
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // opens drawer
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor =  Color(0xFF738376))
            )
        }
    ) { padding ->


        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(padding)
                //Color(0XFF6DA376))
                .verticalScroll(scrollState)
                .background(color = Color(0xFFAEC2B2))
        ) {
            val email = profileViewModel.getEmail()
            val displayName = profileUiState.savedUsername
            Log.d("user management", "displayName from getUsername(): $displayName")

            var pfpUri = profileUiState.profilePicture
            storage.child("user_photos/${profileUiState.profilePicture}").downloadUrl.addOnSuccessListener { downloadURL ->
                pfpUri = downloadURL.toString()
            }.addOnFailureListener { error ->
                Log.d("cloud storage", "could not get download URL for profile picture ${pfpUri}: ${error.message}")
            }

            PfpPic(
                pfpUri = pfpUri,
                onOpenCamera = {
                    if (!permissions.hasCameraPermission()) {
                        permissions.requestPermission()
                    }
                    else {
                        isCameraVisible = true
                    }
                },
                onOpenLibrary = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemovePhoto = {
                    profileViewModel.removeCurrentPhoto()
                }
            )
            DisplayAndOptions(
                email,
                displayName,
                openDialog = {openDialog = true},
                onSaveClick = {
                    profileViewModel.updateUserProfile(
                        username = profileUiState.username,
                        email = profileUiState.email,
                        pfpUri = profileUiState.profilePicture
                    )
                }
            )

            // alert dialog if delete user is clicked
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
                            text = "Delete Account?",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        ) },
                    text = {
                        Text(
                            text = "All your data will be lost.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(0f, 0f, 0.9f) ),
                            onClick = {
                                isDeleting = true
                                profileViewModel.deleteUser()
                            }
                        ) {
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
        }
    }
}


@Composable
fun PfpPic(
    pfpUri: String,
    onOpenCamera: () -> Unit,
    onOpenLibrary: () -> Unit,
    onRemovePhoto: () -> Unit
){
    Spacer(modifier = Modifier.padding(top = 18.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp)
        ) {
            ImagePic(
                imageUri = pfpUri,
                //profileImage = painterResource(id = R.drawable.empty_pfp),
                modifier = Modifier
                    .size(120.dp)
                    .weight(3f),
                onOpenCamera = { onOpenCamera() },
                onOpenLibrary = { onOpenLibrary() },
                onRemovePhoto = { onRemovePhoto() }
            )
        }
        Spacer(modifier = Modifier.padding(bottom = 5.dp))
    }
}



@Composable
fun ImagePic(
    imageUri: String,
    modifier: Modifier = Modifier,
    onOpenCamera: () -> Unit,
    onOpenLibrary: () -> Unit,
    onRemovePhoto: () -> Unit
){
    var expanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(Color.White, RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri.isNotEmpty()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .graphicsLayer(
                            clip = true,
                            shape = RoundedCornerShape(100.dp)
                        )
                        .fillMaxSize()
                )
            }
            else {
                Text(
                    text = "No Image",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                )
            }


            // Box to hold the edit button and dropdown menu
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {

                // Edit Button
                Box(modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFAEC2B2), RoundedCornerShape(20.dp))
                    .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                )
                {
                    IconButton(
                        colors = IconButtonColors(
                            containerColor =  Color(0xFF738376),
                            contentColor = Color.White,
                            disabledContainerColor =  Color(0xFF738376),
                            disabledContentColor =  Color.White
                        ),
                        modifier = Modifier.size(28.dp),
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // dropdown menu with camera, library, and remove photo options
                DropdownMenu(expanded = expanded,
                    modifier = Modifier
                        .width(180.dp)
                        .height(160.dp)
                        .background(Color(0xFF738376)),
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.AddAPhoto,
                                tint = Color.White,
                                contentDescription = null
                            )
                        },
                        text = { Text("Open Camera", color = Color.White) },
                        onClick = {
                            expanded = false
                            onOpenCamera()
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
                            onOpenLibrary()
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
                        text = { Text("Remove Picture", color = Color.White) },
                        onClick = {
                            // Remove current photo
                            onRemovePhoto()
                            expanded = false
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun DisplayAndOptions(
    authEmail: String,
    displayName: String,
    openDialog: () -> Unit,
    onSaveClick: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    ){
    val profileUiState by profileViewModel.uiState.collectAsState()

    val buttonColor = Color(0XFF546954)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp, start = 15.dp, bottom = 5.dp, end = 15.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = displayName,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W500,
        )
        Text(
            text = authEmail,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
    }
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ){
        /*
        Spacer(modifier = Modifier.padding(top = 12.dp))
        Text(
            text = "Name",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)

        )
        Spacer(modifier = Modifier.padding(bottom = 8.dp))

         */
        Spacer(modifier = Modifier.padding(top = 8.dp))

        NameField(
            placeholder = "Name",
            value = profileUiState.username,
            onNewValue = profileViewModel::onUsernameChange,
            modifier = Modifier.textField()
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        /*
        Text(
            text = "Email",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

         */

        EmailField(
            placeholder = "Email",
            value = profileUiState.email,
            onNewValue = profileViewModel::onEmailChange,
            modifier = Modifier.textField()
        )

        Spacer(modifier = Modifier.padding(top = 35.dp))
        Button(
            onClick = {
                openDialog()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(7.dp)
        ) {
            Text(
                text = "Delete Account",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.padding(top = 15.dp))
        Button(
            onClick = {
                onSaveClick()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(7.dp)
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(start = 2.dp, end = 2.dp)
            )
        }
        Spacer(modifier = Modifier.padding(top = 35.dp))


    }
}

