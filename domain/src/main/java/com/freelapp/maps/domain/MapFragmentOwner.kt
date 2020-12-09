package com.freelapp.maps.domain

import com.freelapp.maps.domain.databinding.FragmentMapBinding
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface MapFragmentOwner {
    fun getMapFragment(): SupportMapFragment
    fun getMapLayout(): FragmentMapBinding
    fun getShowMapButton(): FloatingActionButton
    fun getPlaceAutocompleteFragment(): AutocompleteSupportFragment
}