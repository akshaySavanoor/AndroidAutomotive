package com.akshay.weatherapp.common

class Constants {
    companion object {
        const val LABEL_LATITUDE = "Latitude"
        const val LABEL_LONGITUDE = "Longitude"
        const val COORDINATES = "Coordinates"
        const val WEATHER_CONDITION = "Weather Condition"
        const val TEMPERATURE = "Temperature"
        const val HUMIDITY = "Humidity"
        const val WIND = "Wind"
        const val SYSTEM_INFORMATION = "System Information"
        const val FEELS_LIKE = "Feels Like"
        const val PRESSURE = "Pressure"
        const val MINIMUM_TEMPERATURE = "Minimum temperature at the moment"
        const val MAXIMUM_TEMPERATURE = "Maximum temperature at the moment"
        const val SEA_LEVEL = "Atmospheric pressure on the sea level"
        const val GROUND_LEVEL = " Atmospheric pressure on the ground level"
        const val WIND_SPEED = "Wind Speed"
        const val WIND_DIRECTION = "Wind Direction"
        const val CLOUD = "Cloudiness"
        const val COUNTRY_CODE = "Country code"
        const val CITY_NAME = "City name"
        const val SUN_RISE_TIME = "Sunrise time"
        const val SUN_SET_TIME = "Sunset time"
        const val WEATHER_INFORMATION = "Weather Information"
        const val SEARCH = "Search"
        const val SETTINGS = "Settings"
        const val FAVOURITE = "Favourite"
        const val WITHIN_FIVE = "Within 5 km"
        const val WITHIN_TEN = "Within 10 km"
        const val WITHIN_TWENTY = "Within 20 km"
        const val CREDIT_CARD = "Credit Card Accepted"
        const val MOBILE_PAYMENT = "Mobile Payment Supported"
        const val CASH = "Cash Accepted"
        const val LIST_TEMPLATE = "List Template"
        const val GRID_TEMPLATE = "Grid Template"
        const val PANE_TEMPLATE = "Pane Template"
        const val MAP_TEMPLATE = "Map Template"
        const val NAVIGATION_TEMPLATE = "Navigation Template"
        const val MESSAGE_TEMPLATE = "Message Template"
        const val LONG_MESSAGE_TEMPLATE = "Long Message Template"
        const val SIGN_IN_TEMPLATE = "Sign-In Template"
        const val SEARCH_TEMPLATE = "Search Template"
        const val PACKAGE_PREFIX = "androidx.car.app"
        const val DUMMY_LOGIN_URL = "https://practicetestautomation.com/practice-test-login/"

        enum class SignInState {
            EMAIL,
            PASSWORD,
            PIN,
            OR_CODE,
            SUCCESS
        }
    }
}
