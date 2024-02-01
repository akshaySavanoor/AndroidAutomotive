package com.akshay.weatherapp.templates

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
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
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.viewmodel.VehiclePropertiesViewModel


class VehiclePropertiesScreen(
    carContext: CarContext,
) : Screen(carContext) {

    private val itemListBuilder = ItemList.Builder().setNoItemsMessage("No data found")
    private var _viewModel: VehiclePropertiesViewModel? = null
    private val viewModel get() = _viewModel!!
    private var carPropertyManager: CarPropertyManager? = null

    private val propertyIds = listOf(
        VehiclePropertyIds.PERF_VEHICLE_SPEED,
        VehiclePropertyIds.CURRENT_GEAR,
        VehiclePropertyIds.IGNITION_STATE,
        VehiclePropertyIds.FUEL_LEVEL,
        VehiclePropertyIds.EV_BATTERY_LEVEL
    )

    override fun onGetTemplate(): Template {
        createPropertiesList()
        return ListTemplate.Builder()
            .setTitle(Constants.HARDWARE_PROPERTIES)
            .setHeaderAction(Action.BACK)
            .setSingleList(itemListBuilder.build())
            .build()
    }

    @OptIn(ExperimentalCarApi::class)
    private fun createPropertiesList() {

        carPropertyManager =
            Car.createCar(carContext).getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
        carPropertyManager?.let {
            _viewModel = VehiclePropertiesViewModel(it)
        }

        val speed = viewModel.fetchSpeedInKmph()
        val gear = viewModel.fetchCurrentGear()
        val evBatteryLevel = viewModel.fetchEvBatteryLevel()
        val fuelLevel = viewModel.fetchFuelLevel()
        val ignitionState = viewModel.fetchIgnitionState()

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        viewModel.setSpeedInKmph(value.value.toString().toFloat())
                    }

                    VehiclePropertyIds.CURRENT_GEAR -> {
                        viewModel.setCurrentGear(value.value.toString().toInt())
                    }

                    VehiclePropertyIds.FUEL_LEVEL -> {
                        viewModel.setFuelLevel(value.value.toString().toFloat())
                    }

                    VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                        viewModel.setEvBatteryLevel(value.value.toString().toFloat())
                    }

                    VehiclePropertyIds.IGNITION_STATE -> {
                        viewModel.setIgnitionState(value.value.toString().toInt())
                    }

                }
                invalidate()
            }

            override fun onErrorEvent(p0: Int, p1: Int) {
                Log.e("ERROR", "Property error")
            }

        }

        try {
            carContext.requestPermissions(listOf(Car.PERMISSION_SPEED)) { result, _ ->
                try {
                    if (result == listOf(Car.PERMISSION_SPEED)) {
                        registerCallbacks(callback, propertyIds)
                    } else {
                        Log.e("CarPropertiesScreen", "Permission denied")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


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
        callback: CarPropertyManager.CarPropertyEventCallback,
        propertyIds: List<Int>
    ) {
        propertyIds.forEach {
            carPropertyManager?.registerCallback(
                callback,
                it,
                CarPropertyManager.SENSOR_RATE_NORMAL
            )
        }
    }

    private fun createRow(property: Int, value: Any): Row {
        return when (property) {
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                Row.Builder()
                    .setTitle("Current speed")
                    .addText("$value kmph")
                    .build()
            }

            VehiclePropertyIds.CURRENT_GEAR -> {
                Row.Builder()
                    .setTitle("Current Gear")
                    .addText(value.toString())
                    .build()
            }

            VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                Row.Builder()
                    .setTitle("Current EV Battery level")
                    .addText("$value")
                    .build()
            }

            VehiclePropertyIds.FUEL_LEVEL -> {
                Row.Builder()
                    .setTitle("Current Fuel level")
                    .addText("$value")
                    .build()
            }

            VehiclePropertyIds.IGNITION_STATE -> {
                Row.Builder()
                    .setTitle("Ignition State")
                    .addText("$value")
                    .build()
            }

            else -> throw IllegalArgumentException("Invalid property ID: $property")
        }
    }
}