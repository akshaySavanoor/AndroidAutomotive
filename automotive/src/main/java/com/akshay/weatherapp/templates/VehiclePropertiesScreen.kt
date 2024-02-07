package com.akshay.weatherapp.templates

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.viewmodel.VehiclePropertiesViewModel


class VehiclePropertiesScreen(
    carContext: CarContext,
) : Screen(carContext), DefaultLifecycleObserver {

    private val itemListBuilder = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(R.string.permission_is_needed_to_access_the_car_information))

    private var carPropertyManager: CarPropertyManager? = null

    private var _viewModel: VehiclePropertiesViewModel? = null
    private val viewModel get() = _viewModel!!

    private val TAG = carContext.getString(R.string.vehiclepropertiesscreen)

    private val propertyIds = listOf(
        VehiclePropertyIds.PERF_VEHICLE_SPEED,
        VehiclePropertyIds.CURRENT_GEAR,
        VehiclePropertyIds.IGNITION_STATE,
        VehiclePropertyIds.FUEL_LEVEL,
        VehiclePropertyIds.EV_BATTERY_LEVEL
    )

    private var isLoading = true
    private var isPermissionGranted = ContextCompat.checkSelfPermission(
        carContext, Car.PERMISSION_SPEED
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        carContext, Car.PERMISSION_ENERGY
    ) == PackageManager.PERMISSION_GRANTED

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        checkForPermission()
    }

    override fun onGetTemplate(): Template {
        if (isPermissionGranted) {
            if (_viewModel == null) {
                carPropertyManager =
                    Car.createCar(carContext).getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
                carPropertyManager?.let {
                    _viewModel = VehiclePropertiesViewModel(it, carContext)
                }
            }
            createPropertiesList()
        }
        val carHardwareList = ListTemplate.Builder()
        if (isLoading && !isPermissionGranted) {
            return carHardwareList.run {
                setTitle(Constants.HARDWARE_PROPERTIES)
                setHeaderAction(Action.BACK)
                setLoading(true)
                build()
            }
        }
        return carHardwareList.setTitle(Constants.HARDWARE_PROPERTIES).setHeaderAction(Action.BACK)
            .setSingleList(itemListBuilder.build()).build()
    }

    private fun requestPermissionPrompt() {
        try {
            carContext.requestPermissions(
                listOf(
                    Car.PERMISSION_SPEED, Car.PERMISSION_ENERGY
                )
            ) { result, _ ->
                try {
                    if (result == listOf(Car.PERMISSION_SPEED, Car.PERMISSION_ENERGY)) {
                        isLoading = false
                        isPermissionGranted = true
                        invalidate()
                    } else {
                        screenManager.pop()
                        Utility.showErrorMessage(
                            carContext, carContext.getString(R.string.permission_denied)
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkForPermission() {
        if (!isPermissionGranted) {
            requestPermissionPrompt()
        } else {
            invalidate()
        }
    }

    @OptIn(ExperimentalCarApi::class)
    private fun createPropertiesList() {

        val speed = viewModel.fetchSpeedInKmph()
        val gear = viewModel.fetchCurrentGear()
        val evBatteryLevel = viewModel.fetchEvBatteryLevel()
        val fuelLevel = viewModel.fetchFuelLevel()
        val ignitionState = viewModel.fetchIgnitionState()

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        if (viewModel.checkSpeedChanged(value.value as Float)) {
                            viewModel.setSpeedInKmph(value.value.toString().toFloat())
                            invalidate()
                        }
                    }

                    VehiclePropertyIds.CURRENT_GEAR -> {
                        if (viewModel.checkCurrentGearChanged(value.value as Int)) {
                            viewModel.setCurrentGear(value.value.toString().toInt())
                            invalidate()
                        }
                    }

                    VehiclePropertyIds.FUEL_LEVEL -> {
                        if (viewModel.checkFuelLevelChanged(value.value as Float)) {
                            viewModel.setFuelLevel(value.value.toString().toFloat())
                            invalidate()
                        }
                    }

                    VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                        if (viewModel.checkEvBatteryLevelChanged(value.value as Float)) {
                            viewModel.setEvBatteryLevel(value.value.toString().toFloat())
                            invalidate()
                        }
                    }

                    VehiclePropertyIds.IGNITION_STATE -> {
                        if (viewModel.checkIgnitionStateChanged(value.value as Int)) {
                            viewModel.setIgnitionState(value.value.toString().toInt())
                            invalidate()
                        }
                    }
                }
            }

            override fun onErrorEvent(p0: Int, p1: Int) {
                Log.e(TAG, carContext.getString(R.string.property_error))
            }

        }
        registerCallbacks(callback, propertyIds)


        itemListBuilder.apply {
            clearItems()
            addItem(createRow(VehiclePropertyIds.PERF_VEHICLE_SPEED, speed))
            addItem(createRow(VehiclePropertyIds.CURRENT_GEAR, gear))
            addItem(createRow(VehiclePropertyIds.EV_BATTERY_LEVEL, evBatteryLevel))
            addItem(createRow(VehiclePropertyIds.FUEL_LEVEL, fuelLevel))
            addItem(createRow(VehiclePropertyIds.IGNITION_STATE, ignitionState))
        }
    }

    private fun registerCallbacks(
        callback: CarPropertyManager.CarPropertyEventCallback, propertyIds: List<Int>
    ) {
        propertyIds.forEach {
            carPropertyManager?.registerCallback(
                callback, it, CarPropertyManager.SENSOR_RATE_NORMAL
            )
        }
    }

    private fun createRow(property: Int, value: Any): Row {
        return when (property) {
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                Row.Builder().setTitle(carContext.getString(R.string.current_speed))
                    .addText(carContext.getString(R.string.value_kmph, value)).build()
            }

            VehiclePropertyIds.CURRENT_GEAR -> {
                Row.Builder().setTitle(carContext.getString(R.string.current_gear))
                    .addText(value.toString()).build()
            }

            VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                Row.Builder().setTitle(carContext.getString(R.string.current_ev_battery_level))
                    .addText("$value").build()
            }

            VehiclePropertyIds.FUEL_LEVEL -> {
                Row.Builder().setTitle(carContext.getString(R.string.current_fuel_level))
                    .addText("$value").build()
            }

            VehiclePropertyIds.IGNITION_STATE -> {
                Row.Builder().setTitle(carContext.getString(R.string.ignition_state))
                    .addText("$value").build()
            }

            else -> Row.Builder().build()
        }
    }
}