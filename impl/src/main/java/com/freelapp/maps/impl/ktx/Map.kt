package com.freelapp.maps.impl.ktx

import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.LocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

@ExperimentalCoroutinesApi
fun GoogleMap.locationListeners(scope: CoroutineScope) =
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
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        null
    )