package com.akshay.weatherapp.templates

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.CarColor
import androidx.car.app.model.Distance
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.RoutingInfo
import com.akshay.weatherapp.ui.RoutingMapAction


class NavigationTemplateEx(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {

        return NavigationTemplate.Builder()
            .setNavigationInfo(
                RoutingInfo.Builder()
                    .setCurrentStep(
                        RoutingMapAction.getCurrentStep(carContext),
                        Distance.create(200.0, Distance.UNIT_METERS)
                    )
                    .setNextStep(RoutingMapAction.getNextStep(carContext))
                    .build()
            )
            .setDestinationTravelEstimate(RoutingMapAction.getTravelEstimate(carContext))
            .setActionStrip(RoutingMapAction.getActionStrip(carContext, this::finish))
            .setMapActionStrip(RoutingMapAction.getMapActionStrip(carContext))
            .setBackgroundColor(CarColor.PRIMARY)
            .build()
    }
}