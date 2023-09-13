package com.bloombook.backend.Camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ClickableViewAccessibility", "RestrictedApi")
@Composable
fun CameraScreen (
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onCancel: () -> Unit,
    onImageFile: (Uri) -> Unit = {},
    cameraViewModel: CameraViewModel = viewModel()
) {


    val activity = LocalContext.current as ComponentActivity
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraState by cameraViewModel.cameraUiState.collectAsState()

    // name for the file
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())

    // Create time stamped name and MediaStore entry
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            Log.d("Content Values", "Writing to pictures/bloom-book")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Bloom-Book")
        }
    }


    // Create output options object to store file and metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            activity.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    var previewUseCase by remember {
        mutableStateOf<UseCase>(Preview.Builder().build())
    }


    val isFlashOn = cameraViewModel.isFlashOn.collectAsState()
    val flashMode = when (isFlashOn.value) {
        true -> FLASH_MODE_ON
        false -> FLASH_MODE_OFF
    }
    var imageCaptureUseCase by remember {
        mutableStateOf(
            ImageCapture.Builder()
                .setFlashMode(flashMode)
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
        )
    }


    var savedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isPhotoPreviewVisible by rememberSaveable { mutableStateOf(false) }
    val zoomScale = cameraViewModel.zoomScale.collectAsState()

    val cameraProviderInit = ProcessCameraProvider.getInstance(context)
    var cameraProvider: ProcessCameraProvider = cameraProviderInit.get()


    val listener = object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val currentZoomRatio  = cameraState.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
            val delta = detector.scaleFactor
            val newZoom = currentZoomRatio * delta
            cameraState.cameraControl?.setZoomRatio(newZoom)
            cameraViewModel.setZoom(newZoom)
            return true
        }
    }

    val scaleGestureDetector = ScaleGestureDetector(context, listener)


    // initialize when camera first starts
    LaunchedEffect(Unit) {
        cameraProvider?.unbindAll()

        cameraViewModel.updateCameraConfigs(
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
            )
        )
    }

    LaunchedEffect(previewUseCase) {

        cameraProvider?.unbindAll()
        cameraViewModel.updateCameraConfigs(cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
        ))
    }



    // Update the imageUseCase when the flash is toggled
    LaunchedEffect(isFlashOn.value) {

        val newFlashMode = when (isFlashOn.value) {
            true -> FLASH_MODE_ON
            false -> FLASH_MODE_OFF
        }

        val newImageCaptureUseCase = ImageCapture.Builder()
            .setFlashMode(newFlashMode)
            .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraProvider.unbind(imageCaptureUseCase)
        cameraViewModel.updateCameraConfigs(cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, newImageCaptureUseCase))
        imageCaptureUseCase = newImageCaptureUseCase

    }



    Box (
        // add a higher z index so no screen obstructs the camera preview
        modifier = Modifier
            .fillMaxHeight()
            .zIndex(1f)
    ) {

        if (isPhotoPreviewVisible) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = savedUri,
                    contentDescription = "preview image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            clip = true,
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF738376))
                    .align(Alignment.TopCenter),
            ) {

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back icon to retake photo
                    IconButton(
                        onClick = {
                            isPhotoPreviewVisible = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIos,
                            contentDescription = "Retake Photo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        onClick = {
                            onImageFile(savedUri!!)
                        }
                    ) {
                        Text(
                            text = "Next",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }


            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(174.dp)
                    .background(Color(0xFF738376))
                    .align(Alignment.BottomCenter),
            )
        }


        // If photo was not taken yet, show capture screen
        else {


                var touchPoint by remember { mutableStateOf<MeteringPoint?>(null) }
                Box( modifier = Modifier.fillMaxSize()) {
                    // Camera Preview with tap focus and zoom



                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {context ->
                            val previewView = PreviewView(context).apply {
                                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }


                            previewUseCase = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            previewView.setOnTouchListener { _, motionEvent ->
                                scaleGestureDetector.onTouchEvent(motionEvent)

                                when (motionEvent.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        val factory = previewView.meteringPointFactory
                                        touchPoint = factory.createPoint(motionEvent.x, motionEvent.y)
                                        val action = FocusMeteringAction.Builder(touchPoint!!).build()
                                        cameraState.cameraControl?.startFocusAndMetering(action)
                                        return@setOnTouchListener true
                                    }
                                    else -> {
                                        touchPoint = null
                                        return@setOnTouchListener false
                                    }
                                }
                            }

                            previewView


                        }
                    )
                    // Zoom scale display
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter)
                        .zIndex(2f)) {

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            enabled = false,
                            modifier = Modifier
                                .alpha(0.5f)
                                .zIndex(2f),
                            onClick = {
                                // nothing
                            }
                        ) {
                            Text(
                                text = "${zoomScale.value}x",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                }






            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF738376))
                    .align(Alignment.TopCenter),
            ) {

                Row(modifier = Modifier
                    .fillMaxWidth(0.57f)
                    .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // Cancel button to go back to app
                    IconButton(
                        onClick = {
                            onCancel()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Flash icon button that toggles between ON and OFF
                    IconButton(
                        onClick = {
                            if (isFlashOn.value) {
                                cameraViewModel.setFlash(false)
                            }
                            else {
                                cameraViewModel.setFlash(true)
                            }
                        }
                    ) {
                        if (isFlashOn.value) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        else {
                            Icon(
                                imageVector = Icons.Rounded.FlashOff,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }


            // Bottom row that holds the capture button and flip camera button
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .background(Color(0xFF738376))
                    .align(Alignment.BottomCenter),
            ){


                CaptureButton(
                    onClick = {
                        coroutineScope.launch {


                            val executor =  ContextCompat.getMainExecutor(context)

                            imageCaptureUseCase.takePicture(
                                outputOptions,
                                executor,
                                object: ImageCapture.OnImageSavedCallback {

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraX", "Photo capture failed: ${exception.message}")
                                    }

                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                                        // open the preview screen and show the captured photo
                                        // at the same time, save the output in a temporary variable
                                        if (output.savedUri != null) {
                                            //onImageFile(savedUri)
                                            savedUri = output.savedUri
                                            isPhotoPreviewVisible = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }


    }
}


