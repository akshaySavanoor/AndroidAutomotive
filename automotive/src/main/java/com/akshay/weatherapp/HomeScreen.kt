package com.akshay.weatherapp

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.HARDWARE_PROPERTIES
import com.akshay.weatherapp.common.Constants.Companion.HOME_SCREEN
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LONG_MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MAP_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.MESSAGE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.NAVIGATION_WITH_ALERT
import com.akshay.weatherapp.common.Constants.Companion.PANE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.PLACE_LIST_MAP_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.ROUTE_PREVIEW_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SEARCH_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SIGN_IN_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.TEMPLATE_RESTRICTION
import com.akshay.weatherapp.common.Utility.Companion.checkPermission
import com.akshay.weatherapp.templates.GridTemplateExample
import com.akshay.weatherapp.templates.ListTemplateExample
import com.akshay.weatherapp.templates.LongMessageTemplateExample
import com.akshay.weatherapp.templates.MapTemplateExample
import com.akshay.weatherapp.templates.MessageTemplateExample
import com.akshay.weatherapp.templates.NavigationTemplateEx
import com.akshay.weatherapp.templates.PaneTemplateExample
import com.akshay.weatherapp.templates.PlaceListMapExample
import com.akshay.weatherapp.templates.RouteTemplateExample
import com.akshay.weatherapp.templates.SearchTemplateExample
import com.akshay.weatherapp.templates.SignInTemplateExample
import com.akshay.weatherapp.misc.TemplateRestrictionUi

class HomeScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private var mCurrentUxRestrictions: CarUxRestrictions? = null
    private val mCar: Car = Car.createCar(carContext)

    private val mUxrChangeListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { carUxRestrictions ->
        mCurrentUxRestrictions = carUxRestrictions
        println(carUxRestrictions.isRequiresDistractionOptimization)
    }
    private val itemListBuilder = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(R.string.no_data_found))

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        checkPermission(carContext)
        val mCarUxRestrictionsManager = mCar.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
        mCarUxRestrictionsManager.registerListener(mUxrChangeListener)
        mUxrChangeListener.onUxRestrictionsChanged(mCarUxRestrictionsManager.currentCarUxRestrictions)
    }

    /**
     * screenManager.popTo(marker): Removes screens from the top of the stack until a Screen which has the given
     * marker is found, or the root has been reached.
     */
    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        this.marker = HOME_SCREEN //Using marker we can remove multiple screens from the stack
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
            addItem(createWeatherRow(PLACE_LIST_MAP_TEMPLATE))
            addItem(createWeatherRow(SEARCH_TEMPLATE))
            addItem(createWeatherRow(SIGN_IN_TEMPLATE))
            addItem(createWeatherRow(ROUTE_PREVIEW_TEMPLATE))
            addItem(createWeatherRow(NAVIGATION_WITH_ALERT))
            addItem(createWeatherRow(TEMPLATE_RESTRICTION))
            addItem(createWeatherRow(HARDWARE_PROPERTIES))
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
            .setOnClickListener(ParkedOnlyOnClickListener.create{
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
                    PLACE_LIST_MAP_TEMPLATE -> screenManager.push(PlaceListMapExample(carContext))
                    SEARCH_TEMPLATE -> screenManager.push(SearchTemplateExample(carContext))
                    SIGN_IN_TEMPLATE -> screenManager.push(SignInTemplateExample(carContext))
                    ROUTE_PREVIEW_TEMPLATE -> screenManager.push(RouteTemplateExample(carContext))
                    NAVIGATION_WITH_ALERT -> screenManager.push(NavigationTemplateEx(carContext))
                    TEMPLATE_RESTRICTION -> screenManager.push(TemplateRestrictionUi(carContext))
                    HARDWARE_PROPERTIES -> screenManager.push(VehiclePropertiesScreen(carContext))
                }
            })
            .build()
    }
}