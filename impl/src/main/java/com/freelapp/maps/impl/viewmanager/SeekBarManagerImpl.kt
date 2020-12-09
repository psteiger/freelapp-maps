package com.freelapp.maps.impl.viewmanager

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.domain.usersearchradius.SetUserSearchRadiusUseCase
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.maps.components.SeekBarOwner
import com.freelapp.maps.domain.SeekBarManager
import com.freelapp.maps.impl.R
import com.freelapp.maps.impl.entity.SeekBarProgress
import com.freelapp.maps.impl.ktx.changes
import com.freelapp.maps.impl.ktx.rounded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SeekBarManagerImpl @Inject constructor(
    lifecycleOwner: LifecycleOwner,
    private val seekBarOwner: SeekBarOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val setUserSearchRadiusUseCase: SetUserSearchRadiusUseCase
) : SeekBarManager,
    DefaultLifecycleObserver {

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
        seekBarOwner
            .getSeekBar()
            .changes()
            .conflate()
            .onEach {
                seekBarOwner.getSeekBarHint().isVisible = it is SeekBarProgress.Change
                if (it is SeekBarProgress.Change) {
                    seekBarOwner.getSeekBarHint().text =
                        it.progress.rounded().toLocalizedString(it.sb.context)
                } else if (it is SeekBarProgress.Stop) {
                    setUserSearchRadiusUseCase(it.sb.progress.rounded())
                }
            }
            .observeIn(owner)

    private fun Int.toLocalizedString(context: Context) =
        if (Locale.getDefault().country == "US") {
            val miles = context.getString(R.string.miles)
            "${(this * 0.6213712).toInt()} $miles"
        } else {
            val km = context.getString(R.string.km)
            "$this $km"
        }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
}