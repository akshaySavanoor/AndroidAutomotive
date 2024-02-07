package com.akshay.weatherapp.templates

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action.BACK
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.Template
import androidx.car.app.versioning.CarAppApiLevels
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.createGenericActionStrip
import com.akshay.weatherapp.common.TemplateUtility.getIconByResource

/**
 * Template for UI messages conveying brief information and optional actions.
 * Ideal for error messages, permission prompts, and UI state details.
 *
 * Components:
 * - Header with optional action strip
 * - Up to 2 lines of wrapping text
 * - Optional image, icon, or loading spinner
 * - Up to 2 buttons in template body (one can be designated as primary)
 *
 * Usage:
 * - Quick messages with related actions as secondary
 * - For longer messages, consider using the Long Message template
 * - For more detailed information with prominent actions, use the Pane template
 *
 * Guidelines:
 * - MUST include text
 * - SHOULD designate a primary action when providing 2 actions
 * - SHOULD place the primary action closest to the driver (left for LHD vehicles) when there are 2 actions
 * - MAY include an image or icon asset
 * - MAY include up to 2 actions
 * - Use this template for prompting users about app permissions and opening related flows on the parked phone
 *   (as demonstrated in Grant permissions on the phone).
 */

class MessageTemplateExample(carContext: CarContext) : Screen(carContext) {
    private var mIsConfirmed = false
    override fun onGetTemplate(): Template {
        val settings = createGenericAction(
            title = carContext.getString(R.string.skip),
            onClickListener = OnClickListener {
                if (!mIsConfirmed) {
                    screenManager.pop()
                }
                mIsConfirmed = false
                invalidate()
            }
        )

        if (mIsConfirmed) {
            return MessageTemplate.Builder(carContext.getString(R.string.no_routes_found))
                .setIcon(CarIcon.ALERT)
                .setActionStrip(
                    createGenericActionStrip(
                        settings
                    )
                )
                .build()
        }
        val primaryActionBuilder = createGenericAction(
            title = carContext.getString(R.string.confirm),
            onClickListener = OnClickListener {
                mIsConfirmed = !mIsConfirmed
                invalidate() //Call invalidate() to re-render the onGetTemplate()
            },
            flag = if (carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_4) FLAG_PRIMARY else null
        )

        return MessageTemplate.Builder(carContext.getString(R.string.would_you_like_to_start_new_route))
            .run {
                setTitle(carContext.getString(R.string.route_option))
                setIcon(
                    getIconByResource(
                        icon = R.drawable.ic_route,
                        carContext = carContext
                    )
                )
                setHeaderAction(BACK)
                addAction(primaryActionBuilder)
                addAction(createGenericAction(
                    title = carContext.getString(R.string.cancel),
                    backgroundColor = CarColor.RED,
                    onClickListener = OnClickListener { screenManager.pop() }
                ))
                setActionStrip(createGenericActionStrip(settings))
                build()
            }
    }
}