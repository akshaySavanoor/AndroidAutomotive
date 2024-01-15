package com.akshay.weatherapp

import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
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

class HomeScreen(carContext: CarContext) : Screen(carContext) {
    private val itemListBuilder = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(R.string.no_data_found))

    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        itemListBuilder.apply {
            clearItems()
            addItem(createWeatherRow(LIST_TEMPLATE))
            addItem(createWeatherRow(GRID_TEMPLATE))
            addItem(createWeatherRow(MESSAGE_TEMPLATE))
            addItem(createWeatherRow(LONG_MESSAGE_TEMPLATE))
            addItem(createWeatherRow(PANE_TEMPLATE))
            addItem(createWeatherRow(MAP_TEMPLATE))
            addItem(createWeatherRow(NAVIGATION_TEMPLATE))
            addItem(createWeatherRow(SEARCH))
            addItem(createWeatherRow(SIGN_IN_TEMPLATE))
        }
        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.templates))
            .setSingleList(itemListBuilder.build())
            .build()
    }

    private fun createWeatherRow(title: String): Row {

        return Row.Builder()
            .setTitle(title)
            .setBrowsable(true)
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
    }
}