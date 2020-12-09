package com.freelapp.maps.impl.viewmanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.common.domain.usersearchmode.SetUserSearchModeUseCase
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.entity.SearchMode
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.builder.*
import com.freelapp.maps.impl.util.*
import com.google.android.gms.common.api.Status
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.hypot

class MapManagerImpl(
    lifecycleOwner: LifecycleOwner,
    mapFragmentOwner: MapFragmentOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase,
    private val setUserSearchModeUseCase: SetUserSearchModeUseCase,
    private val locationSource: LocationSource
) : DefaultLifecycleObserver,
    MapManager {

    private val mapLayout by lazy { mapFragmentOwner.getMapLayout() }
    private val mapContainer by lazy { mapLayout.mapContainer }
    private val centerButton by lazy { mapLayout.centerButton }
    private val worldwideButton by lazy { mapLayout.worldwideViewButton }
    private val closeMapButton by lazy { mapLayout.closeMapButton }
    private val showMapButton by lazy { mapFragmentOwner.getShowMapButton() }
    private val mapFragment by lazy { mapFragmentOwner.getMapFragment() }
    private val placesFragment by lazy { mapFragmentOwner.getPlaceAutocompleteFragment() }

    override fun onCreate(owner: LifecycleOwner) {
        placesFragment.view?.apply {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
        worldwideButton.setOnClickListener {
            it.performHapticFeedback()
            setUserSearchModeUseCase(SearchMode.Worldwide)
            hideMap()
        }
        showMapButton.setOnClickListener {
            showMap()
        }
        closeMapButton.setOnClickListener {
            hideMap()
        }
        owner.lifecycleScope.launch {
            mapFragment
                .getMap { myMap ->
                    centerButton.setOnClickListener {
                        it.performHapticFeedback()
                        val location = myMap.map.cameraPosition.target.toPair()
                        setUserSearchModeUseCase(SearchMode.Nearby.Custom(location))
                        hideMap()
                    }
                    myMap.addOnCameraMoveListener { centerButton.isGone = true }
                    myMap.addOnCameraIdleListener { centerButton.isVisible = true }
                    placesFragment
                        .setPlaceFields(listOf(Place.Field.LAT_LNG))
                        .setOnPlaceSelectedListener(object : PlaceSelectionListener {
                            override fun onPlaceSelected(place: Place) {
                                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                    place.latLng,
                                    getUserSearchRadiusUseCase().value.asZoomLevel()
                                )
                                myMap.map.moveCamera(cameraUpdate)
                            }

                            override fun onError(error: Status) {
                                Log.e("MapManagerImpl", "on error: $error")
                            }
                        })
                }
                .makeCircleMap(owner, getUserSearchRadiusUseCase)
                .makeLocationAware(owner, locationSource.realLocation.filterNotNull())
                .makeHeatMap(owner, getGlobalUsersPositionsUseCase)
        }
    }

    override fun showMap() {
        mapContainer.isVisible = true
        createAnimator(AnimationType.SHOW).start()
    }

    override fun hideMap() {
        createAnimator(AnimationType.HIDE)
            .apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mapContainer.isInvisible = true
                    }
                })
            }
            .start()
    }

    override fun mapIsShowing() = mapContainer.visibility == View.VISIBLE

    private fun createAnimator(type: AnimationType): Animator {
        // get the center for the clipping circle
        val pos = IntArray(2)
        showMapButton.getLocationInWindow(pos)
        val cx = pos[0] + showMapButton.width / 2
        val cy = pos[1]
        // get the final radius for the clipping circle
        val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        return ViewAnimationUtils.createCircularReveal(
            mapContainer,
            cx,
            cy,
            if (type == AnimationType.SHOW) 0f else radius,
            if (type == AnimationType.SHOW) radius else 0f
        )
    }

    enum class AnimationType { HIDE, SHOW }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
}