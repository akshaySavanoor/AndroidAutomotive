package com.akshay.weatherapp.templates

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action.BACK
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.CarColor
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.car.app.model.LongMessageTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.car.app.versioning.CarAppApiLevels
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.createGenericActionStrip

/**
 * The LongMessageTemplate class presents a detailed message to be read while the car is parked,
 * with optional relevant actions.
 *
 * This template is useful for providing details about a destination or presenting legal text,
 * such as terms of service or a privacy policy, during a sign-in process.
 *
 * Includes:
 * - Header with optional action strip
 * - Unlimited lines of wrapping text (scrollable)
 * - Up to 2 buttons in the template body (optional), where one can be designated as primary.
 *
 * Note: This template displays its contents only when parked (see examples) and does not increase the step count.
 *
 * When the car is parked, this template can show a detailed message, such as a privacy policy, or terms of service
 * for the user to accept when signing in to the app. When the user is driving,
 * the long message is not shown, to prevent driver distraction. For these situations, it’s helpful to provide
 * a button with an alternative option, such as skipping sign-in and using the app in guest mode.
 *
 * Long Message Template UX Requirements:
 * App developers:
 * - MUST Include text.
 * - SHOULD Designate a primary action when providing 2 actions.
 * - SHOULD Place the primary action closest to the driver (on the left for left-hand-drive vehicles) when there are 2 actions.
 * - MAY Include up to 2 actions.
 * [Reference](https://developers.google.com/cars/design/create-apps/apps-for-drivers/templates/long-message-template)
 */
class LongMessageTemplateExample(
    carContext: CarContext,
    private val title: String = carContext.getString(R.string.privacy_policy)
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        if (carContext.carAppApiLevel < CarAppApiLevels.LEVEL_2) {
            return MessageTemplate.Builder(
                carContext.getString(R.string.long_msg_template_not_supported_text)
            )
                .setHeaderAction(BACK)
                .build()
        }

        val primaryActionBuilder =
            createGenericAction(
                title = carContext.getString(R.string.accept),
                onClickListener = ParkedOnlyOnClickListener.create {
                    screenManager.push(ListTemplateExample(carContext))
                },
                flag = if (carContext.carAppApiLevel >= CarAppApiLevels.LEVEL_4) FLAG_PRIMARY else null
            )
        // This flag highlights buttons with higher importance

        val longMessage = SpannableString(carContext.getString(R.string.long_message))
        longMessage.setSpan(
            ForegroundCarColorSpan.create(CarColor.GREEN),
            2,
            11,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return LongMessageTemplate.Builder(longMessage)
            .run {
                setTitle(title)
                setHeaderAction(BACK)
                addAction(primaryActionBuilder)
                addAction(
                    createGenericAction(
                        title = carContext.getString(R.string.no_thanks),
                        backgroundColor = CarColor.RED,
                        onClickListener = ParkedOnlyOnClickListener.create { screenManager.pop() }
                    )
                )
                setActionStrip(
                    createGenericActionStrip(
                        createGenericAction(
                            title = carContext.getString(R.string.skip),
                            onClickListener = OnClickListener { screenManager.pop() }
                        )
                    )
                )
                build()
            }
    }
}