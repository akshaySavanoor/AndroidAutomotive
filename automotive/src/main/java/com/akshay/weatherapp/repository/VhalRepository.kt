package com.akshay.weatherapp.repository

import android.car.VehicleGear
import android.car.VehicleIgnitionState
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import androidx.car.app.CarContext
import com.akshay.weatherapp.R

class VhalRepository(
    carPropertyManager: CarPropertyManager,
    val carContext: CarContext
) {
    private var speed = carPropertyManager.getFloatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED,0)
    private var currentGear = carPropertyManager.getIntProperty(VehiclePropertyIds.CURRENT_GEAR, 0)
    private var evBatteryLevel = carPropertyManager.getFloatProperty(VehiclePropertyIds.EV_BATTERY_LEVEL, 0)
    private var fuelLevel = carPropertyManager.getFloatProperty(VehiclePropertyIds.FUEL_LEVEL, 0)
    private var ignitionState = carPropertyManager.getIntProperty(VehiclePropertyIds.IGNITION_STATE, 0)

    fun setEvBatteryLevel(value: Float) {
        evBatteryLevel = value
    }

    fun fetchEvBatteryLevel(): Float {
        return evBatteryLevel
    }

    fun checkEvBatteryLevelChanged(newVal:Float):Boolean{
        return newVal != evBatteryLevel
    }

    fun setFuelLevel(value: Float) {
        fuelLevel = value
    }

    fun fetchFuelLevel(): Float {
        return fuelLevel
    }

    fun checkFuelLevelChanged(newVal: Float):Boolean{
        return newVal != fuelLevel
    }

    fun setIgnitionState(value: Int) {
        ignitionState = value
    }

    fun fetchIgnitionState(): String {
        return when (ignitionState) {
            VehicleIgnitionState.ON -> carContext.getString(R.string.on)
            VehicleIgnitionState.OFF -> carContext.getString(R.string.off)
            VehicleIgnitionState.ACC -> carContext.getString(R.string.acc)
            VehicleIgnitionState.LOCK -> carContext.getString(R.string.lock)
            VehicleIgnitionState.START -> carContext.getString(R.string.start)
            VehicleIgnitionState.UNDEFINED -> carContext.getString(R.string.undefined)
            else -> {
                carContext.getString(R.string.empty_string)
            }
        }
    }

    fun checkIgnitionStateChanged(newVal: Int):Boolean{
        return newVal != ignitionState
    }

    fun setSpeedInKmph(value: Float) {
        speed = value
    }

    fun fetchSpeedInKmph(): Float {
        return speed * 3.6f
    }

    fun checkSpeedChanged(newVal: Float):Boolean{
        return newVal != speed
    }

    fun setCurrentGear(value: Int) {
        currentGear = value
    }

    fun fetchCurrentGear(): String {
        return when (currentGear) {
            VehicleGear.GEAR_NEUTRAL -> carContext.getString(R.string.n)
            VehicleGear.GEAR_REVERSE -> carContext.getString(R.string.r)
            VehicleGear.GEAR_PARK -> carContext.getString(R.string.p)
            VehicleGear.GEAR_DRIVE -> carContext.getString(R.string.d)
            VehicleGear.GEAR_SECOND -> carContext.getString(R.string.d2)
            VehicleGear.GEAR_THIRD -> carContext.getString(R.string.d3)
            VehicleGear.GEAR_FOURTH -> carContext.getString(R.string.d4)
            VehicleGear.GEAR_FIFTH -> carContext.getString(R.string.d5)
            VehicleGear.GEAR_SIXTH -> carContext.getString(R.string.d6)
            VehicleGear.GEAR_SEVENTH -> carContext.getString(R.string.d7)
            else -> {
                carContext.getString(R.string.p)
            }
        }
    }
    fun checkCurrentGearChanged(newVal: Int):Boolean{
        return newVal != currentGear
    }
}