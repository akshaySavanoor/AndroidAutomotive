package com.akshay.weatherapp.viewmodel

import android.car.hardware.property.CarPropertyManager
import androidx.car.app.CarContext
import androidx.lifecycle.ViewModel
import com.akshay.weatherapp.repository.VhalRepository


class VehiclePropertiesViewModel(
    carPropertyManager: CarPropertyManager,
    carContext: CarContext
) : ViewModel() {

    private val repository = VhalRepository(carPropertyManager,carContext)

    fun setEvBatteryLevel(value: Float) {
        repository.setEvBatteryLevel(value)
    }

    fun fetchEvBatteryLevel(): Float {
        return repository.fetchEvBatteryLevel()
    }

    fun checkEvBatteryLevelChanged(newVal:Float): Boolean {
        return repository.checkEvBatteryLevelChanged(newVal)
    }

    fun setFuelLevel(value: Float) {
        repository.setFuelLevel(value)
    }

    fun fetchFuelLevel(): Float {
        return repository.fetchFuelLevel()
    }

    fun checkFuelLevelChanged(newVal:Float): Boolean {
        return repository.checkFuelLevelChanged(newVal)
    }

    fun setIgnitionState(value: Int) {
        repository.setIgnitionState(value)
    }

    fun fetchIgnitionState(): String {
        return repository.fetchIgnitionState()
    }

    fun checkIgnitionStateChanged(newVal:Int): Boolean {
        return repository.checkIgnitionStateChanged(newVal)
    }

    fun setSpeedInKmph(value: Float) {
        repository.setSpeedInKmph(value)
    }

    fun fetchSpeedInKmph(): Float {
        return repository.fetchSpeedInKmph()
    }

    fun checkSpeedChanged(newVal:Float): Boolean {
        return repository.checkSpeedChanged(newVal)
    }

    fun setCurrentGear(value: Int) {
        repository.setCurrentGear(value)
    }

    fun fetchCurrentGear(): String {
        return repository.fetchCurrentGear()
    }

    fun checkCurrentGearChanged(newVal:Int): Boolean {
        return repository.checkCurrentGearChanged(newVal)
    }
}