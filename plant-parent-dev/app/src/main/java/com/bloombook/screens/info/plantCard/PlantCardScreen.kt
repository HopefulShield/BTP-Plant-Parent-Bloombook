package com.bloombook.screens.info.plantCard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bloombook.screens.MainNav
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantCardScreen (
    navController: NavController,
    plantCardViewModel: PlantCardViewModel = viewModel()
) {


    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    val plantCardUIState by plantCardViewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        plantCardViewModel.fetchInfo()

    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFAEC2B2)
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {

                    },
                    navigationIcon = {
                        IconButton (
                            onClick = {
                                navController.navigate(MainNav.PlantInfoScreen.name)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back Button",
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
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .background(color = Color(0xFFAEC2B2))
            ){
                InfoHeader(plantCardUIState)
                PlantImage(plantCardUIState)
                GeneralInfo(plantCardUIState)
                CareInfo(plantCardUIState)
                Spacer(modifier = Modifier.padding(30.dp))
            }
        }
    }
}


@Composable
fun InfoHeader(plantCardUIState: PlantCardState) {
    Column {

        if (plantCardUIState.common_name.isNotEmpty()) {
            ExpandableText(text = plantCardUIState.common_name)
        }

        if(plantCardUIState.botanical_name.isNotEmpty()) {
            Text(
                text = plantCardUIState.botanical_name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                textAlign = TextAlign.Left
            )
        }
    }
}

@Composable
fun PlantImage(plantCardUIState: PlantCardState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.padding(top = 16.dp))


        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp, 8.dp)
                .background(Color.White, RoundedCornerShape(20.dp)),

        ){
            AsyncImage(
                model = plantCardUIState.image_url,
                contentDescription = "Plant Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .graphicsLayer(
                        clip = true,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }

}

@Composable
fun PlantInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(22.dp, 8.dp)
    )
}

@Composable
fun GeneralInfo(
    plantCardUIState: PlantCardState
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "General Information",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp),
            textAlign = TextAlign.Left
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            Column (
                modifier = Modifier.padding(top = 14.dp, bottom = 14.dp )
            ){

                if (plantCardUIState.family != "") {
                    PlantInfoText("Family: ${plantCardUIState.family}")
                }
                if (plantCardUIState.plant_type != "") {
                    PlantInfoText("Plant Type: ${plantCardUIState.plant_type}")
                }
                if (plantCardUIState.native_area != "") {
                    PlantInfoText("Native Area: ${plantCardUIState.native_area}")
                }
                if (plantCardUIState.mature_size != "") {
                    PlantInfoText("Typical Size: ${plantCardUIState.mature_size}")
                }
                if (plantCardUIState.bloom_time != "") {
                    PlantInfoText("Bloom Time: ${plantCardUIState.bloom_time}")
                }
                if (plantCardUIState.flower_color != "") {
                    PlantInfoText("Flower Color: ${plantCardUIState.flower_color}")
                }
                if (plantCardUIState.toxicity != "") {
                    PlantInfoText("Toxicity: ${plantCardUIState.toxicity}")
                }

            }
        }
    }
}



@Composable
fun CareInfo(
    plantCardUIState: PlantCardState
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = "Care Information",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,

            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp),
            textAlign= TextAlign.Left
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ){
            Column (
                modifier = Modifier.padding(top = 14.dp, bottom = 14.dp )
            )
            {

                if (plantCardUIState.sun_exposure != "") {
                    PlantInfoText("Required Sun Exposure: ${plantCardUIState.sun_exposure}")
                }
                if (plantCardUIState.soil_type != "") {
                    PlantInfoText("Soil Type: ${plantCardUIState.soil_type}")
                }
                if (plantCardUIState.soil_pH != "") {
                    PlantInfoText("Soil pH: ${plantCardUIState.soil_pH}")
                }
                if (plantCardUIState.hardiness_zones != "") {
                    PlantInfoText("Hardiness Zones: ${plantCardUIState.hardiness_zones}")
                }
            }
        }

    }
}

@Composable
fun ExpandableText(text: String) {
    Column(Modifier.fillMaxSize()) {

        // Creating a boolean value for
        // storing expanded state
        var showMore by remember { mutableStateOf(false) }


        Column (
            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
        )
        {

            // Creating a clickable modifier
            // that consists text
            Column(modifier = Modifier
                .animateContentSize(animationSpec = tween(100))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showMore = !showMore }) {

                /* If showMore is true, the Text will expand
                 * Otherwise, Text will be restricted to 3 Lines of display
                 */
                if (showMore) {
                    Text(text = text,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 10.dp),
                        textAlign = TextAlign.Left
                    )
                } else {
                    Text(text = text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 10.dp),
                        textAlign = TextAlign.Left
                    )
                }
            }
        }
    }
}



