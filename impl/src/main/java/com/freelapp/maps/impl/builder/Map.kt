package com.freelapp.maps.impl.builder

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.maps.impl.entity.CameraState
import com.freelapp.maps.impl.ktx.locationListeners
import com.freelapp.maps.impl.ktx.toLatLng
import com.google.android.libraries.maps.CameraUpdate
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.ln

class MyGoogleMap(private val map: GoogleMap) {

    val cameraState = MutableStateFlow<CameraState>(CameraState.Idle(map.cameraPosition))

    fun makeCircleMap(
        owner: LifecycleOwner,
        getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase
    ): MyGoogleMap = apply {
        val circleState = MutableStateFlow<Circle>(
            map.addCircle(
                CircleOptions()
                    .center(map.cameraPosition.target)
                    .radius((getUserSearchRadiusUseCase().value * 1000).toDouble())
                    .fillColor(0x100000FF)
                    .strokeWidth(0f)
            )
        )

        fun adjustZoomLevel(searchRadius: Int) {
            val cu = newLatLngZoom(map.cameraPosition.target, searchRadius.asZoomLevel())
            map.animateCamera(cu)
        }

        combine(getUserSearchRadiusUseCase(), cameraState, circleState) { searchRadius, state, circle ->
            circle.apply {
                center = state.position.target
                radius = (searchRadius * 1000).toDouble()
            }
            if
            adjustZoomLevel(searchRadius)
        }.observeIn(owner)
    }

    @ExperimentalCoroutinesApi
    fun makeLocationAware(
        owner: LifecycleOwner,
        realLocation: Flow<Location>,
        onMyLocationButtonClickListener: GoogleMap.OnMyLocationButtonClickListener? = null
    ): MyGoogleMap =
        apply {
            map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
            owner.lifecycleScope.launchWhenStarted {
                val location = realLocation.first().toLatLng()
                try {
                    map.isMyLocationEnabled = true
                    map.uiSettings?.isMyLocationButtonEnabled = true
                } catch (_: SecurityException) {
                }
                map.moveCamera(CameraUpdateFactory.newLatLng(location))
            }
            realLocation
                .combine(map.locationListeners()) { location, listener ->
                    listener?.onLocationChanged(location)
                }
                .observeIn(owner)
        }

    fun makeHeatMap(
        owner: LifecycleOwner,
        getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase
    ): MyGoogleMap =
        apply {
            var provider: HeatmapTileProvider? = null
            getGlobalUsersPositionsUseCase()
                .filterNot { it.isEmpty() }
                .onEach {
                    if (provider == null) {
                        provider = HeatmapTileProvider.Builder()
                            .data(it.values.toLatLng())
                            .build()
                        val options = TileOverlayOptions().tileProvider(provider)
                        map.addTileOverlay(options)
                    } else {
                        provider!!.setData(it.values.toLatLng())
                    }
                }
                .observeIn(owner)
        }

    fun moveCamera(cu: CameraUpdate) {
        map.moveCamera(cu)
    }

    init {
        map.setOnCameraMoveListener { cameraState.value = CameraState.Moving(map.cameraPosition) }
        map.setOnCameraIdleListener { cameraState.value = CameraState.Idle(map.cameraPosition) }
    }
}

internal suspend fun SupportMapFragment.getMap(): MyGoogleMap =
    suspendCancellableCoroutine {
        getMapAsync { map -> it.resume(MyGoogleMap(map)) }
    }

internal fun Int.asZoomLevel(): Float =
    (15.5 - ln(((toInt() * 1000) / 500).toDouble()) / ln(2.0)).toFloat()