package com.freelapp.maps.impl.viewmanager

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.domain.usersearchradius.SetUserSearchRadiusUseCase
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.R
import com.freelapp.maps.domain.entity.SeekBarProgress
import com.freelapp.maps.impl.ktx.changes
import com.freelapp.maps.impl.ktx.rounded
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeekBarManagerImpl @Inject constructor(
    owner: LifecycleOwner,
    @ApplicationContext private val context: Context,
    private val seekBarOwner: SeekBarOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val setUserSearchRadiusUseCase: SetUserSearchRadiusUseCase
) : SeekBarManager,
    DefaultLifecycleObserver {

    override val seekBarChanges: StateFlow<SeekBarProgress> by lazy {
        val seekBar = seekBarOwner.getSeekBar()
        seekBar
            .changes()
            .conflate()
            .stateIn(
                owner.lifecycleScope,
                SharingStarted.Eagerly,
                SeekBarProgress.Stop(seekBar.progress)
            )
    }

    @ObsoleteCoroutinesApi
    override fun onCreate(owner: LifecycleOwner) {
        seekBarOwner.getSeekBarHintContainer().visibility = View.INVISIBLE
        seekBarOwner.getSeekBar().run {
            max = 2000
            progress = getUserSearchRadiusUseCase().value
        }
        observeSeekBarChanges(owner)
    }

    private fun observeSeekBarChanges(owner: LifecycleOwner) =
        seekBarChanges
            .onEach { progress ->
                seekBarOwner.hideShowHint(progress is SeekBarProgress.Change)
                if (progress is SeekBarProgress.Change) {
                    setHintText(progress)
                } else if (progress is SeekBarProgress.Stop) {
                    setUserSearchRadiusUseCase(progress.progress.rounded())
                }
            }
            .observeIn(owner)

    private fun SeekBarOwner.hideShowHint(show: Boolean) {
        listOf(getSeekBarHint(), getSeekBarHintContainer())
            .forEach { it.isVisible = show }
    }

    private fun setHintText(progress: SeekBarProgress.Change) {
        seekBarOwner.getSeekBarHint().text =
            progress.progress.rounded().toLocalizedString()
    }

    private fun Int.toLocalizedString() =
        if (Locale.getDefault().country == "US") {
            val miles = context.getString(R.string.miles)
            "${(this * 0.6213712).toInt()} $miles"
        } else {
            val km = context.getString(R.string.km)
            "$this $km"
        }

    init { owner.lifecycle.addObserver(this) }
}