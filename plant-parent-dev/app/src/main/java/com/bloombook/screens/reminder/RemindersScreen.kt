package com.bloombook.screens.reminder
import android.content.Context
import android.widget.CalendarView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bloombook.screens.MainNav
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.bloombook.common.composables.ReminderTagItem
import androidx.compose.runtime.DisposableEffect
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.models.Reminders
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageReference
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.platform.LocalContext
import com.bloombook.screens.reminder.notifications.NotificationScheduler
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar


/**
 * A Composable that wraps the layout for displaying the reminders
 * split across two views: List and Calendar views
 * @param drawerState - data class with functionality to open and close the drawer
 * @param navController - the navigation object to navigate
 * @param remindersViewModel - handles backend logic to grab data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    drawerState: DrawerState,
    navController: NavController,
    notifContext: Context,
    remindersViewModel: RemindersViewModel = viewModel()
) {
    val remindersUiState by remindersViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val storage = remindersViewModel.storage()

    // list state for the List View of reminders
    val listState = rememberLazyListState()

    // state to handle toggle switch between the list and calendar views
    var toggleCalendar by rememberSaveable { mutableStateOf(false)}

    var openDialog by remember {
        mutableStateOf(false)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }


    var selectedDate by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    val notificationScheduler = NotificationScheduler(notifContext)



    LaunchedEffect(key1 = Unit) {
        remindersViewModel.getReminders()
        remindersViewModel.selectedDate(Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate())
    }

    LaunchedEffect(
        key1 = remindersUiState.deleteStatus,
    ) {
        if (remindersUiState.deleteStatus) {
            openDialog = false
            remindersViewModel.resetDeleteStatus()
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
                    IconToggleButton(
                        checked = toggleCalendar,
                        colors = IconToggleButtonColors (
                            checkedContainerColor = Color(0xFF738376),
                            containerColor = Color(0xFF616e63),
                            contentColor = Color(0xFF738376),
                            checkedContentColor = Color.White,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent
                        ),
                        onCheckedChange = {toggleCalendar = it}) {
                        Icon(imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar Toggle",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Reminders",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
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
                            modifier = Modifier
                                .size(42.dp)
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
                onClick = {
                    navController.navigate(MainNav.EditReminderScreen.name)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Entry", tint=Color.Black)
            }
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
                            remindersViewModel.deleteReminder()
                            notificationScheduler.cancel(remindersUiState.selectedReminder!!.documentId, remindersUiState.selectedReminder!!.message, "Dummy")
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


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconToggleButton(checked = toggleCalendar, onCheckedChange = { toggleCalendar = it }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar Toggle",
                    tint = if (toggleCalendar) Color(0XFF546954) else Color.LightGray,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Column(
            verticalArrangement = Top,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xFFAEC2B2))
        ) {
            if (toggleCalendar) {
                Calendar(
                    remindersUiState,
                    remindersViewModel,
                    navController,
                    storage,
                    selectedDate,
                    setSelectedDate = { time ->
                        selectedDate = time
                    },
                    showDialog = {openDialog = true}
                )
            }
            else {
                List(
                    remindersUiState,
                    remindersViewModel,
                    navController,
                    listState,
                    storage,
                    showDialog = {openDialog = true}
                )
            }
        }
    }
}



/**
 * A Composable for the calendar view that displays reminders based on selected day
 * @param remindersUiState - data class with functionality to open and close the drawer
 * @param remindersViewModel - handles backend logic to grab data
 */

@Composable
fun Calendar(
    remindersUiState: ReminderState,
    remindersViewModel: RemindersViewModel,
    navController: NavController,
    storage: StorageReference,
    selectedDate: Long,
    setSelectedDate: (Long) -> Unit,
    showDialog: () -> Unit
) {
    val dateForm = DateTimeFormatter.ofPattern("M/dd/yyyy")
    val timeForm = DateTimeFormatter.ofPattern("h:mm a")


    var displayDate = when (remindersUiState.selectedDate.format(dateForm)) {
        LocalDate.now().format(dateForm) -> "Today"
        LocalDate.now().minusDays(1).format(dateForm) -> "Yesterday"
        LocalDate.now().plusDays(1).format(dateForm) -> "Tomorrow"
        else -> remindersUiState.selectedDate.format(dateForm)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column (
        verticalArrangement = Top,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .verticalScroll(scrollState)
            .animateContentSize(animationSpec = tween(100))
            .padding(start = 16.dp, end = 16.dp, top = 90.dp)
    )  {


        Box(
            modifier = Modifier
                .background(color = Color.White, RoundedCornerShape(12.dp))
                .padding(start = 16.dp, end = 16.dp)
        ) {

            AndroidView(
                factory = { context ->
                    CalendarView(context).apply {
                        date = remindersUiState.calendarDate
                        setOnDateChangeListener { _, year, month, day ->
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                            }

                            setSelectedDate(selectedCalendar.timeInMillis)// Update the selected date
                            //date = selectedDate
                            remindersViewModel.selectedDate(LocalDate.of(year, month+1, day))
                            //remindersViewModel.saveCalendarDate(selectedDate)
                        }
                    }
                },
                update = { calendarView ->
                    calendarView.date = selectedDate  // Update the date when the view is updated
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Reminders for ${displayDate}",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start
        )

        if (remindersUiState.remindersCalendar.isNotEmpty()) {
            remindersUiState.remindersCalendar.forEach { reminder ->

                ReminderCard(
                    storage,
                    reminder,
                    remindersViewModel,
                    onDelete = {
                        showDialog()
                    },
                    onEdit = {
                        navController.navigate("${MainNav.EditReminderScreen.name}/${reminder.documentId}")
                    }
                )

            }
        }

        else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reminders",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}




/**
 * A Composable that wraps the layout for displaying all the reminders in a list view
 * Reminders will be ordered by their scheduled times, in ascending order
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun List(
    remindersUiState: ReminderState,
    remindersViewModel: RemindersViewModel,
    navController: NavController,
    listState: LazyListState,
    storage: StorageReference,
    showDialog: () -> Unit
){


    Column(
        verticalArrangement = Top,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {

        if (remindersUiState.remindersList.isNotEmpty()) {
            LazyColumn(
                state = listState,
                horizontalAlignment = CenterHorizontally,
                contentPadding = PaddingValues(top = 90.dp, bottom = 100.dp)
            ) {
                items(
                    items = remindersUiState.remindersList,
                    key = { it.documentId }
                ) { reminder ->

                    Row(
                        modifier = Modifier.animateItemPlacement()
                    ) {
                        ReminderCard(
                            storage,
                            reminder,
                            remindersViewModel,
                            onDelete = {
                                showDialog()
                            },
                            onEdit = {
                                navController.navigate("${MainNav.EditReminderScreen.name}/${reminder.documentId}")
                            }
                        )

                    }

                }
            }
        }
        else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 90.dp)
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Center
            ) {
                Text(
                    text = "You have no reminders",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
fun ReminderCard(
    storage: StorageReference,
    reminder: Reminders,
    remindersViewModel: RemindersViewModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit
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
                        text = formatDate(reminder.scheduledDateTime!!),
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTime(reminder.scheduledDateTime!!),
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
                            remindersViewModel.setSelectedReminder(reminder)
                            expanded = true },
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
                                    text = "Due again ${formatDate(reminder.nextDueDate!!)}",
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
                                verticalAlignment = CenterVertically,
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


fun formatDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("EEE, LLL d", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
fun formatTime(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}


