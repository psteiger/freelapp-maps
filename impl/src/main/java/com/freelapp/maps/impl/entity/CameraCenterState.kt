package com.freelapp.maps.impl.entity

import com.google.android.libraries.maps.model.LatLng

sealed class CameraCenterState(open val position: LatLng) {
    data class Moving(override val position: LatLng) : CameraCenterState(position)
    data class Idle(override val position: LatLng) : CameraCenterState(position)
}
