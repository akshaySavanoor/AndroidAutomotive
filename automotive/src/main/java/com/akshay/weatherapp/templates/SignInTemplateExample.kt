package com.akshay.weatherapp.templates

import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.BACK
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.InputCallback
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.car.app.model.signin.InputSignInMethod
import androidx.car.app.model.signin.PinSignInMethod
import androidx.car.app.model.signin.QRCodeSignInMethod
import androidx.car.app.model.signin.SignInTemplate
import androidx.car.app.versioning.CarAppApiLevels
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.HomeScreen
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.Constants.Companion.DUMMY_LOGIN_URL
import com.akshay.weatherapp.common.Utility.Companion.clickable
import com.akshay.weatherapp.common.Utility.Companion.generateRandomString
import com.akshay.weatherapp.common.Utility.Companion.validateEmail
import com.akshay.weatherapp.common.Utility.Companion.validatePassword

/**
 * The Sign-in template presents options for signing in to the app while parked.
 *
 * Points to consider:
 *
 * 1. Header with optional action strip
 * 2. Up to 2 lines of primary text (optional)
 * 3. Primary sign-in method
 * 4. Additional text, such as disclaimers and links to terms of service (optional)
 * 5. Up to 2 buttons (optional)
 *
 * Sign-in template UX requirements:
 *
 * App developers:
 *
 * MUST    Include a sign-in method when using this template.
 * SHOULD  Use input fields only for user sign-in, not for collecting other types of user input.
 * SHOULD  Prioritize the shortest flow (using the fewest clicks).
 * SHOULD  Prioritize the most popular method.
 */

class SignInTemplateExample(carContext: CarContext) : Screen(carContext) {
    var error: String? = null
    private var currentState = Constants.Companion.SignInState.EMAIL
    private val additionalText = clickable(
        carContext.getString(R.string.sign_in_additional_text),
        32,
        16,
        61,
        14,
        {
            screenManager.push(
                LongMessageTemplateExample(
                    carContext,
                    carContext.getString(R.string.terms_of_policy)
                )
            )
        },
        {
            screenManager.push(LongMessageTemplateExample(carContext))
        }
    )

    private val mQRCodeSignInAction = Action.Builder()
        .setTitle(getCarContext().getString(R.string.use_qr_code))
        .setOnClickListener(ParkedOnlyOnClickListener.create {
            currentState = Constants.Companion.SignInState.OR_CODE
            invalidate()
        })
        .build()

    private val mPinSignInAction = Action.Builder()
        .setTitle(getCarContext().getString(R.string.use_pin))
        .setOnClickListener(ParkedOnlyOnClickListener.create {
            currentState = Constants.Companion.SignInState.PIN
            invalidate()
        })
        .build()

    private val mInputSignIn = Action.Builder()
        .setTitle(getCarContext().getString(R.string.use_email))
        .setOnClickListener(ParkedOnlyOnClickListener.create {
            currentState = Constants.Companion.SignInState.EMAIL
            invalidate()
        })
        .build()

    private val skipAction = Action.Builder()
        .setTitle(carContext.getString(R.string.skip))
        .setOnClickListener { screenManager.pop() }
        .build()

    private fun getInputSignInMethod(
        hint: String,
        keyboardType: Int,
        callback: InputCallback
    ): InputSignInMethod.Builder {
        return InputSignInMethod.Builder(callback)
            .setInputType(InputSignInMethod.INPUT_TYPE_DEFAULT)
            .setHint(hint)
            .setKeyboardType(keyboardType)
            .apply {
                error?.let { setErrorMessage(it) }
            }
    }

    private fun handleInputSubmission(text: String, nextState: Constants.Companion.SignInState) {
        when (currentState) {
            Constants.Companion.SignInState.EMAIL -> {
                validateEmail(carContext, text)?.let {
                    error = it
                    invalidate()
                } ?: kotlin.run {
                    error = null
                    currentState = nextState
                    invalidate()
                }
            }

            Constants.Companion.SignInState.PASSWORD -> {
                validatePassword(carContext, text)?.let {
                    error = it
                    invalidate()
                } ?: kotlin.run {
                    error = null
                    currentState = nextState
                    invalidate()
                }
            }

            else -> {}
        }

    }

    /**
     * When the user is driving, the sign-in content is not shown to prevent driver distraction.
     * For these situations, it’s helpful to provide a button with an alternative option,
     * such as skipping sign-in and using the app in guest mode.
     */

    private fun createSignInTemplate(
        hint: String,
        keyboardType: Int,
        nextState: Constants.Companion.SignInState
    ): SignInTemplate {
        val callback = object : InputCallback {
            override fun onInputSubmitted(text: String) {
                super.onInputSubmitted(text)

                handleInputSubmission(text, nextState)
            }
        }

        val inputSignInMethod = getInputSignInMethod(hint, keyboardType, callback)

        return SignInTemplate.Builder(inputSignInMethod.build())
            .setTitle(carContext.getString(R.string.sign_in))
            .setHeaderAction(BACK)
            .setAdditionalText(additionalText)
            .setInstructions(carContext.getString(R.string.sign_in_instruction))
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(skipAction)
                    .build()
            )
            .addAction(if (carContext.carAppApiLevel > CarAppApiLevels.LEVEL_3) mQRCodeSignInAction else mPinSignInAction)
            .build()
    }

    /**
     * The provided pin must be no more than 12 characters long. To facilitate typing this
     * code, it is recommended restricting the string to a limited set (for example, numbers,
     * upper-case letters, hexadecimal, etc.).
     */
    private fun getPinSignInTemplate(): Template {
        val pinSignInMethod = PinSignInMethod(generateRandomString(12))
        return SignInTemplate.Builder(pinSignInMethod)
            .setTitle(carContext.getString(R.string.sign_in))
            .setInstructions(carContext.getString(R.string.pin_sign_in_instruction))
            .setHeaderAction(BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(skipAction)
                    .build()
            )
            .addAction(mInputSignIn)
            .setAdditionalText(additionalText)
            .build()
    }

    /**
     * The URL to be used in creating a QR Code.
     */
    private fun getQRCodeSignInTemplate(): Template {
        val qrCodeSignInMethod =
            QRCodeSignInMethod(Uri.parse(DUMMY_LOGIN_URL))
        return SignInTemplate.Builder(qrCodeSignInMethod)
            .setTitle(carContext.getString(R.string.qr_code_sign_in_title))
            .setHeaderAction(BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(skipAction)
                    .build()
            )
            .addAction(mInputSignIn)
            .addAction(mPinSignInAction)
            .setAdditionalText(additionalText)
            .build()
    }

    private fun successPage(): Template {
        return MessageTemplate.Builder(carContext.getString(R.string.login_successful))
            .setTitle(carContext.getString(R.string.sign_in))
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.ic_success
                    )
                )
                    .build()
            )
            .addAction(Action.Builder()
                .setTitle(carContext.getString(R.string.go_to_home))
                .setFlags(FLAG_PRIMARY)
                .setOnClickListener {
                    screenManager.push(HomeScreen(carContext))
                }
                .build())
            .build()
    }

    override fun onGetTemplate(): Template {
        if (carContext.carAppApiLevel < CarAppApiLevels.LEVEL_2) {
            return MessageTemplate.Builder(carContext.getString(R.string.sign_in))
                .setTitle(carContext.getString(R.string.cancel))
                .setHeaderAction(BACK)
                .build()
        }

        return when (currentState) {
            Constants.Companion.SignInState.EMAIL -> createSignInTemplate(
                carContext.getString(R.string.email),
                InputSignInMethod.KEYBOARD_EMAIL,
                Constants.Companion.SignInState.PASSWORD
            )

            Constants.Companion.SignInState.PASSWORD -> createSignInTemplate(
                carContext.getString(R.string.password),
                InputSignInMethod.KEYBOARD_DEFAULT,
                Constants.Companion.SignInState.SUCCESS
            )

            Constants.Companion.SignInState.PIN -> getPinSignInTemplate()
            Constants.Companion.SignInState.OR_CODE -> getQRCodeSignInTemplate()
            Constants.Companion.SignInState.SUCCESS -> successPage()
        }
    }
}
