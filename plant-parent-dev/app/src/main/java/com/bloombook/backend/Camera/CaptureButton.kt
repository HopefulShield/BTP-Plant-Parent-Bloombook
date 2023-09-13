package com.bloombook.backend.Camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/* Code Snippet from:
 * https://github.com/gefilte/compose-photo-integration/blob/
 * b1563e67f5c732ae517d73a815d80babd6ec728d/app/src/main/java/com/example/
 * composephoto/camera/CapturePictureButton.kt
 */
@Composable
fun CaptureButton(
    onClick: () -> Unit = { },
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color = if (isPressed) Color(0xFF738376) else Color.White
    val contentPadding = PaddingValues(if (isPressed) 8.dp else 10.dp)


    OutlinedButton(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.White),
        contentPadding = contentPadding,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        onClick = { },
        enabled = false
    ) {
        Button(
            modifier = Modifier.width(50.dp).height(50.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                 containerColor = color
            ),
            interactionSource = interactionSource,
            onClick = onClick
        ) {
            // Nothing here
        }
    }
}