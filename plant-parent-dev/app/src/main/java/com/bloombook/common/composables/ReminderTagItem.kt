package com.bloombook.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bloombook.models.Journals
import com.bloombook.models.Reminders
import com.google.firebase.storage.StorageReference


@Composable
fun ReminderTagItem (
    journal: Journals,
    storage: StorageReference
) {
    var imageUri = journal.displayImageUri
    storage.child("user_photos/${imageUri}").downloadUrl.addOnSuccessListener { downloadURL ->
        imageUri = downloadURL.toString()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {

        Row {
            // Wrapper for display image of the current journal
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.LightGray, RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Cover Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .graphicsLayer(
                            clip = true,
                            shape = RoundedCornerShape(24.dp)
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
                    color = Color.White,

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
                    color = Color.White,

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
