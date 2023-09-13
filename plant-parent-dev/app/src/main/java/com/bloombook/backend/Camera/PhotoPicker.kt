package com.bloombook.backend.Camera

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest

class PhotoPicker(
    private val pickMediaLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
) {


}