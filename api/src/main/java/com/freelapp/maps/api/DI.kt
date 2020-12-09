package com.freelapp.maps.api

import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.viewmanager.MapManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class MapManagerModule {

    @Binds
    @ActivityScoped
    abstract fun bindMapManager(impl: MapManagerImpl): MapManager
}