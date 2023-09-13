package com.bloombook.backend.Camera

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.ListenableFuture


// Code snippet from:
// https://github.com/gefilte/compose-photo
// -integration/blob/b1563e67f5c732ae517d73a815d80babd6ec728d/app
// /src/main/java/com/example/composephoto/camera/CameraPreview.kt


/*
CameraPreview(
    onUseCase = {
        previewUseCase = it
    },
    camera!!,
    context,
    lifecycleOwner
)
 */
@Composable
fun CameraPreview(
    onUseCase: (UseCase) -> Unit = {},
    camera: Camera,
    context: Context,
    lifecycleOwner: LifecycleOwner
) {

    val cameraControl = camera.cameraControl
    val cameraInfo = camera.cameraInfo


    val listener = object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val currentZoomRatio  = cameraInfo.zoomState.value?.zoomRatio ?: 0F
            val delta = detector.scaleFactor
            cameraControl.setZoomRatio(currentZoomRatio * delta)
            return true
        }
    }

    val scaleGestureDetector = ScaleGestureDetector(context, listener)


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



            onUseCase(
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            )

            previewView.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)

                // Handle the click event
                if (event.action == MotionEvent.ACTION_UP) {
                    previewView.performClick()
                }

                true
            }

            previewView.setOnTouchListener { view, motionEvent ->

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(motionEvent.x, motionEvent.y)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(motionEvent.x, motionEvent.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        cameraControl.startFocusAndMetering(action)
                        true
                    }

                }
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    previewView.performClick()
                    true
                }
                false
            }
            previewView
        }
    )
}