package com.freelapp.maps.components

import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView

interface SeekBarOwner {
    fun getSeekBar(): SeekBar
    fun getSeekBarHintContainer(): CardView
    fun getSeekBarHint(): TextView
}
