package com.akshay.weatherapp.templates

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.CarColor
import androidx.car.app.model.Distance
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.RoutingInfo
import com.akshay.weatherapp.misc.RoutingMapAction

/**
 * - Displayed when the car is parked, typically for showing detailed messages.
 * - Often used for presenting privacy policies or terms of service.
 * - Crucial for obtaining user agreement before accessing app features, particularly in Android Auto.
 * - Hidden during driving to prioritize safety and prevent distractions.
 * - By default doesn't expose map use surface callback
 */
class NavigationTemplateEx(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val navigationInfo = RoutingInfo.Builder().apply {
            setCurrentStep(
                RoutingMapAction.getCurrentStep(carContext),
                Distance.create(200.0, Distance.UNIT_METERS)
            )
            setNextStep(RoutingMapAction.getNextStep(carContext))
        }

        return NavigationTemplate.Builder()
            .run {
                setNavigationInfo(navigationInfo.build())
                setDestinationTravelEstimate(RoutingMapAction.getTravelEstimate(carContext))
                setActionStrip(
                    RoutingMapAction.getActionStrip(
                        carContext,
                        this@NavigationTemplateEx::finish
                    )
                )
                setMapActionStrip(RoutingMapAction.getMapActionStrip(carContext))
                setBackgroundColor(CarColor.PRIMARY)
                build()
            }
    }
}