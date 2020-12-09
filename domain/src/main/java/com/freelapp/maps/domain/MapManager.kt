package com.freelapp.maps.domain

interface MapManager {
    fun mapIsShowing(): Boolean
    fun hideMap()
    fun showMap()
}