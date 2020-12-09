package com.freelapp.maps.impl.ktx

import android.widget.SeekBar
import com.freelapp.maps.domain.entity.SeekBarProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
internal fun SeekBar.changes() =
    callbackFlow<SeekBarProgress> {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(sb: SeekBar) {
                runCatching { offer(SeekBarProgress.Stop(sb.progress)) }
            }
            override fun onStartTrackingTouch(sb: SeekBar) {
                runCatching { offer(SeekBarProgress.Start(sb.progress)) }
            }
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                runCatching { offer(SeekBarProgress.Change(progress, fromUser)) }
            }
        }
        setOnSeekBarChangeListener(listener)
        awaitClose()
    }