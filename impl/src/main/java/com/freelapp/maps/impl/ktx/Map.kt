package com.freelapp.maps.impl.ktx

import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.LocationSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun GoogleMap.locationListeners() =
    callbackFlow<LocationSource.OnLocationChangedListener?> {
        setLocationSource(object : LocationSource {
            override fun activate(listener: LocationSource.OnLocationChangedListener) {
                runCatching { offer(listener) }
            }
            override fun deactivate() {
                runCatching { offer(null) }
            }
        })
        awaitClose { setLocationSource(null) }
    }
