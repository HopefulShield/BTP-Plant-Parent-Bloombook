package com.bloombook.screens.launch.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bloombook.common.composables.EmailField
import com.bloombook.common.composables.PasswordField
import com.bloombook.common.modifier.textField
import com.bloombook.R
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.common.composables.customToast
import com.bloombook.screens.MainViewModel
import com.bloombook.ui.theme.RobotoFamily
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
) {

    val loginUiState by loginViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginUiState.errorMessage) {
        if(loginUiState.errorMessage.isNotEmpty()){
            //customToast(context, loginUiState.errorMessage, Toast.LENGTH_SHORT)
            snackbarHostState.showSnackbar(loginUiState.errorMessage)
            loginViewModel.resetErrorMessage()
        }
    }

   Surface(
       modifier = Modifier.background( Color(0xFFAEC2B2)).fillMaxSize()
   ) {
       // Place the background image


       Scaffold (
           containerColor = Color(0xFFAEC2B2),
           snackbarHost = { SnackbarHost(snackbarHostState) },
       ) { padding ->

           Image(
               painter = painterResource(
                   id = R.drawable.wavy_background
               ),
               contentDescription = "Bloom Book logo",
               contentScale = ContentScale.FillBounds,
               modifier = Modifier
                   .fillMaxWidth()
                   .fillMaxHeight(.75f)
           )

           Column(
               horizontalAlignment = Alignment.CenterHorizontally,
               modifier = Modifier
                   .fillMaxSize()
                   .verticalScroll(scrollState)

           ) {
               Image(
                   painter = painterResource(
                       id = R.drawable.logo_bloombook_main
                   ),
                   contentDescription = "Bloom Book logo",
                   contentScale = ContentScale.Fit,
                   modifier = Modifier.size(160.dp)
               )

               // Header
               Text(
                   text = "Sign In",
                   style = MaterialTheme.typography.headlineLarge,
                   color = Color.White,
                   modifier = Modifier.offset(y = (-30).dp),
               )


               EmailField(
                   "Email",
                   loginUiState.email,
                   loginViewModel::onEmailChange,
                   Modifier.textField()
               )

               PasswordField(
                   "Password",
                   loginUiState.password,
                   loginViewModel::onPasswordChange,
                   Modifier.textField()
               )


               /*
               ClickableText(
                   text = buildAnnotatedString {
                       withStyle(style = SpanStyle(color = Color.White, textDecoration = TextDecoration.Underline)) {
                           append("Forgot Password?")
                       }
                   },
                   style = TextStyle(color = Color.White, fontWeight = FontWeight.Normal, fontSize = 16.sp, textAlign = TextAlign.End),
                   onClick = {
                       navController.navigate("SignUpScreen") {
                           popUpTo(navController.graph.startDestinationId)
                           launchSingleTop = true
                       } },

                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(top = 8.dp, end = 16.dp)
               )
               */

               // Sign in Button
               Button(
                   colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                   modifier = Modifier.padding(top = 104.dp, bottom = 8.dp),
                   onClick = {
                       coroutineScope.launch {
                           if(loginUiState.allFieldsCorrect){
                               loginViewModel.onLoginClick(loginUiState.email, loginUiState.password)
                           }
                           else {
                               coroutineScope.launch {
                                   snackbarHostState.showSnackbar("Please ensure all fields are filled out & correct")
                               }
                           }
                       }

                   }
               ) {
                   if(loginUiState.isLoading) {
                       LoadingSpinner()
                   }
                   else {
                       Text(
                           text = "Sign In",
                           color = Color.Gray,
                           modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                       )
                   }

               }

               Row(
                   Modifier.padding(top = 16.dp, bottom = 24.dp),
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   ClickableText(
                       text = buildAnnotatedString {
                           append("New here? ")
                           withStyle(style = SpanStyle(color = Color.White, textDecoration = TextDecoration.Underline),) {
                               append("Sign up")
                           }
                       },
                       style = TextStyle(color = Color.White, fontWeight = FontWeight.Normal, fontSize = 16.sp),
                       onClick = {
                           navController.navigate("SignUpScreen") {
                               popUpTo(navController.graph.startDestinationId)
                               launchSingleTop = true
                           } }
                   )
               }
           }
       }
   }

}

@Composable
fun SignInLinkedAccountIcons(
    emailInput:String,
    passwordInput:String
) {
    Text(
        text = "Or",
        color = Color.White,
        fontFamily = RobotoFamily,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.W300
    )
    IconButton(onClick = {

    }) {
        Icon(
            painter = painterResource(id = R.drawable.logo_google),
            contentDescription = "Sign In with Google",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(320.dp)
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
        )
    }
}




