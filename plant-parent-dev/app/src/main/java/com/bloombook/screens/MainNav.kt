package com.bloombook.screens

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bloombook.screens.home.HomeScreen
import com.bloombook.screens.info.plantCard.PlantCardScreen
import com.bloombook.screens.info.plantInfo.PlantInfoScreen
import com.bloombook.screens.journal.JournalScreen
import com.bloombook.screens.journal.editEntry.EditEntryScreen
import com.bloombook.screens.journal.entries.EntriesScreen
import com.bloombook.screens.journal.gallery.GalleryScreen
import com.bloombook.screens.plantImageSearch.ImageSearchScreen
import com.bloombook.screens.profile.ProfileScreen
import com.bloombook.screens.reminder.editReminder.EditReminderScreen
import com.bloombook.screens.reminder.RemindersScreen



@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.mainGraph(
    notifContext: Context,
    drawerState: DrawerState,
    navController: NavController,
    homeSnackbarHostState: SnackbarHostState,
    setNavSelection: (String) -> Unit
) {

    // navigation graph when the user reaches the home screen
    navigation(startDestination = MainNav.HomeScreen.name, route = NavRoutes.MainRoute.name) {


        composable(MainNav.HomeScreen.name) {
            HomeScreen(drawerState, navController, notifContext, homeSnackbarHostState, setNavSelection)
        }

        composable(MainNav.ProfileScreen.name) {
            ProfileScreen(drawerState, navController = navController, setNavSelection)
        }


        composable(route = MainNav.RemindersScreen.name) {
            RemindersScreen(drawerState, navController, notifContext)
        }
        composable(route = "${MainNav.EditReminderScreen.name}/{reminder_id}") {
            val reminderId = it.arguments?.getString("reminder_id") ?: ""
            EditReminderScreen(reminderId = reminderId, navController, notifContext)
        }
        composable(route = "${MainNav.EditReminderScreen.name}") {
            EditReminderScreen(reminderId = "", navController = navController, notifContext)
        }


        composable(route = "${MainNav.JournalScreen.name}/{open_modal}") {

            // Because the user is given the ability to add a plant from the home screen,
            // navigating to the journal screen should trigger the addPlantModal to open.
            // Otherwise, if we're navigating there from the nav drawer, don't open it
            val openModal = it.arguments?.getString("open_modal") ?: ""
            JournalScreen(drawerState, navController, openModal)
        }
        composable(route = "${MainNav.JournalEntriesScreen.name}/{journal_id}") {
            val journalId = it.arguments?.getString("journal_id") ?: ""
            EntriesScreen(journalId = journalId, navController = navController)
        }
        composable(route = "${MainNav.EditEntriesScreen.name}/{journal_id}/{entry_id}") {
            val journalId = it.arguments?.getString("journal_id") ?: ""
            val entryId = it.arguments?.getString("entry_id") ?: ""
            EditEntryScreen(journalId = journalId, entryId = entryId, navController = navController)
        }
        composable(route = "${MainNav.EditEntriesScreen.name}/{journal_id}") {
            val journalId = it.arguments?.getString("journal_id") ?: ""
            EditEntryScreen(journalId = journalId, entryId = "", navController = navController)
        }
        composable(route = "${MainNav.GalleryScreen.name}/{journal_id}/{entry_id}") {
            val journalId = it.arguments?.getString("journal_id") ?: ""
            val entryId = it.arguments?.getString("entry_id") ?: ""
            GalleryScreen(journalId = journalId, entryId = entryId, navController = navController)
        }


        composable(route = MainNav.PlantInfoScreen.name) {
            PlantInfoScreen(drawerState, navController)
        }
        composable(route = "${MainNav.PlantCardScreen.name}/{searched_plant}") {
            PlantCardScreen(navController)
        }

        composable(route = MainNav.ImageSearchScreen.name) {
            ImageSearchScreen(drawerState, navController)
        }
    }
}

enum class MainNav {
    HomeScreen,
    ProfileScreen,
    RemindersScreen,
    EditReminderScreen,
    JournalScreen,
    JournalEntriesScreen,
    EditEntriesScreen,
    PlantInfoScreen,
    PlantCardScreen,
    GalleryScreen,
    ImageSearchScreen
}