package com.bloombook.screens.info.plantInfo



import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bloombook.common.composables.LoadingSpinner
import com.bloombook.common.composables.SearchBar
import com.bloombook.screens.MainNav
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantInfoScreen(
    drawerState: DrawerState,
    navController: NavController,
    plantInfoViewModel: PlantInfoViewModel = viewModel()
) {

    val coroutineScope = rememberCoroutineScope()


    val plants by plantInfoViewModel.plants.collectAsState(emptyList())
    val isSearching by plantInfoViewModel.isSearching.collectAsState()


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFAEC2B2)
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Plant Information",
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
            }
        ) { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(color = Color(0xFFAEC2B2))
            ) {


                Spacer(modifier = Modifier.padding(top = 180.dp))

                Text(
                    text = "Bloom Engine",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                SearchBar(plantInfoViewModel)


                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSpinner()
                    }
                }
                else {
                    if(plants.isNotEmpty()){
                        Box(modifier = Modifier
                            .height(220.dp)
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .padding(16.dp, 8.dp)
                        ) {
                            LazyColumn (
                                modifier = Modifier.padding(8.dp)
                            ){
                                Log.d("LazyColumn", "LazyColumn items count: ${plants.size}")
                                items(plants) { item ->
                                    PlantView(
                                        searched_plant = item ,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}






@Composable
fun PlantView(
    searched_plant: String,
    navController: NavController
) {


    Box(
        modifier = Modifier
            .fillMaxWidth(.80f)
            .background(color = Color.White)
            .clickable {
                navController.navigate("${MainNav.PlantCardScreen.name}/$searched_plant")
            },


    ) {
        Text(
            text = searched_plant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = TextStyle(textAlign = TextAlign.Center)
        )
    }
}







