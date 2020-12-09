package com.freelapp.maps.domain

import com.freelapp.maps.domain.entity.SeekBarProgress
import kotlinx.coroutines.flow.StateFlow

interface SeekBarManager {
    val seekBarChanges: StateFlow<SeekBarProgress>
}