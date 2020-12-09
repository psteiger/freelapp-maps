package com.freelapp.maps.api

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapInteractor
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.viewmanager.MapManagerImpl

object MapManagerFactory {
    fun create(
        mapFragmentOwner: MapFragmentOwner,
        lifecycleOwner: LifecycleOwner,
        mapInteractor: MapInteractor,
        locationSource: LocationSource,
        context: Context
    ): MapManager = MapManagerImpl(
        mapFragmentOwner,
        lifecycleOwner,
        mapInteractor,
        locationSource,
        context
    )
}