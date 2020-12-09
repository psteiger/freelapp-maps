package com.freelapp.maps.api

import androidx.lifecycle.LifecycleOwner
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.common.domain.usersearchmode.SetUserSearchModeUseCase
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.libs.locationfetcher.LocationFetcher
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.viewmanager.MapManagerImpl

object MapManagerFactory {
    fun create(
        lifecycleOwner: LifecycleOwner,
        mapFragmentOwner: MapFragmentOwner,
        getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
        getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase,
        setUserSearchModeUseCase: SetUserSearchModeUseCase,
        locationFetcher: LocationFetcher,
        locationSource: LocationSource,
        seekBarManager: SeekBarManager,
    ): MapManager =
        MapManagerImpl(
            lifecycleOwner,
            mapFragmentOwner,
            getUserSearchRadiusUseCase,
            getGlobalUsersPositionsUseCase,
            setUserSearchModeUseCase,
            locationFetcher,
            locationSource,
            seekBarManager
        )
}