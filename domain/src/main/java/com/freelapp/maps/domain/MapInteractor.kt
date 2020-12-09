package com.freelapp.maps.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MapInteractor {
    val mode: MutableStateFlow<Mode>
    var searchRadius: Int
    val searchRadiusFlow: StateFlow<Int>
    val isSubscribed: Boolean
    val globalUsersPositions: SharedFlow<Map<String, Pair<Double, Double>>>

    suspend fun subscribe(): Boolean
    fun showSnackBar(message: String)

    enum class Mode { NEARBY, WORLD }
}