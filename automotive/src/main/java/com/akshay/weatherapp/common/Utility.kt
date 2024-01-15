package com.akshay.weatherapp.common

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Spannable
import android.text.SpannableString
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.model.CarColor
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
    }
}