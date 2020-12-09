package com.freelapp.maps.impl.builder

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.flowlifecycleobserver.observe
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.maps.domain.MapInteractor
import com.freelapp.maps.impl.util.toLatLng
import com.freelapp.maps.impl.util.toLocation
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.CameraUpdateFactory.newLatLngZoom
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.LocationSource
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.Circle
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.ln

class MyGoogleMap(val map: GoogleMap) {
    private val onCameraIdleListeners = mutableListOf<(GoogleMap) -> Unit>()
    private val onCameraMoveListeners = mutableListOf<(GoogleMap) -> Unit>()

    fun addOnCameraIdleListener(listener: (GoogleMap) -> Unit) {
        onCameraIdleListeners.add(listener)
        map.setOnCameraIdleListener {
            onCameraIdleListeners.forEach { it(map) }
        }
    }
    fun addOnCameraMoveListener(listener: (GoogleMap) -> Unit) {
        onCameraMoveListeners.add(listener)
        map.setOnCameraMoveListener {
            onCameraMoveListeners.forEach { it(map) }
        }
    }
}

suspend fun SupportMapFragment.getMap(fn: (MyGoogleMap) -> Unit): MyGoogleMap =
    withContext(Dispatchers.Main) {
        suspendCoroutine<MyGoogleMap> {
            getMapAsync { map -> it.resume(MyGoogleMap(map)) }
        }.apply {
            fn(this)
        }
    }

fun MyGoogleMap.makeCircleMap(
    owner: LifecycleOwner,
    searchRadiusFlow: StateFlow<Int>
): MyGoogleMap = apply {
    var circle: Circle? = null

    fun adjustZoomLevel(searchRadius: Int) {
        val cu = newLatLngZoom(map.cameraPosition.target, searchRadius.asZoomLevel())
        map.moveCamera(cu)
    }

    fun drawCircle(newCenter: LatLng, searchRadiusInKms: Int) {
        if (circle == null) {
            val circleOptions = CircleOptions()
                .center(newCenter)
                .radius((searchRadiusInKms * 1000).toDouble())
                .fillColor(0x100000FF)
                .strokeWidth(0f)
            circle = map.addCircle(circleOptions)
        } else {
            circle?.run {
                center = newCenter
                radius = (searchRadiusInKms * 1000).toDouble()
            }
        }
    }

    fun onCameraMove() {
        val center = map.cameraPosition.target.toLocation().toLatLng()
        val radius = searchRadiusFlow.value
        drawCircle(center, radius)
    }

    addOnCameraMoveListener { onCameraMove() }
    addOnCameraIdleListener { onCameraMove() }

    searchRadiusFlow.observe(owner) { radius ->
        val center = map.cameraPosition.target.toLocation().toLatLng()
        drawCircle(center, radius)
        adjustZoomLevel(radius)
    }
}

fun MyGoogleMap.makeLocationAware(
    owner: LifecycleOwner,
    realLocation: Flow<Location>,
    onMyLocationButtonClickListener: GoogleMap.OnMyLocationButtonClickListener? = null
): MyGoogleMap = apply {
    var googleMapLocationListener: LocationSource.OnLocationChangedListener? = null
    map.setLocationSource(object : LocationSource {
        override fun activate(listener: LocationSource.OnLocationChangedListener?) {
            googleMapLocationListener = listener
        }

        override fun deactivate() {
            googleMapLocationListener = null
        }
    })
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
    realLocation.observe(owner) {
        googleMapLocationListener?.onLocationChanged(it)
    }
}

fun MyGoogleMap.makeHeatMap(
    lifecycleOwner: LifecycleOwner,
    mapInteractor: MapInteractor
): MyGoogleMap = apply {
    var provider: HeatmapTileProvider? = null
    mapInteractor
        .globalUsersPositions
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
        .observeIn(lifecycleOwner)
}

fun Int.asZoomLevel(): Float =
    (15.5 - ln(((toInt() * 1000) / 500).toDouble()) / ln(2.0)).toFloat()