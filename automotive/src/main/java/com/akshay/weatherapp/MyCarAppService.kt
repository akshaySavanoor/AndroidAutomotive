package com.akshay.weatherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * The CarAppService validates that a host connection can be trusted and henceforth be used to provide session instances.
 * [Refer](https://medium.com/tribalscale/android-automotive-os-part-2-b7fe6b781be2#:~:text=CarAppService%2C%20Session)
 *
 * The platform cannot detect or enforce an app's actual adherence to the restrictions; it can only check an app's declaration in the manifest.
 * Adherence to these driver distraction guidelines is enforced during the Play Store review process.
 *
 * If app is not listed in the config.xml check their Manifest meta-data to
 * see if they have any Distraction Optimized(DO) activities.
 * For non system apps, we check if the app install source was a permittable
 * source. This prevents side-loaded apps to fake DO.  Bypass the check
 * for debug builds for development convenience.
 * [Ref](https://android.googlesource.com/platform/packages/services/Car/+/refs/heads/main/service/src/com/android/car/pm/CarPackageManagerService.java#:~:text=If%20app%20is,for%20development%20convenience.)
 */
class MyCarAppService : CarAppService() {
    override fun onCreateSession(): Session {
        return WeatherAppSession()
    }

    /**
     * Validates that the calling package is authorized to connect to a CarAppService.
     * [reference](https://developer.android.com/reference/androidx/car/app/validation/HostValidator#:~:text=ALLOW_ALL_HOSTS_VALIDATOR-,A%20host%20validator%20that%20doesn%27t%20block%20any%20hosts.,-static%C2%A0final%20String)
     * ALLOW_ALL_HOSTS_VALIDATOR host validator doesn't block any hosts [Reference](https://developer.android.com/reference/androidx/car/app/validation/HostValidator#:~:text=ALLOW_ALL_HOSTS_VALIDATOR-,A%20host%20validator%20that%20doesn%27t%20block%20any%20hosts.,-static%C2%A0final%20String)
     */
    @SuppressLint("PrivateResource")
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
        return HomeScreen(carContext)
    }
}

