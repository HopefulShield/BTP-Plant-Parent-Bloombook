package com.bloombook.screens.plantImageSearch

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.FilterDrama
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.backend.Camera.CameraScreen
import com.bloombook.backend.Camera.Permissions
import com.bloombook.common.composables.LoadingSpinner
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchScreen (
    drawerState: DrawerState,
    navController: NavController,
    imageSearchViewModel: ImageSearchViewModel = viewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val imageSearchUiState by imageSearchViewModel.uiState.collectAsState()


    var isCameraVisible by remember { mutableStateOf(false) }

    val activity = LocalContext.current as ComponentActivity

    val pickMedia= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            imageSearchViewModel.addImage(uri.toString())
            Log.d("Journal View Model", "Selected URI: ${imageSearchUiState.displayImageUri}")
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

    LaunchedEffect(true) {

    }


    if (isCameraVisible) {
        CameraScreen(
            onCancel = {
                isCameraVisible = false
            },
            onImageFile = { uri ->
                // we capture the returned image file (Uri) by using the
                // the viewModel's method which will update the display Image
                imageSearchViewModel.addImage(uri.toString())
                Log.d("Journal View Model", "Camera URI: ${imageSearchUiState.displayImageUri}")

                // hide the camera composable
                isCameraVisible = false
            }
        )
    }
    else {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {

                    },
                    title = {
                        Text(
                            text = "Plant Image Search",
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


        ) { padding ->
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(color = Color(0xFFAEC2B2))
                    ) {
                TakeImage(
                    permissions,
                    pickMedia,
                    imageSearchUiState,
                    imageSearchViewModel,
                    showCamera = {
                        isCameraVisible = true
                    }
                )
            }
        }
    }

}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TakeImage(
    permissions: Permissions,
    pickMedia: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    imageSearchUiState: ImageSearchUiState,
    imageSearchViewModel: ImageSearchViewModel,
    showCamera: () -> Unit,
    ) {

    val isSearching by imageSearchViewModel.isSearching.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val identifiedPlants = imageSearchViewModel.identifiedPlants.value
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    // used to show/hide the identified plant list
    val showDialog = remember {mutableStateOf(false)}

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
            ) {

        if (imageSearchUiState.displayImageUri.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize(0.8f)
            ) {
                Text(
                    text = "To identify a plant...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(20.dp)
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 8.dp)
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF738376),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (!permissions.hasCameraPermission()) {
                            permissions.requestPermission()
                        }
                        else {
                            showCamera()


                        }
                    }
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            Icons.Rounded.AddAPhoto,
                            tint = Color.White,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Take a Photo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Start,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.size(16.dp)
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 8.dp)
                        .height(50.dp)
                    ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF738376),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(7.dp),
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                    }
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            Icons.Rounded.PhotoLibrary,
                            tint = Color.White,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pick an Image",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 20.dp, start = 8.dp)
        ) {
            if (imageSearchUiState.displayImageUri.isNotEmpty()) {

                ImageUI(
                    imageUri = imageSearchUiState.displayImageUri,
                    onDelete = {
                        imageSearchViewModel.deleteImage()
                    }
                )
            }
            else if (imageSearchUiState.displayImageUri.isEmpty()){
            Spacer(
                modifier = Modifier.size(180.dp)
            )
            }
        }

    }

    if (imageSearchUiState.displayImageUri.isNotEmpty()) {
        // Button to identify plant
        Spacer(
            modifier = Modifier.size(16.dp)
        )

        Button(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .padding(bottom = 8.dp)
                .height(50.dp)
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF738376),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(7.dp),
            onClick = {
                showDialog.value = true

                val contentResolver = context.contentResolver
                val imageUri = Uri.parse(imageSearchUiState.displayImageUri)

                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(imageUri, projection, null, null, null)

                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val imagePath = cursor.getString(columnIndex)

                    val imageFile = File(imagePath)
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart =
                        MultipartBody.Part.createFormData("images", imageFile.name, requestFile)


                    // Now you can use the `imagePart` in your API call
                    imageSearchViewModel.identifyPlant(listOf(imagePart))

                    cursor.close()
                }


            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                // Show loading indicator
                if(isSearching) {
                    LoadingSpinner()
                }
                Text(
                    text = "Identify Plant",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        Box (
            contentAlignment = Alignment.Center,
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.64f)
                    .padding(bottom = 8.dp)
                    .height(50.dp)
                ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF738376),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(7.dp),
                onClick = {

                    // Open prompt to open camera or upload image file
                    expanded = true
                }
            ) {
                Text(
                    text = "Change Image",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
            // Hidden Dropdown menu that appears when the above MoreVert Icon is clicked
            DropdownMenu(
                expanded  = expanded,
                modifier = Modifier
                    .width(200.dp)
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

    }

    //possible icons: eco, emojiNature, filterDrama, filterVintage, Grass, LocalFlorist, Park, Yard
    //show identified plants
    AnimatedVisibility(visible = (showDialog.value && !isSearching)) {
        AlertDialog(

            icon = {
                Icon(
                    Icons.Outlined.Eco,
                    tint = Color.Gray,
                    contentDescription = null,
                )
            },
            containerColor = Color.White,
            onDismissRequest = {
                showDialog.value = false},
                //imageSearchViewModel.clearIdentifiedPlants()},
            title = {
                Text(
                    text = "Identified Plants",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                ) },
            text = {
                if (identifiedPlants != null) {
                    if(identifiedPlants.isEmpty()) {

                        Text(
                            text = "No identified plants",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                    else {
                        LazyColumn {
                            items(identifiedPlants) { plantMatch ->
                                IdentifiedPlantView(plantMatch = plantMatch)
                            }
                        }
                    }

                } else {
                    Log.d("No identified plants", "No identified plants")
                }
            },
            confirmButton = {
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(0f, 0f, 0.9f) ),
                    onClick = {
                        showDialog.value = false
                        //imageSearchViewModel.clearIdentifiedPlants()
                    }) {

                    Text("Close", color = Color.Black, style = MaterialTheme.typography.bodyLarge)
                }

            },
            modifier = Modifier
                .width(350.dp)
                .height(350.dp)
        )
    }

    if (!showDialog.value && !isSearching) {
        imageSearchViewModel.clearIdentifiedPlants()

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUI(
    imageUri: String,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier.size(280.dp)
    ) {

        AsyncImage(
            model = imageUri,
            contentDescription = "Image",
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
                .offset(x = 12.dp, y = (-14).dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

    }
}

@Composable
fun IdentifiedPlantView(
    plantMatch: PlantMatch
) {

    val percentMatch = plantMatch.score * 100
    val formattedNumber = String.format("%.2f", percentMatch)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .border(2.dp, Color.LightGray),


        ) {
        Column{
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plantMatch.species.scientificName,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.7F)
                )
                Text(
                    text = "$formattedNumber% Match",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                )
            }

            if (plantMatch.species.commonNames.firstOrNull() != null) {
                Text(
                    text = plantMatch.species.commonNames.first(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }

        }
    }
}


