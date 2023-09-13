package com.bloombook.backend.Camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.bloombook.screens.journal.entries.EntriesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class CameraViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _isFlashOn: MutableStateFlow<Boolean> = savedStateHandle.get<Boolean>("isFlashOn")?.let {
        MutableStateFlow(it)
    } ?: MutableStateFlow(false)


    private val _zoomScale: MutableStateFlow<Float> = savedStateHandle.get<Float>("zoomScale")?.let {
        MutableStateFlow(it)
    } ?: MutableStateFlow(0F)

    private val _cameraUiState = MutableStateFlow(CameraState())
    val cameraUiState: StateFlow<CameraState> = _cameraUiState.asStateFlow()

    fun setFlash(state: Boolean) {
        savedStateHandle["isFlashOn"] = state
        _isFlashOn.value = state
    }

    fun setZoom(scale: Float) {
        savedStateHandle["zoomScale"] = scale
        _zoomScale.value = scale

    }

    fun updateCameraConfigs(camera: Camera){
        _cameraUiState.update { currState ->
            currState.copy(
                camera = camera,
                cameraControl = camera.cameraControl,
                cameraInfo = camera.cameraInfo
            )
        }
    }


    val isFlashOn: StateFlow<Boolean> = _isFlashOn
    val zoomScale: StateFlow<Float> = _zoomScale

    /*
    val isFlashOn: Boolean
        get() = savedStateHandle["isFlashOn"] ?: false

     */
}

data class CameraState (
    var camera: Camera? = null,
    var cameraControl: CameraControl? = null,
    var cameraInfo: CameraInfo? = null
)