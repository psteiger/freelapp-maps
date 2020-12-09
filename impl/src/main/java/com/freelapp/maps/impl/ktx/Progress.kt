package com.freelapp.maps.impl.ktx

fun Int.rounded() = if (this < 10) 10 else this / 10 * 10