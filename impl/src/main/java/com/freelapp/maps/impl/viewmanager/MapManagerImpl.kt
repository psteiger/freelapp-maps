package com.freelapp.maps.impl.viewmanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.freelapp.common.domain.getglobaluserspositions.GetGlobalUsersPositionsUseCase
import com.freelapp.common.domain.usersearchmode.SetUserSearchModeUseCase
import com.freelapp.common.domain.usersearchradius.GetUserSearchRadiusUseCase
import com.freelapp.common.entity.SearchMode
import com.freelapp.flowlifecycleobserver.observeIn
import com.freelapp.libs.locationfetcher.LocationSource
import com.freelapp.maps.components.MapFragmentOwner
import com.freelapp.maps.domain.MapManager
import com.freelapp.maps.impl.builder.MyGoogleMap
import com.freelapp.maps.impl.builder.asZoomLevel
import com.freelapp.maps.impl.builder.getMap
import com.freelapp.maps.impl.entity.CameraState
import com.freelapp.maps.impl.ktx.performHapticFeedback
import com.freelapp.maps.impl.ktx.selectedPlaces
import com.freelapp.maps.impl.ktx.toPair
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.hypot

class MapManagerImpl @Inject constructor(
    lifecycleOwner: LifecycleOwner,
    private val mapFragmentOwner: MapFragmentOwner,
    private val getUserSearchRadiusUseCase: GetUserSearchRadiusUseCase,
    private val getGlobalUsersPositionsUseCase: GetGlobalUsersPositionsUseCase,
    private val setUserSearchModeUseCase: SetUserSearchModeUseCase,
    private val locationSource: LocationSource
) : DefaultLifecycleObserver,
    MapManager {

    private val mapLayout get() = mapFragmentOwner.getMapLayout()
    private val mapContainer get() = mapLayout.mapContainer
    private val centerButton get() = mapLayout.centerButton
    private val worldwideButton get() = mapLayout.worldwideViewButton
    private val closeMapButton get() = mapLayout.closeMapButton
    private val showMapButton get() = mapFragmentOwner.getShowMapButton()
    private val mapFragment get() = mapFragmentOwner.getMapFragment()
    private val placesFragment get() = mapFragmentOwner.getPlaceAutocompleteFragment()

    @ExperimentalCoroutinesApi
    override fun onCreate(owner: LifecycleOwner) {
        placesFragment.view?.apply {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
        owner.lifecycleScope.launch {
            val map = createMap(owner)
            map.setButtonClickListeners()
            map.cameraState
                .onEach { centerButton.isVisible = it is CameraState.Idle }
                .observeIn(owner)
            placesFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
            placesFragment
                .selectedPlaces()
                .onEach {
                    val zoomLevel = getUserSearchRadiusUseCase().value.asZoomLevel()
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it.latLng, zoomLevel)
                    map.moveCamera(cameraUpdate)
                }
                .observeIn(owner)
        }
    }

    private fun MyGoogleMap.setButtonClickListeners() {
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
        centerButton.setOnClickListener {
            it.performHapticFeedback()
            val location = cameraState.value.position.target.toPair()
            setUserSearchModeUseCase(SearchMode.Nearby.Custom(location))
            hideMap()
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun createMap(owner: LifecycleOwner) =
        mapFragment
            .getMap()
            .makeCircleMap(owner, getUserSearchRadiusUseCase)
            .makeLocationAware(owner, locationSource.realLocation.filterNotNull())
            .makeHeatMap(owner, getGlobalUsersPositionsUseCase)

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