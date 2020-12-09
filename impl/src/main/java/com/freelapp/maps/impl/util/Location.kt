package com.freelapp.maps.impl.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.freelapp.maps.impl.R
import com.google.android.libraries.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

internal fun LatLng.toLocation() = Location("fromLatLng").apply {
    latitude = this@toLocation.latitude
    longitude = this@toLocation.longitude
}

internal fun Location.toLatLng() = LatLng(latitude, longitude)

internal suspend fun LatLng.getName(context: Context): String? = withContext(Dispatchers.IO) {
    val addresses = getAddresses(context)
    if (addresses?.size ?: 0 > 0) {
        addresses!![0].locality
    } else {
        null
    }
}

internal suspend fun Location.getName(context: Context): String? = toLatLng().getName(context)

@Suppress("BlockingMethodInNonBlockingContext")
internal suspend fun LatLng.getAddresses(context: Context): List<Address>? =
    withContext(Dispatchers.IO) {
        try {
            Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
        } catch (_: IOException) {
            null
        }
    }

internal typealias Distance = Int

fun Distance.toLocalizedString(context: Context) =
    if (Locale.getDefault().country == "US") {
        val miles = context.getString(R.string.miles)
        "${(this * 0.6213712).toInt()} $miles"
    } else {
        val km = context.getString(R.string.km)
        "$this $km"
    }

fun Pair<Double, Double>.toLatLng() = LatLng(first, second)

fun Iterable<Pair<Double, Double>>.toLatLng() = map { it.toLatLng() }