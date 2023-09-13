package com.bloombook.backend.Camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner



/* This class will check the Manifest file if the user has permissions
 * If not, the user will be prompted to grant the app access to a feature like the Camera
 * The methods defined can be called anywhere where the user wants to take a photo
 */
class Permissions(
    private val activity: ComponentActivity,
    private val permissionLauncher: ActivityResultLauncher<String>,
    private val onRequestPermissionsResult: (Boolean) -> Unit
) {
    private val cameraPermission = Manifest.permission.CAMERA


    /*
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
            onRequestPermissionsResult(isGranted)
    }

     */

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            cameraPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        if (!hasCameraPermission()) {
            permissionLauncher.launch(cameraPermission)
        }
        else {
            onRequestPermissionsResult(true)
        }
    }




}