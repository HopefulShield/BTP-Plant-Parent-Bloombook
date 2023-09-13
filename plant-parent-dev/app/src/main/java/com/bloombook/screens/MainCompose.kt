package com.bloombook.screens

import android.content.Context
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.bloombook.screens.launch.launchGraph
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch


/**
 * This file houses the modalNavigationDrawer which is a wrapper composable
 * that is responsible for navigation across the main graph' screens.
 * @param navController - the navigation object to navigate
 * @param drawerState - data class with functionality to open and close the drawer
 * @param mainViewModel - view model that hoists the userID data so that all screens can access it
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompose (
    notifContext: Context,
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    mainViewModel: MainViewModel = viewModel()
) {
    val isUserSignedIn by mainViewModel.authStatus.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // snackbar host to configure snackbar messages when the user signs in and goes to home screen
    val homeSnackbarHostState = remember { SnackbarHostState() }
    var selectedItem by remember { mutableStateOf("Home") }


    // set status bar color to match app theme
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color(0xFF738376),
            darkIcons = false
        )
    }

    
    // every time the user auth status changes, we check only if they are authenticated
    LaunchedEffect(isUserSignedIn) {

        if (isUserSignedIn) {

            val user = mainViewModel.auth.currentUser

            // if user is new, display a welcome message
            if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                homeSnackbarHostState.showSnackbar("Welcome to Bloom Book!")
            }

            // otherwise welcome the user back to the app
            // We don't use the user.displayName since that takes time to udpate if a user
            // had previously changed it, so using the firestore doc name gives more
            // consistent results
            else {
                mainViewModel.getUserInfo().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        coroutineScope.launch {
                            homeSnackbarHostState.showSnackbar("Welcome Back, ${doc.getString("name")}")
                        }
                    }
                }
            }
        }
    }

    // Side navigation drawer used to navigate between main graph screens
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Menu(
                navController,
                drawerState,
                mainViewModel,
                selectedItem,
                setSelectedItem = { newSelection -> selectedItem = newSelection}
            )
        },

        // prevents the modal from being opened by swiping. This is intentional because
        // otherwise, it can be opened from the launch screens
        gesturesEnabled = false,
    ) {

        /* The content is a nested navigation graph of our screens. We defines two separate
         * graphs. The launchgraph is the graph where the app will go to by default to authenticate
         * the user. If a user is authenticated and not a new user, the mainViewModel will detect
         * that status and navigate the user to the home screen in the main graph, while setting
         * the snackbar message appropriately to welcome them back to the app. Otherwise, if the
         * user is new we set the home screen snackbar message to welcome them to the app.
         */

        NavHost(
            navController,
            startDestination = if (isUserSignedIn) NavRoutes.MainRoute.name else NavRoutes.LaunchRoute.name
        ) {
            launchGraph(navController)
            mainGraph(
                notifContext,
                drawerState,
                navController,
                homeSnackbarHostState,

                // callback function to reset nav selection after using deletes account
                setNavSelection = {selection -> selectedItem = "Home"}
            )
        }
    }
}

enum class NavRoutes {
    LaunchRoute,
    MainRoute,
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainActivityPreview() {
    //MainCompose()
}