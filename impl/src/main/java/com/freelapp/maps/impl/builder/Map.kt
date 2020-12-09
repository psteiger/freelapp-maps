package com.freelapp.maps.impl.builder

import android.location.Location
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.maps.domain.entity.SeekBarProgress
import com.freelapp.maps.impl.entity.CameraCenterState
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.ln

class MyGoogleMap(
    fragment: SupportMapFragment,
    private val map: GoogleMap
) {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle(map.cameraPosition))
    private lateinit var circle: MutableStateFlow<Circle>

    @ExperimentalCoroutinesApi
    val cameraCenter: StateFlow<CameraCenterState> =
        _cameraState
            .mapLatest {
                val newCenter = it.position.target
                when (it) {
                    is CameraState.Moving -> CameraCenterState.Moving(newCenter)
                    is CameraState.Idle -> CameraCenterState.Idle(newCenter)
                }
            }
            .stateIn(
                fragment.lifecycleScope,
                SharingStarted.Eagerly,
                CameraCenterState.Idle(map.cameraPosition.target)
            )

    @ExperimentalCoroutinesApi
    fun makeCircleMap(
        owner: LifecycleOwner,
        seekBarChanges: StateFlow<SeekBarProgress>
    ): MyGoogleMap = apply {
        circle = MutableStateFlow(
            map.addCircle(
                CircleOptions()
                    .center(map.cameraPosition.target)
                    .radius((seekBarChanges.value.progress * 1000).toDouble())
                    .fillColor(0x100000FF)
                    .strokeWidth(0f)
            )
        )

        fun adjustZoomLevel(searchRadius: Int) {
            Log.d("Map", "Adjusting zoom level to searchRadius=$searchRadius")
            val cu = newLatLngZoom(map.cameraPosition.target, searchRadius.asZoomLevel())
            map.animateCamera(cu)
        }

        cameraCenter
            .onEach { circle.value.center = it.position }
            .observeIn(owner)

        seekBarChanges
            .onEach {
                val radius = it.progress
                circle.value.radius = (radius * 1000).toDouble()
                adjustZoomLevel(radius)
            }
            .observeIn(owner)
    }

    @ExperimentalCoroutinesApi
    fun makeLocationAware(
        owner: LifecycleOwner,
        realLocation: Flow<Location>,
        onMyLocationButtonClickListener: GoogleMap.OnMyLocationButtonClickListener? = null
    ): MyGoogleMap =
        apply {
            map.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
            owner.lifecycleScope.launch {
                val location = realLocation.first().toLatLng()
                try {
                    map.isMyLocationEnabled = true
                    map.uiSettings?.isMyLocationButtonEnabled = true
                } catch (_: SecurityException) {
                }
                Log.d("Map", "makeLocationAware moving camera to location=$location")
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
        map.setOnCameraMoveListener { _cameraState.value = CameraState.Moving(map.cameraPosition) }
        map.setOnCameraIdleListener { _cameraState.value = CameraState.Idle(map.cameraPosition) }
    }
}

internal suspend fun SupportMapFragment.getMap(): MyGoogleMap =
    suspendCancellableCoroutine {
        getMapAsync { map -> it.resume(MyGoogleMap(this, map)) }
    }

internal fun Int.asZoomLevel(): Float =
    (15.5 - ln(((toInt() * 1000) / 500).toDouble()) / ln(2.0)).toFloat()