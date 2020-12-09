package com.freelapp.maps.impl.viewmanager

import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.domain.usersearchradius.SetUserSearchRadiusUseCase
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.util.toLocalizedString
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import javax.inject.Inject

class SeekBarManagerImpl @Inject constructor(
    lifecycleOwner: LifecycleOwner,
    private val seekBarOwner: SeekBarOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val setUserSearchRadiusUseCase: SetUserSearchRadiusUseCase
) : SeekBarManager,
    DefaultLifecycleObserver {

    data class SeekBarProgressChange(val sb: SeekBar, val progress: Int)

    @ObsoleteCoroutinesApi
    val seekBarActor =
        lifecycleOwner
            .lifecycleScope
            .actor<SeekBarProgressChange>(capacity = Channel.CONFLATED) {
                fun round(num: Int) = if (num < 10) 10 else num / 10 * 10

                for ((sb, progress) in channel) {
                    setUserSearchRadiusUseCase(round(progress))
                    seekBarOwner.getSeekBarHint().text =
                        round(progress).toLocalizedString(sb.context)
                }
            }

    @ObsoleteCoroutinesApi
    override fun onCreate(owner: LifecycleOwner) {
        val seekBarHintContainer = seekBarOwner.getSeekBarHintContainer()
        seekBarOwner.getSeekBar().run {
            max = 2000
            progress = getUserSearchRadiusUseCase().value
            seekBarOwner.getSeekBarHintContainer().visibility = View.INVISIBLE
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(sb: SeekBar) {
                    seekBarHintContainer.visibility = View.INVISIBLE
                }

                override fun onStartTrackingTouch(sb: SeekBar) {
                    seekBarHintContainer.visibility = View.VISIBLE
                }

                override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                    runCatching {
                        seekBarActor.offer(SeekBarProgressChange(sb, progress))
                    }
                }
            })
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
}