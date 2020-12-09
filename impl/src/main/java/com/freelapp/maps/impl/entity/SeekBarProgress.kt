package com.freelapp.maps.impl.entity

import android.widget.SeekBar

sealed class SeekBarProgress(open val sb: SeekBar) {
    data class Start(override val sb: SeekBar) : SeekBarProgress(sb)
    data class Change(override val sb: SeekBar, val progress: Int, val fromUser: Boolean) : SeekBarProgress(sb)
    data class Stop(override val sb: SeekBar) : SeekBarProgress(sb)
}
