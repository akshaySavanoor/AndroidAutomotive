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

    fun setFuelLevel(value: Float) {
        repository.setFuelLevel(value)
    }

    fun fetchFuelLevel(): Float {
        return repository.fetchFuelLevel()
    }

    fun setIgnitionState(value: Int) {
        repository.setIgnitionState(value)
    }

    fun fetchIgnitionState(): String {
        return repository.fetchIgnitionState()
    }

    fun setSpeedInKmph(value: Float) {
        repository.setSpeedInKmph(value)
    }

    fun fetchSpeedInKmph(): Float {
        return repository.fetchSpeedInKmph()
    }

    fun setCurrentGear(value: Int) {
        repository.setCurrentGear(value)
    }

    fun fetchCurrentGear(): String {
        return repository.fetchCurrentGear()
    }
}