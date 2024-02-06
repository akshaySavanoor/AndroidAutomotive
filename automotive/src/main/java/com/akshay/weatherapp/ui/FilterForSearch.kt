package com.akshay.weatherapp.ui

import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.CASH
import com.akshay.weatherapp.common.Constants.Companion.CREDIT_CARD
import com.akshay.weatherapp.common.Constants.Companion.MOBILE_PAYMENT
import com.akshay.weatherapp.common.Constants.Companion.WITHIN_FIVE
import com.akshay.weatherapp.common.Constants.Companion.WITHIN_TEN
import com.akshay.weatherapp.common.Constants.Companion.WITHIN_TWENTY
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage

/**
 * IMPORTANT: Avoid using setOnChangedListener with Radio buttons when dealing with multiple sectioned lists.
 * ERROR: A selectable list cannot be added alongside any other lists.
 *
 * NOTE: If you're using text for the action stripe, spacing may be ignored based on the host.
 *
 * IMPORTANT: Cannot use addAction in list template.
 * ERROR: java.lang.IllegalArgumentException: Action list exceeded the maximum number of 0 actions with custom titles.
 */

class FilterForSearch(carContext: CarContext) : Screen(carContext) {
    private var flag = ""
    private val selectedPaymentList = mutableListOf<String>()
    private val distanceListBuilder =
        ItemList.Builder().setNoItemsMessage(carContext.getString(R.string.no_data_found))
    private val paymentListBuilder =
        ItemList.Builder().setNoItemsMessage(carContext.getString(R.string.no_data_found))

    private fun createWeatherRow(title: String, currentOption: String): Row {
        if (currentOption in listOf(CREDIT_CARD, MOBILE_PAYMENT, CASH)) {
            return Row.Builder()
                .setToggle(
                    Toggle.Builder {
                        if (it) {
                            selectedPaymentList.add(currentOption)
                        }
                    }.setChecked(
                        selectedPaymentList.contains(currentOption)
                    ).build()
                )
                .setTitle(title)
                .build()
        }
        return Row.Builder()
            .setToggle(
                Toggle.Builder {
                    if (it) {
                        flag = currentOption
                        invalidate()
                    }
                }.setChecked(flag == currentOption).build()
            )
            .setTitle(title)
            .build()
    }

    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        distanceListBuilder.apply {
            clearItems()
            addItem(createWeatherRow(carContext.getString(R.string.within_5_km), WITHIN_FIVE))
            addItem(createWeatherRow(carContext.getString(R.string.within_10_km), WITHIN_TEN))
            addItem(createWeatherRow(carContext.getString(R.string.within_20_km), WITHIN_TWENTY))
        }

        paymentListBuilder.apply {
            clearItems()
            addItem(
                createWeatherRow(
                    carContext.getString(R.string.credit_card_accepted),
                    CREDIT_CARD
                )
            )
            addItem(
                createWeatherRow(
                    carContext.getString(R.string.mobile_payment_supported),
                    MOBILE_PAYMENT
                )
            )
            addItem(createWeatherRow(carContext.getString(R.string.cash_accepted), CASH))
        }

        val distanceSectionedList = SectionedItemList.create(
            distanceListBuilder.build(),
            carContext.getString(R.string.distance)
        )
        val paymentSectionedList = SectionedItemList.create(
            paymentListBuilder.build(),
            carContext.getString(R.string.payment_options)
        )

        return ListTemplate.Builder().run {
            clearSectionedLists()
            setTitle(carContext.getString(R.string.filter_gas_stations))
            setHeaderAction(Action.BACK)
            setActionStrip(ActionStrip.Builder()
                .addAction(
                    Action.Builder()
                        .setTitle(carContext.getString(R.string.apply))
                        .setOnClickListener {
                            showErrorMessage(carContext, carContext.getString(R.string.filter_applied))
                            screenManager.pop()

                        }
                        .build()
                ).build())
            addSectionedList(distanceSectionedList)
            addSectionedList(paymentSectionedList)
            build()
        }
    }
}
