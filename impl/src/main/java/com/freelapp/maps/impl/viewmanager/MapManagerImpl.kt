package com.freelapp.maps.impl.viewmanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.location.Location
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
import com.freelapp.common.entity.Mode
import com.freelapp.components.snacker.domain.Snacker
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.R
import com.freelapp.maps.impl.builder.*
import com.freelapp.maps.impl.util.*
import com.google.android.gms.common.api.Status
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.math.hypot

class MapManagerImpl(
    lifecycleOwner: LifecycleOwner,
    mapFragmentOwner: MapFragmentOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase,
    private val setUserSearchModeUseCase: SetUserSearchModeUseCase,
    private val snacker: Snacker,
    private val locationSource: LocationSource,
    private val context: Context
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
        worldwideButton.onClick(owner) {
            it.performHapticFeedback()
            setWorldMode()
//            if (mapInteractor.isSubscribed) {
//                setWorldMode()
//            } else {
//                mapInteractor.subscribe()
//            }
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
                    centerButton.onClick(owner) {
                        it.performHapticFeedback()
                        myMap.map.setCustomLocation()

//                        if (mapInteractor.isSubscribed) {
//                            myMap.map.setCustomLocation()
//                        } else {
//                            mapInteractor.subscribe()
//                        }
                    }
                    myMap.addOnCameraMoveListener { centerButton.isGone = true }
                    myMap.addOnCameraIdleListener { centerButton.isVisible = true }
                    placesFragment
                        .setPlaceFields(listOf(Place.Field.LAT_LNG))
                        .setOnPlaceSelectedListener(object : PlaceSelectionListener {
                            override fun onPlaceSelected(place: Place) {
                                myMap.map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        place.latLng,
                                        getUserSearchRadiusUseCase().value.asZoomLevel()
                                    )
                                )
                            }

                            override fun onError(error: Status) {}
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

    private fun setWorldMode() {
        setUserSearchModeUseCase(Mode.WORLD)
        snacker(worldwideButton.context.getString(R.string.worldwide))
        hideMap()
    }

    private suspend fun GoogleMap.setCustomLocation() = supervisorScope {
        setUserSearchModeUseCase(Mode.NEARBY)
        val location = cameraPosition.target.toLocation()
        launch { location.showName() }
        locationSource.setCustomLocation(location)
        locationSource.setPreferredSource(LocationSource.Source.CUSTOM)
        hideMap()
    }

    override fun mapIsShowing() = mapContainer.visibility == View.VISIBLE

    private suspend fun Location.showName() {
        getName(context)?.let { name ->
            val radius = getUserSearchRadiusUseCase().value.toLocalizedString(context)
            snacker("$name ($radius)")
        }
    }

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