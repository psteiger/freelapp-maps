package com.freelapp.maps.impl.ktx

import android.util.Log
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
internal fun AutocompleteSupportFragment.selectedPlaces() =
    callbackFlow<Place> {
        val listener = object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                runCatching { offer(place) }
            }

            override fun onError(error: Status) {
                Log.e("MapManagerImpl", "on error: $error")
            }
        }
        setOnPlaceSelectedListener(listener)
        awaitClose()
    }