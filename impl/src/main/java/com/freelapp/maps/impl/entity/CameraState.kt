package com.freelapp.maps.impl.entity

import com.google.android.libraries.maps.model.CameraPosition

sealed class CameraState(open val position: CameraPosition) {
    data class Moving(override val position: CameraPosition) : CameraState(position)
    data class Idle(override val position: CameraPosition) : CameraState(position)
}
