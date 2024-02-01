package com.akshay.weatherapp.repository

import android.car.VehicleGear
import android.car.VehicleIgnitionState
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager

class VhalRepository(
    carPropertyManager: CarPropertyManager
) {
    private var speed: Float = 0.0f
    private var currentGear: Int = 0
    private var evBatteryLevel: Float = 0.0f
    private var fuelLevel: Float = 0.0f
    private var ignitionState: Int = VehicleIgnitionState.ON

    init {
        carPropertyManager.apply {
            speed = getFloatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, 0)
            currentGear = getIntProperty(VehiclePropertyIds.CURRENT_GEAR, 0)
            evBatteryLevel = getFloatProperty(VehiclePropertyIds.EV_BATTERY_LEVEL, 0)
            fuelLevel = getFloatProperty(VehiclePropertyIds.FUEL_LEVEL, 0)
            ignitionState = getIntProperty(VehiclePropertyIds.IGNITION_STATE, 0)
        }
    }

    fun setEvBatteryLevel(value: Float) {
        evBatteryLevel = value
    }

    fun fetchEvBatteryLevel(): Float {
        return evBatteryLevel
    }

    fun setFuelLevel(value: Float) {
        fuelLevel = value
    }

    fun fetchFuelLevel(): Float {
        return fuelLevel
    }

    fun setIgnitionState(value: Int) {
        ignitionState = value
    }

    fun fetchIgnitionState(): String {
        return when (ignitionState) {
            VehicleIgnitionState.ON -> "ON"
            VehicleIgnitionState.OFF -> "OFF"
            VehicleIgnitionState.ACC -> "ACC"
            VehicleIgnitionState.LOCK -> "LOCK"
            VehicleIgnitionState.START -> "START"
            VehicleIgnitionState.UNDEFINED -> "UNDEFINED"

            else -> {
                ""
            }
        }
    }

    fun setSpeedInKmph(value: Float) {
        speed = value
    }

    fun fetchSpeedInKmph(): Float {
        return speed * 3.6f
    }

    fun setCurrentGear(value: Int) {
        currentGear = value
    }

    fun fetchCurrentGear(): String {
        return when (currentGear) {
            VehicleGear.GEAR_NEUTRAL -> "N"
            VehicleGear.GEAR_REVERSE -> "R"
            VehicleGear.GEAR_PARK -> "P"
            VehicleGear.GEAR_DRIVE -> "D"
            VehicleGear.GEAR_SECOND -> "D2"
            VehicleGear.GEAR_THIRD -> "D3"
            VehicleGear.GEAR_FOURTH -> "D4"
            VehicleGear.GEAR_FIFTH -> "D5"
            VehicleGear.GEAR_SIXTH -> "D6"
            VehicleGear.GEAR_SEVENTH -> "D7"
            else -> {
                "P"
            }
        }
    }
}