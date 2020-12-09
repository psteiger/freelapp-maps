package com.freelapp.maps.api

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.domain.usersearchradius.SetUserSearchRadiusUseCase
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.viewmanager.SeekBarManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi

object SeekBarManagerFactory {
    @ExperimentalCoroutinesApi
    fun create(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        seekBarOwner: SeekBarOwner,
        getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
        setUserSearchRadiusUseCase: SetUserSearchRadiusUseCase
    ): SeekBarManager =
        SeekBarManagerImpl(
            lifecycleOwner,
            context,
            seekBarOwner,
            getUserSearchRadiusUseCase,
            setUserSearchRadiusUseCase
        )
}