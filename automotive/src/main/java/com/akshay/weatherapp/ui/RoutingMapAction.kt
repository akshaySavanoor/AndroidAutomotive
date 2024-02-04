package com.akshay.weatherapp.ui

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Alert
import androidx.car.app.model.AlertCallback
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarIconSpan
import androidx.car.app.model.CarText
import androidx.car.app.model.DateTimeWithZone
import androidx.car.app.model.Distance
import androidx.car.app.model.OnClickListener
import androidx.car.app.navigation.model.Lane
import androidx.car.app.navigation.model.LaneDirection
import androidx.car.app.navigation.model.LaneDirection.SHAPE_NORMAL_RIGHT
import androidx.car.app.navigation.model.LaneDirection.SHAPE_STRAIGHT
import androidx.car.app.navigation.model.Maneuver
import androidx.car.app.navigation.model.Step
import androidx.car.app.navigation.model.TravelEstimate
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Utility.Companion.showToast
import java.util.TimeZone
import java.util.concurrent.TimeUnit

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

    private fun createAlert(carContext: CarContext): Alert {
        val yesAction = Action.Builder().apply {
            setTitle(carContext.getString(R.string.yes))
            setFlags(FLAG_PRIMARY)
            setOnClickListener {
                showToast(carContext, carContext.getString(R.string.yes_action_toast_msg))
            }
        }
            .build()
        val noAction = Action.Builder().apply {
            setTitle(carContext.getString(R.string.no))
            setOnClickListener {
                showToast(carContext, carContext.getString(R.string.no_action_toast_msg))
            }
        }
            .build()

        val alertCallBack = object : AlertCallback {
            override fun onCancel(reason: Int) {
                if (reason == AlertCallback.REASON_TIMEOUT) {
                    showToast(carContext, carContext.getString(R.string.request_timeout))
                }
            }

            override fun onDismiss() {
//                showToast(carContext, carContext.getString(R.string.cancelled))
            }
        }

        //Alerts can be used only in the navigation template
        return Alert.Builder(
            0,
            CarText.create(carContext.getString(R.string.navigation_alert_title)),
            10000
        )
            .setIcon(CarIcon.ALERT)
            .setSubtitle(CarText.create(carContext.getString(R.string.navigation_alert_subtitle)))
            .addAction(yesAction)
            .addAction(noAction)
            .setCallback(
                alertCallBack
            )
            .build()


    }

    fun getActionStrip(carContext: CarContext, onStopNavigation: OnClickListener): ActionStrip {
        val builder = ActionStrip.Builder()
        if (carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_5) {
            builder.addAction(
                Action.Builder()
                    .setOnClickListener {
                        carContext.getCarService(AppManager::class.java)
                            .showAlert(createAlert(carContext))
                    }
                    .setIcon(
                        CarIcon.Builder(
                            IconCompat.createWithResource(
                                carContext,
                                R.drawable.ic_baseline_add_alert_24
                            )
                        ).build()
                    ).build()
            )
        }
        builder.addAction(
            Action.Builder()
                .setOnClickListener {
                    CarToast.makeText(
                        carContext,
                        carContext.getString(R.string.bug_reported_toast_msg),
                        CarToast.LENGTH_SHORT
                    ).show()
                }
                .setIcon(
                    CarIcon.Builder(
                        IconCompat.createWithResource(
                            carContext,
                            R.drawable.ic_bug_report_24px
                        )
                    ).build()
                ).build()
        )
        builder.addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.stop_action_title))
                .setOnClickListener(onStopNavigation)
                .setFlags(Action.FLAG_IS_PERSISTENT) //Flag indicates that this action will not fade in/out inside an ActionStrip.
                .build()
        )
        return builder.build()
    }

    /**
     * ERROR: java.lang.IllegalStateException: Current step must have a lanes image if the lane information is set
     */
    fun getCurrentStep(carContext: CarContext): Step {
        val currentStepCue = carContext.getString(R.string.current_step_cue)
        val currentStepCueWithImage = SpannableString(currentStepCue)
        val highwaySign = CarIconSpan.create(
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_520)).build(),
            CarIconSpan.ALIGN_CENTER
        )
        currentStepCueWithImage.setSpan(highwaySign, 9, 12, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        val currentTurnIcon =
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.arrow_right_turn))
                .build()
        val currentManeuver =
            Maneuver.Builder(Maneuver.TYPE_TURN_NORMAL_RIGHT).setIcon(currentTurnIcon).build()

        val lanesImage =
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.lanes)).build()

        val straightNormal =
            Lane.Builder().addDirection(LaneDirection.create(SHAPE_STRAIGHT, false)).build()
        val rightHighlighted =
            Lane.Builder().addDirection(LaneDirection.create(SHAPE_NORMAL_RIGHT, true)).build()

        return Step.Builder(currentStepCueWithImage)
            .run {
                setManeuver(currentManeuver)
                setLanesImage(lanesImage)
                addLane(straightNormal)
                addLane(straightNormal)
                addLane(straightNormal)
                addLane(straightNormal)
                addLane(rightHighlighted)
                build()
            }
    }

    /**
     * id the ID for a TimeZone, either an abbreviation
     * such as "PST", a full name such as "Asia/Kolkata", or
     * a custom ID such as "GMT-8:00".
     */
    fun getTravelEstimate(carContext: CarContext): TravelEstimate {
        val nowUtcMillis = System.currentTimeMillis()
        val timeToDestinationMillis = TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(55)

        return TravelEstimate.Builder(
            Distance.create(112.0, Distance.UNIT_KILOMETERS),
            DateTimeWithZone.create(
                nowUtcMillis + timeToDestinationMillis,
                TimeZone.getTimeZone("Asia/Kolkata")
            )
        ).run {
            setRemainingTimeSeconds(TimeUnit.MILLISECONDS.toSeconds(timeToDestinationMillis))
            setRemainingTimeColor(CarColor.YELLOW)
            setRemainingDistanceColor(CarColor.RED)
            setTripText(CarText.create(carContext.getString(R.string.travel_est_trip_text)))
            setTripIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.ic_face_24px
                    )
                ).build()
            )
            build()
        }
    }


    fun getNextStep(carContext: CarContext): Step {
        val nextStepCue = carContext.getString(R.string.next_step_cue)
        val nextStepCueWithImage = SpannableString(nextStepCue)
        val highwaySign = CarIconSpan.create(
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_i5)).build(),
            CarIconSpan.ALIGN_CENTER
        )
        nextStepCueWithImage.setSpan(highwaySign, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val nextTurnIcon =
            CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.arrow_straight))
                .build()
        val nextManeuver = Maneuver.Builder(Maneuver.TYPE_STRAIGHT).setIcon(nextTurnIcon).build()

        return Step.Builder(nextStepCueWithImage).setManeuver(nextManeuver).build()
    }


}