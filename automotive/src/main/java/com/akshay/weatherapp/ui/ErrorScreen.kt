package com.akshay.weatherapp.ui

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.akshay.weatherapp.HomeScreen
import com.akshay.weatherapp.R
import com.akshay.weatherapp.templates.GridTemplateExample
import com.akshay.weatherapp.templates.ListTemplateExample
import com.akshay.weatherapp.templates.LongMessageTemplateExample
import com.akshay.weatherapp.templates.MapTemplateExample
import com.akshay.weatherapp.templates.MessageTemplateExample
import com.akshay.weatherapp.templates.NavigationTemplateExample
import com.akshay.weatherapp.templates.PaneTemplateExample
import com.akshay.weatherapp.templates.SearchTemplateExample
import com.akshay.weatherapp.templates.SignInTemplateExample
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LONG_MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MAP_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.PANE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SEARCH
import com.akshay.weatherapp.common.Constants.Companion.SIGN_IN_TEMPLATE

class ErrorScreen(carContext: CarContext, private val title: String) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val goToHome = Action.Builder()
            .setTitle(carContext.getString(R.string.home))
            .setOnClickListener {
                screenManager.push(HomeScreen(carContext))
            }
            .build()
        val retryAction = Action.Builder()
            .setTitle(carContext.getString(R.string.retry))
            .setFlags(FLAG_PRIMARY)
            .setOnClickListener {
                when (title) {
                    LIST_TEMPLATE -> screenManager.push(ListTemplateExample(carContext))
                    GRID_TEMPLATE -> screenManager.push(GridTemplateExample(carContext))
                    MESSAGE_TEMPLATE -> screenManager.push(MessageTemplateExample(carContext))
                    LONG_MESSAGE_TEMPLATE -> screenManager.push(
                        LongMessageTemplateExample(
                            carContext
                        )
                    )

                    PANE_TEMPLATE -> screenManager.push(PaneTemplateExample(carContext))
                    MAP_TEMPLATE -> screenManager.push(MapTemplateExample(carContext))
                    NAVIGATION_TEMPLATE -> screenManager.push(NavigationTemplateExample(carContext))
                    SEARCH -> screenManager.push(SearchTemplateExample(carContext))
                    SIGN_IN_TEMPLATE -> screenManager.push(SignInTemplateExample(carContext))
                }
            }

            .build()
        return MessageTemplate.Builder(carContext.getString(R.string.something_went_wrong))
            .setTitle(title)
            .setIcon(CarIcon.ERROR)
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(goToHome)
                    .build()
            )
            .addAction(
                retryAction
            )
            .build()
    }
}