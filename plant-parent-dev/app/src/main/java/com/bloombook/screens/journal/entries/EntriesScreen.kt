package com.bloombook.screens.journal.entries

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ChipColors
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.R
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.models.Entries
import com.bloombook.screens.MainNav
import com.bloombook.ui.theme.RobotoFamily
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EntriesScreen(
    journalId: String,
    navController: NavController,
    entriesViewModel: EntriesViewModel = viewModel()
) {

    val entriesUiState by entriesViewModel.entriesUiState.collectAsState()

    val listState = rememberLazyListState()
    var openDialog by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    var selectedEntry: Entries? by remember {
        mutableStateOf(null)
    }



    LaunchedEffect(key1 = Unit) {
        entriesViewModel.getJournalEntries(journalId)
    }

    LaunchedEffect(
        key1 = entriesUiState.entryDeletedStatus,
    ) {
        if (entriesUiState.entryDeletedStatus) {
            openDialog = false
            entriesViewModel.resetDeleteStatus()
        }
    }

    DisposableEffect(openDialog) {
        onDispose {
            isDeleting = false
        }
    }

    Scaffold(
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
                    text = "Journal Entries",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                    )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF738376))
        )
    },
    floatingActionButton = {
        FloatingActionButton(
            containerColor = Color.hsl(0f, 0f, 0.9f),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                val emptyId = ""
                navController.navigate("${MainNav.EditEntriesScreen}/$journalId")
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Entry")
        }
    }
) { padding ->
    Column (
        modifier = Modifier
            .padding(padding)
            .background(color = Color(0xFFAEC2B2))
            .fillMaxHeight()
    ){
       LazyColumn(
           state = listState,
           modifier = Modifier.fillMaxWidth(),
           contentPadding = PaddingValues(top = 8.dp)
       ) {


           items(
               entriesUiState.entriesList
           ) {entry ->

               Row(
                   modifier = Modifier.animateItemPlacement(),
               ) {
                   EntryItem(
                       entriesViewModel.storage(),
                       entries = entry,
                       onOptionsClick = {entriesViewModel.setSelectedEntry(entry)},
                       onEditClick = {
                           val entryId = entry.documentId
                           val journalId = entry.journalId
                           navController.navigate("${MainNav.EditEntriesScreen}/$journalId/$entryId")
                       },
                       onDeleteClick = {
                           openDialog = true
                           selectedEntry = entry
                       },
                       navController = navController
                   )
               }
           }
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
                        text = "Delete Entry?",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    ) },
                text = {
                    Text(
                        text = "Entry will be permanently removed from this journal ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                },
                confirmButton = {

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(0f, 0f, 0.9f) ),
                        onClick = {
                            isDeleting = true

                            // the entry will already have been selected at this point
                            entriesViewModel.deleteEntry(journalId)

                        }) {
                        if (isDeleting) {
                            LoadingSpinner()
                        }
                        else {
                            Text(
                                "Delete",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        }
                },
                dismissButton = {

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent ),
                        onClick = {  openDialog = false }
                    ) {
                        Text("Cancel", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                    }

                }
            )
        }

    }
}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryItem(
    storage: StorageReference,
    entries: Entries,
    onOptionsClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {

    var expanded by remember { mutableStateOf(false) }


    var uri = entries.imageList.getOrNull(0)?.get("value")
    Log.d("Entry image", "Default journal entry image uri: ${uri}")

    storage.child("user_photos/${uri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for journal entry image: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for journal entry image ${uri}: ${error.message}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp, 8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
    ) {
        Row (
            modifier = Modifier
                .background(Color.White)
                .fillMaxHeight()
        ) {
            val displayImageUri = entries.imageList.getOrNull(0)?.get("value")
            val entryId = entries.documentId
            val journalId = entries.journalId

            if (!displayImageUri.isNullOrEmpty()) {
                Box {
                    AsyncImage(
                        model = displayImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier
                        .offset(x = (-8).dp, y = 8.dp)
                        .align(Alignment.TopEnd)) {
                        Box (modifier = Modifier
                            .zIndex(1f)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .size(32.dp)

                        ){
                            IconButton(
                                onClick = {
                                    navController.navigate("${MainNav.GalleryScreen}/$journalId/$entryId")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = "View Gallery",
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row (verticalAlignment = Alignment.Top){
                    // Wrapper to hold icon and dropdown menu
                    Column {
                        Text(
                            text = formatDate(entries.timestamp),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 6.dp),

                            )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = formatTime(entries.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        // Dropdown ellipse icon to open more options
                        IconButton(
                            onClick = {
                                onOptionsClick()
                                expanded = true },
                            modifier = Modifier.size(28.dp),
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "meatballz",
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
                                    onEditClick.invoke()
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
                                    onDeleteClick.invoke()
                                }
                            )
                        }
                    }
                }
                Row (modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(vertical = 8.dp)){
                    var locationTag = entries.selectedOption
                    if (entries.selectedOption == "Custom") {
                        locationTag = entries.customLocation
                    }
                    if (entries.temperature.isNotBlank()) {
                        SuggestionChip(
                            modifier = Modifier.padding(end = 12.dp),
                            enabled = false,
                            colors = ChipColors(
                                containerColor = Color(0xFF738376),
                                labelColor = Color.White,
                                leadingIconContentColor = Color.Transparent,
                                trailingIconContentColor = Color.Transparent,
                                disabledContainerColor = Color(0xFF738376),
                                disabledLabelColor = Color.White,
                                disabledLeadingIconContentColor = Color.Transparent,
                                disabledTrailingIconContentColor = Color.Transparent
                            ),
                            border = null,
                            shape = RoundedCornerShape(24.dp),
                            onClick = { },
                            label = {
                                Text(
                                    text = "${entries.temperature} F",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                )
                            }
                        )
                    }
                    if (locationTag.isNotBlank()) {
                        SuggestionChip(
                            enabled = false,
                            colors = ChipColors(
                                containerColor = Color(0xFF738376),
                                labelColor = Color.White,
                                leadingIconContentColor = Color.Transparent,
                                trailingIconContentColor = Color.Transparent,
                                disabledContainerColor = Color(0xFF738376),
                                disabledLabelColor = Color.White,
                                disabledLeadingIconContentColor = Color.Transparent,
                                disabledTrailingIconContentColor = Color.Transparent
                            ),
                            border = null,
                            shape = RoundedCornerShape(24.dp),
                            onClick = { },
                            label = {
                                Text(
                                    text = locationTag,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White, overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }

                }
                Text(
                    text = entries.observations,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
            }

        }
    }
}

private fun formatDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("EEE, LLL d yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
private fun formatTime(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}