package com.freelapp.maps.impl.ktx

import android.view.View
import android.widget.SeekBar
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.impl.entity.SeekBarProgressChange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
internal fun SeekBarOwner.changes() =
    callbackFlow<SeekBarProgressChange> {
        val seekBar = getSeekBar()
        val hintContainer = getSeekBarHintContainer()
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(sb: SeekBar) {
                hintContainer.visibility = View.INVISIBLE
            }

            override fun onStartTrackingTouch(sb: SeekBar) {
                hintContainer.visibility = View.VISIBLE
            }

            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                runCatching {
                    offer(SeekBarProgressChange(sb, progress, fromUser))
                }
            }
        }
        seekBar.setOnSeekBarChangeListener(listener)
        awaitClose()
    }