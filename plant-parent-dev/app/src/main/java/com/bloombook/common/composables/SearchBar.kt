package com.bloombook.common.composables

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloombook.common.modifier.searchField
import com.bloombook.screens.info.plantInfo.PlantInfoViewModel
import com.bloombook.ui.theme.RobotoFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(plantInfoViewModel: PlantInfoViewModel = viewModel()) {

    val searchText by plantInfoViewModel.searchText.collectAsState()

    TextField(
            value = searchText,
            onValueChange = { plantInfoViewModel.onSearchTextChange(it) },
            modifier = Modifier.searchField(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(20.dp)
                )
            },
            placeholder = {
                Text(
                    text = "Search For Plant Info...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = Color.Gray,
                focusedTextColor = Color.Gray,
                unfocusedTextColor = Color.Gray,
                selectionColors = TextSelectionColors(handleColor = Color.Gray, backgroundColor = Color.Transparent),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true,

        )


}


