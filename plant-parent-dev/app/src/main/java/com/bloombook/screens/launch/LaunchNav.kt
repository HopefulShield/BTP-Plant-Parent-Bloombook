package com.bloombook.screens.launch

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bloombook.screens.MainViewModel
import com.bloombook.screens.NavRoutes
import com.bloombook.screens.launch.login.LoginScreen
import com.bloombook.screens.launch.signUp.SignUpScreen

fun NavGraphBuilder.launchGraph(
    navController: NavController
)
{
    navigation(startDestination = LaunchNav.LoginScreen.name, route = NavRoutes.LaunchRoute.name) {

        composable(LaunchNav.LoginScreen.name) {
            LoginScreen(navController = navController)
        }

        composable(LaunchNav.SignUpScreen.name) {
            SignUpScreen(navController = navController)
        }
    }
}
enum class LaunchNav {
    LoginScreen,
    SignUpScreen
}