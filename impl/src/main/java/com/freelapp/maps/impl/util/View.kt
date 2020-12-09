package com.freelapp.maps.impl.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

internal fun View.performHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}