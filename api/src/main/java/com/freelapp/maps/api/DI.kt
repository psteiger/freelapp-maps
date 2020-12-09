package com.freelapp.maps.api

import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.viewmanager.MapManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationComponentBindings {

    @Binds
    @Singleton
    abstract fun bindMapManager(impl: MapManagerImpl): MapManager
}