package com.bloombook.screens.reminder.editReminder

import android.Manifest
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.common.composables.BigTextField
import com.bloombook.common.composables.CustomField2
import com.bloombook.common.composables.ReminderField
import com.bloombook.common.modifier.bigTextField
import com.bloombook.common.modifier.biggerTextField
import com.bloombook.common.modifier.textField
import com.bloombook.models.Journals
import com.bloombook.screens.reminder.notifications.NotificationScheduler
import com.bloombook.screens.reminder.ReminderState
import com.bloombook.screens.reminder.RemindersViewModel
import com.bloombook.ui.theme.RobotoFamily
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*NOTE: This code is the screen to create a reminder, not the reminder screen itself.*/
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditReminderScreen(
    reminderId: String,
    navController: NavController,
    notifContext: Context,
    editReminderViewModel: EditReminderViewModel = viewModel()
) {
    val editReminderUiState by editReminderViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTagModal by remember { mutableStateOf(false) }
    val tagSheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    val storage = editReminderViewModel.storage()

    val postNotificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(key1 = true ){
        if(!postNotificationPermission.status.isGranted){
            postNotificationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(key1 = Unit) {

        editReminderViewModel.getJournals()

        if (reminderId.isNotEmpty()) {
            editReminderViewModel.getReminder(reminderId)
        }
    }

    LaunchedEffect(
        key1 = editReminderUiState.reminderAddedStatus,
        key2 = editReminderUiState.reminderUpdateStatus
    ) {
        if (editReminderUiState.reminderAddedStatus) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Added successfully")
                navController.popBackStack()
            }
        }

        if (editReminderUiState.reminderUpdateStatus) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Updated successfully")
                navController.popBackStack()
            }
        }
    }

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
                        text = "Reminder Details",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF738376))
            )
        },
    ) { padding ->

        if (showTagModal) {
            TagModal(
                storage,
                editReminderUiState,
                editReminderViewModel,
                tagSheetState,
                listState,
                dismissModal = {
                    showTagModal = false
                }
            )
        }

        Column(
            modifier = Modifier
                .background(Color(0xFFAEC2B2))
                .verticalScroll(scrollState)
        ) {
            /* Screen content goes here */


            Spacer(modifier = Modifier.padding(top = 70.dp))
            SelectPlants(
                storage,
                editReminderViewModel,
                editReminderUiState,
                showTagModal = { showTagModal = true }
            )

            Spacer(modifier = Modifier.padding(top = 20.dp))
            DateAndTime(notifContext, editReminderViewModel, editReminderUiState)

            Spacer(modifier = Modifier.padding(top = 20.dp))
            ReminderSettings(notifContext, editReminderViewModel, editReminderUiState, reminderId)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagModal(
    storage: StorageReference,
    editReminderUiState: EditReminderState,
    editReminderViewModel: EditReminderViewModel,
    tagSheetState: SheetState,
    listState: LazyListState,
    dismissModal: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        containerColor = Color.White,
        modifier = Modifier.fillMaxHeight(0.9f),
        onDismissRequest = {
            dismissModal()
        },
        sheetState = tagSheetState
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text ="Your Plant Collection",
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.W500,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                state = listState,
                horizontalAlignment = CenterHorizontally,
                modifier = Modifier.background(color = Color.White),
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {


                items(
                    items = editReminderUiState.journalsList,
                    key = { it.documentId }
                ) {journal ->
                    ClickableJournalEntry(
                        storage,
                        journal,
                        addTag = { journal ->
                            val result = editReminderViewModel.addTagToScreen(journal)
                            if (!result) {
                                // cannot add a duplicate plant tag!
                            }
                            else {
                                // Hide the modal
                                coroutineScope.launch {
                                    tagSheetState.hide()
                                }.invokeOnCompletion {
                                    if (!tagSheetState.isVisible) {
                                        dismissModal()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}



// Contains the button and list view for selected plant tags
@Composable
fun SelectPlants(
    storage: StorageReference,
    editReminderViewModel: EditReminderViewModel,
    editReminderUiState: EditReminderState,
    showTagModal: () -> Unit
) {

    Column(modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(animationSpec = tween(100)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally,
    ){

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
            ),
            shape = RoundedCornerShape(12.dp),
            onClick = {
                showTagModal()
            }
        ) {
            Text(
                text = "Select Plants",
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
        }

        // Added tag candidates will be displayed here in a column
        if (editReminderUiState.tagList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.animateContentSize(animationSpec = tween(100))
                ) {

                    editReminderUiState.tagList.forEachIndexed { index, journal ->
                        if(index != 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                        }

                        DisplayedTag(
                            storage,
                            journal,
                            onDelete = {
                                editReminderViewModel.deleteTag(index)
                            }
                        )

                    }

                }
            }
        }
    }
}


@Composable
fun DisplayedTag(
    storage: StorageReference,
    journal: Journals,
    onDelete: () -> Unit
) {


    var uri = journal.displayImageUri
    storage.child("user_photos/${uri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for journal modal image: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for journal modal image ${uri}: ${error.message}")
    }

    // Container for journal info: displayImage, commonName, displayName
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Row {

                IconButton(
                    onClick = { onDelete() },
                ){
                    Icon(imageVector = Icons.Filled.DoNotDisturbOn,
                        contentDescription = "Delete Tag",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                }
                // Wrapper for display image of the current journal
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.LightGray, RoundedCornerShape(20.dp))
                ) {
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

                // Container for the commonName and nickName
                Column (
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = journal.commonName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        textAlign = TextAlign.Left,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    // Nickname
                    Text(
                        text = journal.nickName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        textAlign = TextAlign.Left,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                }
            }

        }
    }
}


@Composable
fun ClickableJournalEntry (
    storage: StorageReference,
    journal: Journals,
    addTag: (Journals) -> Unit,
) {

    var isClicked by rememberSaveable { mutableStateOf(false) }

    var uri = journal.displayImageUri
    storage.child("user_photos/${uri}").downloadUrl.addOnSuccessListener { downloadURL ->
        uri = downloadURL.toString()
        Log.d("cloud storage", "Retrieved download URL for journal modal image: ${uri}")
    }.addOnFailureListener { error ->
        Log.d("cloud storage", "could not get download URL for journal modal image ${uri}: ${error.message}")
    }

    // Container for journal info: displayImage, commonName, displayName
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp)
            .clickable {
                isClicked = !isClicked
                addTag(journal)
            }
    ) {
        Row(
            verticalAlignment = CenterVertically,
        ) {

            // Wrapper for display image of the current journal
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.LightGray, RoundedCornerShape(20.dp))
            ) {
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

            // Container for the commonName and nickName
            Column (
                modifier = Modifier.padding(start = 16.dp)
            ) {


                Text(
                    text = journal.commonName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                    textAlign = TextAlign.Left,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                // Nickname
                Text(
                    text = journal.nickName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                    textAlign = TextAlign.Left,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1

                )

            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndTime(context: Context, editReminderViewModel: EditReminderViewModel, editReminderUiState: EditReminderState) {

    val dateForm = DateTimeFormatter.ofPattern("M/dd/yyyy")
    val timeForm = DateTimeFormatter.ofPattern("h:mm a")

    val datePicker = android.app.DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            editReminderViewModel.onDateChange(LocalDate.of(selectedYear, selectedMonth+1, selectedDayOfMonth))
        }, editReminderUiState.date.year, editReminderUiState.date.monthValue-1, editReminderUiState.date.dayOfMonth
    )

    val timePicker = TimePickerDialog(
        context,
        { _, selectedHour: Int, selectedMinute: Int ->
            editReminderViewModel.onTimeChange(LocalTime.of(selectedHour, selectedMinute))
        }, editReminderUiState.time.hour, editReminderUiState.time.minute, false
    )

    Column( //Date & Calendar
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {

        Text(
            text = "Schedule Reminder",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            textAlign = TextAlign.Left
        )

        // Button for picking Date
        Button(
            onClick = {datePicker.show()},
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(70.dp)
                .padding(16.dp, 8.dp),
            elevation = ButtonDefaults.buttonElevation(7.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                Text(text = editReminderUiState.date.format(dateForm),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }


        // Button for picking time
        Button(onClick = {timePicker.show()},
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(70.dp)
                .padding(16.dp, 8.dp),
            elevation = ButtonDefaults.buttonElevation(7.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                Text(text = editReminderUiState.time.format(timeForm),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReminderSettings(
    context: Context,
    editReminderViewModel: EditReminderViewModel,
    editReminderUiState: EditReminderState,
    reminderId: String
) {

    val notificationScheduler = NotificationScheduler(context)
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember {
        mutableStateOf(false)
    }
    var repOptions by remember {
        mutableStateOf("")
    }
    val isFormComplete = editReminderUiState.interval.isNotEmpty()
                && editReminderUiState.message.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = "Repetition",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            textAlign = TextAlign.Left
        )
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentAlignment = Center
        ){

            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it}
            ) {

                CompositionLocalProvider(
                    LocalTextInputService provides null
                ) {
                    TextField(value = editReminderUiState.interval.ifEmpty { repOptions },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(vertical= 8.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12)),
                        placeholder = {
                            Text(
                                text = "Select Rate",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Gray,
                            focusedTextColor = Color.Gray,
                            unfocusedTextColor = Color.Gray,
                        )
                    )
                }

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false},
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text(
                            text = "Once",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            textAlign = TextAlign.Left
                        )},
                        onClick = {
                            repOptions = "Once"
                            isExpanded = false
                            editReminderViewModel.onIntervalChange("Once")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Daily",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                textAlign = TextAlign.Left
                            )},
                        onClick = {
                            repOptions = "Daily"
                            isExpanded = false
                            editReminderViewModel.onIntervalChange("Daily")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(
                            text = "Weekly",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            textAlign = TextAlign.Left
                        )},
                        onClick = {
                            repOptions = "Weekly"
                            isExpanded = false
                            editReminderViewModel.onIntervalChange("Weekly")
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            textAlign = TextAlign.Left
                        )},
                        onClick = {
                            repOptions = "Monthly"
                            isExpanded = false
                            editReminderViewModel.onIntervalChange("Monthly")
                        }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.padding(top = 20.dp))


        Text(
            text = "Reminder Message",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            textAlign = TextAlign.Left
        )

        /* Idea: have a selection of preset messages with "Custom" allowing the user to type their own */
        CustomField2(
            placeholder = "Write a custom reminder",
            value = editReminderUiState.message,
            onNewValue = editReminderViewModel::onMessageChange,
            modifier = Modifier.biggerTextField(),
            keyboardType = KeyboardType.Text,
            maxLines = 7,
            shape = RoundedCornerShape(8.dp)
        )


        //done button
        if(isFormComplete) {
            Spacer(modifier = Modifier.height(80.dp))

            AnimatedVisibility(
                modifier = Modifier.align(CenterHorizontally),
                visible = isFormComplete,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF738376)),
                    onClick = {
                        if (reminderId.isNotEmpty()) {

                            val names = editReminderUiState.journalsList.joinToString { it.nickName }
                            val prevNames = editReminderUiState.selectedReminder!!.tagList.joinToString { it.nickName }
                            notificationScheduler.cancel(reminderId, editReminderUiState.selectedReminder.message, prevNames)

                            editReminderViewModel.updateReminder(reminderId)
                            notificationScheduler.schedule(editReminderUiState, reminderId, names)
                        } else {

                            editReminderViewModel.addReminder { id ->
                                val names = editReminderUiState.journalsList.joinToString { it.nickName }
                                notificationScheduler.schedule(editReminderUiState, id, names)
                            }
                        }

                    }) {
                    Text("Done", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        else {
            Spacer(modifier = Modifier.height(160.dp))

        }

    }
}



