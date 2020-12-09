package com.freelapp.maps.domain.entity

sealed class SeekBarProgress(open val progress: Int) {
    data class Start(override val progress: Int) : SeekBarProgress(progress)
    data class Change(override val progress: Int, val fromUser: Boolean) : SeekBarProgress(progress)
    data class Stop(override val progress: Int) : SeekBarProgress(progress)
}
