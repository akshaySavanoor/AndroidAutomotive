package com.akshay.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LONG_MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MAP_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_WITH_ALERT
import com.akshay.weatherapp.common.Constants.Companion.PANE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.ROUTE_PREVIEW_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SEARCH_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SIGN_IN_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.TEMPLATE_RESTRICTION
import com.akshay.weatherapp.common.Utility.Companion.requestPermission
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.templates.GridTemplateExample
import com.akshay.weatherapp.templates.ListTemplateExample
import com.akshay.weatherapp.templates.LongMessageTemplateExample
import com.akshay.weatherapp.templates.MapTemplateExample
import com.akshay.weatherapp.templates.MessageTemplateExample
import com.akshay.weatherapp.templates.NavigationTemplateEx
import com.akshay.weatherapp.templates.NavigationTemplateExample
import com.akshay.weatherapp.templates.PaneTemplateExample
import com.akshay.weatherapp.templates.RouteTemplateExample
import com.akshay.weatherapp.templates.SearchTemplateExample
import com.akshay.weatherapp.templates.SignInTemplateExample
import com.akshay.weatherapp.ui.TemplateRestrictionUi

class HomeScreen(carContext: CarContext) : Screen(carContext) {

    private var hasPermissionLocation: Boolean = false
    private var requestedPermission = false
    private val itemListBuilder = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(R.string.no_data_found))

    private fun checkPermission() {
        hasPermissionLocation =
            ContextCompat.checkSelfPermission(
                carContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        carContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED

        if (!hasPermissionLocation && !requestedPermission) {
            requestedPermission = true
            requestPermission(carContext) { approved, rejected ->
                if (approved.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.approved))

                } else if (rejected.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.rejected))
                }
            }
        }
    }

    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        checkPermission()

        val exitAction = Action.Builder()
            .setTitle(carContext.getString(R.string.exit))
            .setOnClickListener {
                carContext.finishCarApp()
            }
            .build()
        itemListBuilder.apply {
            clearItems()
            addItem(createWeatherRow(LIST_TEMPLATE))
            addItem(createWeatherRow(GRID_TEMPLATE))
            addItem(createWeatherRow(MESSAGE_TEMPLATE))
            addItem(createWeatherRow(LONG_MESSAGE_TEMPLATE))
            addItem(createWeatherRow(PANE_TEMPLATE))
            addItem(createWeatherRow(MAP_TEMPLATE))
            addItem(createWeatherRow(NAVIGATION_TEMPLATE))
            addItem(createWeatherRow(SEARCH_TEMPLATE))
            addItem(createWeatherRow(SIGN_IN_TEMPLATE))
            addItem(createWeatherRow(ROUTE_PREVIEW_TEMPLATE))
            addItem(createWeatherRow(NAVIGATION_WITH_ALERT))
            addItem(createWeatherRow(TEMPLATE_RESTRICTION))
        }
        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.templates))
            .setActionStrip(ActionStrip.Builder().addAction(exitAction).build())
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
                    SEARCH_TEMPLATE -> screenManager.push(SearchTemplateExample(carContext))
                    SIGN_IN_TEMPLATE -> screenManager.push(SignInTemplateExample(carContext))
                    ROUTE_PREVIEW_TEMPLATE -> screenManager.push(RouteTemplateExample(carContext))
                    NAVIGATION_WITH_ALERT -> screenManager.push(NavigationTemplateEx(carContext))
                    TEMPLATE_RESTRICTION -> screenManager.push(TemplateRestrictionUi(carContext))
                }
            }
            .build()
    }
}