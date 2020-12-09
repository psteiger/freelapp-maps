package com.freelapp.maps.impl.util

import android.location.Location
import com.google.android.libraries.maps.model.LatLng
import java.util.*

internal fun LatLng.toPair() = Pair(latitude, longitude)

internal fun LatLng.toLocation() = Location("fromLatLng").apply {
    latitude = this@toLocation.latitude
    longitude = this@toLocation.longitude
}

internal fun Location.toLatLng() = LatLng(latitude, longitude)

internal fun Pair<Double, Double>.toLatLng() = LatLng(first, second)

internal fun Iterable<Pair<Double, Double>>.toLatLng() = map { it.toLatLng() }