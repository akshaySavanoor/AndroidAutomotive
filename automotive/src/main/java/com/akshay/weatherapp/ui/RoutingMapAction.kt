package com.akshay.weatherapp.ui

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R

object RoutingMapAction {

    /**
     * Note: Ensure that the number of actions does not exceed 4.
     * Error: java.lang.IllegalArgumentException - Action list exceeded the maximum limit of 4 actions.
     *
     * Note: Avoid adding duplicate actions, e.g., having Action.PAN more than once.
     * Error: java.lang.IllegalArgumentException - Duplicated action types are disallowed: [type: PAN, icon: null, bkg: [type: DEFAULT, color: 0, dark: 0], isEnabled: true]
     */
    fun getMapActionStrip(carContext: CarContext): ActionStrip {
        return ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setOnClickListener {
                        CarToast.makeText(
                            carContext,
                            carContext.getString(R.string.zoomed_in_toast_msg),
                            CarToast.LENGTH_SHORT
                        ).show()
                    }
                    .setIcon(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext,
                                R.drawable.ic_zoom_in_24
                            )
                        ).build()
                    ).build()
            )
            .addAction(
                Action.Builder()
                    .setOnClickListener {
                        CarToast.makeText(
                            carContext,
                            carContext.getString(R.string.zoomed_out_toast_msg),
                            CarToast.LENGTH_SHORT
                        ).show()
                    }
                    .setIcon(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext,
                                R.drawable.ic_zoom_out_24
                            )
                        ).build()
                    ).build()
            )
            .addAction(Action.PAN)
            .build()

    }
}