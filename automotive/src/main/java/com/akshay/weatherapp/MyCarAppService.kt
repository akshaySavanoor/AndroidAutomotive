package com.akshay.weatherapp

import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.akshay.weatherapp.ui.RequestPermissionScreen

/**
 * The CarAppService validates that a host connection can be trusted and henceforth be used to provide session instances.
 * [Refer](https://medium.com/tribalscale/android-automotive-os-part-2-b7fe6b781be2#:~:text=CarAppService%2C%20Session)
 */
class MyCarAppService : CarAppService() {
    override fun onCreateSession(): Session {
        return WeatherAppSession()
    }

    /**
     * Validates that the calling package is authorized to connect to a CarAppService.
     * [reference](https://developer.android.com/reference/androidx/car/app/validation/HostValidator#:~:text=ALLOW_ALL_HOSTS_VALIDATOR-,A%20host%20validator%20that%20doesn%27t%20block%20any%20hosts.,-static%C2%A0final%20String)
     * A host validator that doesn't block any hosts [Reference](https://developer.android.com/reference/androidx/car/app/validation/HostValidator#:~:text=ALLOW_ALL_HOSTS_VALIDATOR-,A%20host%20validator%20that%20doesn%27t%20block%20any%20hosts.,-static%C2%A0final%20String)
     */
    override fun createHostValidator(): HostValidator {
        return if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {


            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }
    }
}

/**
 * A session serves as the entry point for an app to display the initial screen on the app launch. A session has a lifecycle and can be considered similar to an activity in mobile Android development.
 */
class WeatherAppSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return RequestPermissionScreen(carContext)
    }
}

