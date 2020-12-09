package com.freelapp.maps.api

import androidx.lifecycle.LifecycleOwner
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.domain.usersearchradius.SetUserSearchRadiusUseCase
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.viewmanager.SeekBarManagerImpl

object SeekBarManagerFactory {
    fun create(
        lifecycleOwner: LifecycleOwner,
        seekBarOwner: SeekBarOwner,
        getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
        setUserSearchRadiusUseCase: SetUserSearchRadiusUseCase
    ): SeekBarManager = SeekBarManagerImpl(
        lifecycleOwner,
        seekBarOwner,
        getUserSearchRadiusUseCase,
        setUserSearchRadiusUseCase
    )
}