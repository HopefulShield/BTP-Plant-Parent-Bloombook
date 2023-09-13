package com.bloombook.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bloombook.screens.launch.LaunchNav
import kotlinx.coroutines.launch

/**
 * Generates the UI elements of the navigation drawer menu and
 * handles the navigation between screens and the menu
 * @param navController - the navigation data
 * @param drawerState - the current state of the drawer, opened or closed
 * @param mainViewModel - the ViewModel of the main function (i.e the drawer)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    navController: NavController,
    drawerState: DrawerState,
    mainViewModel: MainViewModel,
    selectedItem: String,
    setSelectedItem: (String) -> Unit
)
{
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if(mainViewModel.getAuthStatus) {
            //selectedItem = "Home"
            setSelectedItem("Home")
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF738376),
        drawerContentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(start = 25.dp, top = 5.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 15.dp, bottom = 15.dp, end = 15.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }


            MenuItem(
                navController,
                navDestination = MainNav.HomeScreen.name,
                displayText = "Home",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Home") }
            )

            MenuItem(
                navController,
                navDestination = MainNav.ProfileScreen.name,
                displayText = "Profile",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = {setSelectedItem("Profile") }
            )


            MenuItem(
                navController,
                navDestination = MainNav.JournalScreen.name,
                displayText = "Growth Journal",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Growth Journal")}
            )

            MenuItem(
                navController,
                navDestination = MainNav.RemindersScreen.name,
                displayText = "Reminders",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Reminders") }
            )


            MenuItem(
                navController,
                navDestination = MainNav.PlantInfoScreen.name,
                displayText = "Plant Information",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Plant Information")}
            )

            MenuItem(
                navController,
                navDestination = MainNav.ImageSearchScreen.name,
                displayText = "Plant Image Search",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Plant Image Search") }

            )

            MenuItem(
                navController,
                navDestination = LaunchNav.LoginScreen.name,
                displayText = "Sign Out",
                drawerState,
                mainViewModel,
                selectedItem,
                onSelectedItem = { setSelectedItem("Home")  }
            )
        }
    }
}


/**
 * Creates a link to a screen using clickable text
 * @param navController - the navigation data
 * @param navDestination - the screen to be navigated to
 * @param displayText - the text displayed on the menu indicating the destination screen
 * @param drawerState - the current state of the drawer, opened or closed
 * @param mainViewModel - the ViewModel of the main function (i.e the drawer)
 * */
@Composable
fun MenuItem(
    navController: NavController,
    navDestination: String,
    displayText: String,
    drawerState: DrawerState,
    mainViewModel: MainViewModel,
    selectedItem: String,
    onSelectedItem: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    NavigationDrawerItem(

        label = {
                Text(
                    displayText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            },
        icon = {

            if(displayText == "Home") {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            if(displayText == "Profile") {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            if(displayText == "Growth Journal") {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = "Growth Journal",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            if(displayText == "Reminders") {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Reminders",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            if(displayText == "Plant Information") {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Plant Information",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            if(displayText == "Plant Image Search") {
                Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = "Plant Image Search",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (displayText == "Sign Out"){
                Icon(
                    imageVector = Icons.Rounded.Logout,
                    contentDescription = "Sign Out",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },

        selected = (displayText == selectedItem),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF616e63),
            unselectedContainerColor = Color(0xFF738376)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),

        onClick = {
            coroutineScope.launch {

                onSelectedItem()

                if (navDestination == LaunchNav.LoginScreen.name ) {
                    mainViewModel.signOut()
                }

                else if (navDestination == MainNav.JournalScreen.name) {
                    // we pass in "false" to indicate that we shouldn't open the journal
                    // screen's addPlantModal
                    navController.navigate("${navDestination}/false")
                }
                else {
                    navController.navigate(navDestination) {
                        launchSingleTop = true
                    }
                }

                drawerState.close()
            }
        }
    )
}
