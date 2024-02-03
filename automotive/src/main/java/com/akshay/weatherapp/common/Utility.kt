package com.akshay.weatherapp.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Spannable
import android.text.SpannableString
import androidx.car.app.CarAppPermission
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.ClickableSpan
import androidx.car.app.model.ForegroundCarColorSpan
import androidx.core.net.toUri
import com.akshay.weatherapp.R
import com.akshay.weatherapp.model.Location

class Utility {
    companion object {
        fun showErrorMessage(
            carContext: CarContext,
            message: String = carContext.getString(R.string.something_went_wrong)
        ) {
            CarToast.makeText(
                carContext,
                message,
                CarToast.LENGTH_SHORT
            ).show()
        }

        fun kelvinToCelsius(kelvin: Double): Int {
            return (kelvin - 273.15).toInt()
        }

        fun colorize(s: SpannableString, color: CarColor, start: Int, end: Int) {
            s.setSpan(
                ForegroundCarColorSpan.create(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        fun getColoredString(str: String, start: Int, end: Int, color: CarColor): CharSequence {
            if (str.isNotEmpty()) {
                val ss = SpannableString(str)
                colorize(ss, color, start, end)
                return ss
            }
            return str
        }

        fun Location.toIntent(action: String): Intent {
            return Intent(action).apply {
                data = "geo:$latitude,$longitude".toUri()
            }
        }

        fun showToast(carContext: CarContext, message: String) {
            CarToast.makeText(carContext, message, CarToast.LENGTH_SHORT).show()
        }

        fun isDeviceOnLine(carContext: CarContext?): Boolean {
            var isConnected = false
            carContext?.let {
                val connectivityManager =
                    carContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                isConnected = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }

            return isConnected
        }

        fun requestPermission(
            carContext: CarContext,
            permissionCallback: (List<String>, List<String>) -> Unit
        ) {
            val permissions = ArrayList<String>()
            val declaredPermissions: Array<String>

            try {
                val info = carContext.packageManager.getPackageInfo(
                    carContext.packageName,
                    PackageManager.GET_PERMISSIONS
                )
                declaredPermissions = info.requestedPermissions ?: emptyArray()
            } catch (e: PackageManager.NameNotFoundException) {
                showToast(carContext, carContext.getString(R.string.package_not_found))
                return
            }

            for (declaredPermission in declaredPermissions) {
                // Exclude permissions related to the car app host as they are considered normal
                // but might appear as ungranted by the system.
                if (declaredPermission.startsWith(Constants.PACKAGE_PREFIX)) {
                    continue
                }
                try {
                    CarAppPermission.checkHasPermission(carContext, declaredPermission)
                } catch (e: SecurityException) {
                    permissions.add(declaredPermission)
                }
            }

            /**
             * The CarAppTheme style is defined as any other themes in a resource file:
             * <resources>æ
             * <style name="CarAppTheme">
             * <item name="carPermissionActivityLayout">@layout/app_branded_background</item>
             * </style>
             * </resources>
             * The default behavior is to have no background behind the permission request.
             */
            carContext.requestPermissions(
                permissions.toTypedArray().toMutableList()
            ) { approved, rejected ->
                permissionCallback(approved, rejected)
            }

            if (!carContext.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
                showToast(carContext, carContext.getString(R.string.phone_screen_permission_msg))
            }
        }

        fun clickable(
            s: String,
            startingIndexOfFirstText: Int,
            endingIndexOfFirstText: Int,
            startingIndexOfSecondText: Int,
            endingIndexOfSecondText: Int,
            action1: Runnable,
            action2: Runnable
        ): CharSequence {
            val ss = SpannableString(s)
            ss.setSpan(
                ClickableSpan.create { action1.run() },
                startingIndexOfFirstText,
                startingIndexOfFirstText + endingIndexOfFirstText,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ss.setSpan(
                ClickableSpan.create { action2.run() },
                startingIndexOfSecondText,
                startingIndexOfSecondText + endingIndexOfSecondText,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return ss
        }

        fun validateEmail(carContext: CarContext, email: String): String? {
            val emailRegex = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$""")

            return when {
                email.isBlank() -> carContext.getString(R.string.error_email_empty)
                email.length < 7 -> carContext.getString(R.string.error_email_too_short)
                !emailRegex.matches(email) -> carContext.getString(R.string.error_invalid_email_format)
                else -> null
            }
        }

        fun validatePassword(carContext: CarContext, password: String): String? {
            val digitRegex = Regex(".*\\d.*")
            val specialCharRegex = Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")
            val upperCaseRegex = Regex(".*[A-Z].*")
            val lowerCaseRegex = Regex(".*[a-z].*")

            return when {
                password.isBlank() -> carContext.getString(R.string.error_password_empty)
                password.length < 8 || password.length > 14 -> carContext.getString(R.string.error_password_length)
                !digitRegex.matches(password) -> carContext.getString(R.string.error_password_digit)
                !specialCharRegex.matches(password) -> carContext.getString(R.string.error_password_special_char)
                !upperCaseRegex.matches(password) -> carContext.getString(R.string.error_password_uppercase)
                !lowerCaseRegex.matches(password) -> carContext.getString(R.string.error_password_lowercase)
                else -> null
            }
        }

        fun generateRandomString(length: Int): String {
            val chars = ('0'..'9') + ('A'..'Z')
            return (1..length)
                .map { chars.random() }
                .joinToString("")
        }

        fun goToHome(carContext: CarContext, screen: Screen): ActionStrip {
            return  ActionStrip.Builder()
                .addAction(Action.Builder()
                    .setTitle(carContext.getString(R.string.home))
                    .setOnClickListener {
                        screen.screenManager.popToRoot()
                    }
                    .build())
                .build()
        }

        //Only for the development purpose
        @SuppressLint("PrivateApi")
        fun printBuildTypeUsingReflection() {
            try {
                val systemPropertiesClass = Class.forName("android.os.SystemProperties")
                val getMethod = systemPropertiesClass.getMethod("get", String::class.java)

                val buildType = getMethod.invoke(null, "ro.build.type") as String
                println("Build type (reflection): $buildType")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}