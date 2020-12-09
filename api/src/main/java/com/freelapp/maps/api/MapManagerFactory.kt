package com.freelapp.maps.api

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.common.domain.usersearchmode.SetUserSearchModeUseCase
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.components.snacker.domain.Snacker
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.viewmanager.MapManagerImpl

object MapManagerFactory {
    fun create(
        lifecycleOwner: LifecycleOwner,
        mapFragmentOwner: MapFragmentOwner,
        getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
        getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase,
        setUserSearchModeUseCase: SetUserSearchModeUseCase,
        snacker: Snacker,
        locationSource: LocationSource,
        context: Context
    ): MapManager = MapManagerImpl(
        lifecycleOwner,
        mapFragmentOwner,
        getUserSearchRadiusUseCase,
        getGlobalUsersPositionsUseCase,
        setUserSearchModeUseCase,
        snacker,
        locationSource,
        context
    )
}