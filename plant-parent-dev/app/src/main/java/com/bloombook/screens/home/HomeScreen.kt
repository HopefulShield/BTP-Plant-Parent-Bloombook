package com.bloombook.screens.home

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.common.composables.ReminderTagItem
import com.bloombook.models.Journals
import com.bloombook.models.Reminders
import com.bloombook.screens.MainNav
import com.bloombook.screens.reminder.notifications.NotificationScheduler
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Generates the UI elements of the Home Screen
 * @param drawerState - the current state of the drawer, opened or closed
 * @param userID - the unique ID of the current user
 * @param homeViewModel - the ViewModel of the Home Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    navController: NavController,
    notifContext: Context,
    homeSnackbarHostState: SnackbarHostState,
    setNavSelection: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val homeScreenState by homeViewModel.uiState.collectAsState()
    val storage = homeViewModel.storage()
    val scrollState = rememberScrollState()
    val notificationScheduler = NotificationScheduler(notifContext)

    var openDialog by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        homeViewModel.getJournals()
        homeViewModel.getDailyReminders()
    }
    LaunchedEffect(
        key1 = homeScreenState.deleteStatus,
    ) {
        if (homeScreenState.deleteStatus) {
            openDialog = false
            homeViewModel.resetDeleteStatus()
        }
    }

    DisposableEffect(openDialog) {
        onDispose {
            isDeleting = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(homeSnackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
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
                            modifier = Modifier
                                .size(42.dp)
                                .weight(.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF738376))
            )
        }
    ) { padding ->

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
                        text = "Delete Reminder?",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    ) },
                text = {
                    Text(
                        text = "Reminder will be permanently removed.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.hsl(0f, 0f, 0.9f) ),
                        onClick = {
                            isDeleting = true
                            homeViewModel.deleteReminder()
                            val prevNames = homeScreenState.selectedReminder!!.tagList.joinToString { it.nickName }
                            notificationScheduler.cancel(homeScreenState.selectedReminder!!.documentId, homeScreenState.selectedReminder!!.message, prevNames)
                        }
                    ) {
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
        // TODO: Make this column scrollable
        Column(
            modifier = Modifier
                .background(Color(0xFFAEC2B2))
                .fillMaxHeight()
                .verticalScroll(scrollState),
        ) {

            Spacer(modifier = Modifier.height(74.dp))
            JournalsGallery(homeScreenState, storage, navController, setNavSelection)
            Spacer(modifier = Modifier.height(24.dp))
            DailyReminders(homeViewModel, homeScreenState, storage, navController, showDialog = {openDialog = true})
        }
    }
}


@Composable
fun JournalsGallery(
    homeScreenState: HomeScreenState,
    storage: StorageReference,
    navController: NavController,
    setNavSelection: (String) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 8.dp)
    ) {
        Text(
            text = "My Plant Collection",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
        Box(modifier = Modifier.padding(start = 16.dp)) {
            IconButton(
                onClick = {
                    navController.navigate("${MainNav.JournalScreen.name}/true")
                    setNavSelection("Growth Journal") },
                colors = IconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White,
                    disabledContentColor = Color.Black
                ),
                modifier = Modifier.size(30.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Reminder",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp))
            }
        }
    }


    if (homeScreenState.journalsList.isNotEmpty()) {

        // for now i put a lazy row, but talk to the Product owner
        // about what the best layout should be for this section: Grid, column, etc?
        LazyRow (
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 32.dp)
        ) {
            items(
                items = homeScreenState.journalsList
            ) {journal ->

                JournalCard(journal, storage)
            }
        }
    }

    else {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp)
                .height(200.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You have no plants in your collection",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }


}

@Composable
fun JournalCard(
    journal: Journals,
    storage: StorageReference
) {
    var uri = journal.displayImageUri
    storage.child("user_photos/${uri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for home journal card: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for home journal card ${uri}: ${error.message}")
    }

    Card(
        modifier = Modifier
            .width(170.dp)
            .fillMaxHeight()
            .padding(end = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.hsl(0f, 0f, 0.9f)),
            contentAlignment = Alignment.BottomCenter
        ){
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .background(Color.White),
                contentAlignment = Alignment.TopStart
            ){
                Column {
                    Spacer(modifier = Modifier.padding(top = 2.dp))
                    Text(
                        text = journal.commonName,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = journal.nickName,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }


}




@Composable
fun DailyReminders(
    homeViewModel: HomeViewModel,
    homeScreenState: HomeScreenState,
    storage: StorageReference,
    navController: NavController,
    showDialog: () -> Unit,
){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Daily Reminders",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp)
        )

        Box(modifier = Modifier.padding(start = 16.dp)) {
            IconButton(
                onClick = { navController.navigate(MainNav.EditReminderScreen.name) },
                colors = IconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White,
                    disabledContentColor = Color.Black
                ),
                modifier = Modifier.size(30.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Reminder",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp))
            }
        }
    }

    // If there are daily reminders, display them.
    if (homeScreenState.dailyRemindersList.isNotEmpty()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .fillMaxHeight()
            .animateContentSize(animationSpec = tween(100)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            homeScreenState.dailyRemindersList.forEach { reminder ->
                DailyReminderCard(
                    homeViewModel,
                    storage,
                    reminder,
                    onComplete = { homeViewModel.onCompleteReminder(reminder.documentId) },
                    onEdit = { navController.navigate("${MainNav.EditReminderScreen.name}/${reminder.documentId}") },
                    onDelete = { showDialog() }
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    else {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(200.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No reminders for today!",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }





}






/**
 * A Composable to display a reminder on a card. Should contain information about
 * its scheduled date, interval, its next due date if it repeats, last completed date,
 * message, and plant tags (tags will be done later)
 * @param reminder - the reminder object pulled from firebase
 * @param onDelete - callback function to delete this specific reminder from the database
 * @param onEdit - callback function to edit a card and navigate to the EditReminderScreen
 */
@Composable
fun DailyReminderCard(
    homeViewModel: HomeViewModel,
    storage: StorageReference,
    reminder: Reminders,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var expandTags by remember {mutableStateOf(false)}

    // for calculating relative time since this reminder's creation
    val prettyTime = PrettyTime(Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
    ) {

        Column(
            modifier = Modifier.animateContentSize(animationSpec = tween(100))
        ) {
            Row (modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top){
                Column {
                    Text(
                        text = com.bloombook.screens.reminder.formatDate(reminder.scheduledDateTime!!),
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = com.bloombook.screens.reminder.formatTime(reminder.scheduledDateTime!!),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (reminder.lastCompletedDate != null) {
                    Text(
                        text = "Done ${prettyTime.format(reminder.lastCompletedDate.toDate())}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 30.dp, top = 2.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    // Dropdown ellipse icon to open more options
                    IconButton(
                        onClick = {
                            homeViewModel.setSelectedReminder(reminder)
                            expanded = true
                        },
                        modifier = Modifier.size(28.dp),
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
                            .background(Color(0xFF738376)),
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            },
                            text = {Text("Mark as Done", color = Color.White)},
                            onClick = {
                                // Close dropdown & open bottom sheet with card fields
                                expanded = false
                                onComplete()
                            }
                        )
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
                                onEdit()
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
                                onDelete()
                            }
                        )
                    }
                }
            }


            // Column to hold the content of the reminder such as its
            // interval, next due date, message, and plant tags
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
            ) {

                // Use different colors for each interval label
                Row (modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                ){
                    val bgColor = when (reminder.interval) {
                        "Daily" -> 0xFFF77C47
                        "Weekly" -> 0xFFE88256
                        "Monthly" -> 0xFFD98865
                        "Once" -> 0xFFD5C236
                        else -> 0xFF738376
                    }
                    SuggestionChip(
                        enabled = false,
                        modifier = Modifier.padding(end = 12.dp),
                        colors = ChipColors(
                            containerColor = Color(bgColor),
                            labelColor = Color.White,
                            leadingIconContentColor = Color.Transparent,
                            trailingIconContentColor = Color.Transparent,
                            disabledContainerColor = Color(bgColor),
                            disabledLabelColor = Color.White,
                            disabledLeadingIconContentColor = Color.Transparent,
                            disabledTrailingIconContentColor = Color.Transparent
                        ),
                        border = null,
                        shape = RoundedCornerShape(24.dp),
                        onClick = { },
                        label = {
                            Text(
                                text = reminder.interval,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )

                    // If a reminder does not repeat, there's no need to display a next due date
                    if(reminder.interval != "Once") {
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
                                    text = "Due again ${
                                        com.bloombook.screens.reminder.formatDate(
                                            reminder.nextDueDate!!
                                        )
                                    }",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                // Visual divider to separate upper and lower content
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                // Container to hold message and any plant tags
                Box(modifier = Modifier.padding(bottom = 8.dp)) {

                    Column {
                        // The reminder message itself
                        Text(
                            text = reminder.message,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp),
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                        if (reminder.tagList.isNotEmpty()) {
                            // Row to contain any plant tags
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.clickable { expandTags = true }
                                ) {

                                    if (reminder.tagList.size == 1) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .padding(4.dp)
                                                .background(
                                                    Color.LightGray,
                                                    RoundedCornerShape(20.dp)
                                                )
                                        ) {
                                            var imageUri = reminder.tagList[0].displayImageUri
                                            storage.child("user_photos/${imageUri}").downloadUrl.addOnSuccessListener { downloadURL ->
                                                imageUri = downloadURL.toString()
                                            }
                                            AsyncImage(
                                                model = imageUri,
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
                                    if (reminder.tagList.size >= 2) {
                                        ConstraintLayout {
                                            val (image1, image2) = createRefs()

                                            Box(
                                                modifier = Modifier
                                                    .constrainAs(image1) {
                                                        start.linkTo(parent.start)
                                                        top.linkTo(parent.top)
                                                        bottom.linkTo(parent.bottom)
                                                    }
                                                    .size(39.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = 3.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .background(Color.LightGray)
                                            ) {
                                                var image1Uri = reminder.tagList[0].displayImageUri
                                                storage.child("user_photos/${image1Uri}").downloadUrl.addOnSuccessListener { downloadURL ->
                                                    image1Uri = downloadURL.toString()
                                                }
                                                AsyncImage(
                                                    model = image1Uri,
                                                    contentDescription = "Plant Tag",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .graphicsLayer(
                                                            clip = true,
                                                            shape = RoundedCornerShape(24.dp)
                                                        )
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .constrainAs(image2) {
                                                        start.linkTo(image1.end)
                                                        top.linkTo(image1.top)
                                                        end.linkTo(image1.end)
                                                    }
                                                    .size(39.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = 3.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .background(Color.LightGray)
                                                //.aspectRatio( 1f, matchHeightConstraintsFirst = true)
                                            ) {

                                                var image2Uri = reminder.tagList[1].displayImageUri
                                                storage.child("user_photos/${image2Uri}").downloadUrl.addOnSuccessListener { downloadURL ->
                                                    image2Uri = downloadURL.toString()
                                                }
                                                AsyncImage(
                                                    model = image2Uri,
                                                    contentDescription = "Plant Tag",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .graphicsLayer(
                                                            clip = true,
                                                            shape = RoundedCornerShape(24.dp)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                                if (reminder.tagList.size > 2) {
                                    Text(
                                        text = "+${reminder.tagList.size - 2} more",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expandTags,
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(150.dp)
                                    .background(Color(0xFF738376)),
                                onDismissRequest = { expandTags = false }
                            ) {
                                reminder.tagList.forEach { journal ->
                                    ReminderTagItem(journal, storage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("EEE, LLL d", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
private fun formatTime(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

