package com.bloombook.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


/*
 * Used for displaying interactive images on the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveImage (
    imageUri: String,
    boxDimensions: Dp,
    badgeDimensions: Dp,
    iconOffset: Offset,
    onDelete: () -> Unit
) {
    BadgedBox(badge = {
        Badge(
            containerColor = Color.White,
            modifier = Modifier
                .size(badgeDimensions)
                .background(Color.White, RoundedCornerShape(20.dp))
                .offset(iconOffset.x.dp, iconOffset.y.dp)
        ) {
            IconButton(
                onClick = { onDelete() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = "Cancel",
                    tint = Color.LightGray,
                    modifier = Modifier.size(badgeDimensions)
                )
            }
        }

    }) {

        Box(
            modifier = Modifier
                .size(boxDimensions)
                .background(Color.Gray, RoundedCornerShape(20.dp)),

            ) {
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
}