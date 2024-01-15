package com.akshay.weatherapp.ui

import android.location.Location
import android.location.LocationListener
import android.os.Bundle


class MyLocationListener(private val locationCallback: (Location) -> Unit) : LocationListener {

    override fun onLocationChanged(location: Location) {
        locationCallback.invoke(location)
    }
    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}
