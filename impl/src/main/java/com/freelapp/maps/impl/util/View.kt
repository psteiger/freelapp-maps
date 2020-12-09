package com.freelapp.maps.impl.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope

internal fun View.performHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

internal fun View.onClick(owner: LifecycleOwner, fn: suspend (View) -> Unit) {
    setOnClickListener { owner.lifecycleScope.launchWhenResumed { fn(this@onClick) }  }
}