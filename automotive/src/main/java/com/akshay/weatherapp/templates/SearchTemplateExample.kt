package com.akshay.weatherapp.templates

import android.text.SpannableString
import android.text.Spanned
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.*
import androidx.lifecycle.DefaultLifecycleObserver
import com.akshay.weatherapp.ui.FilterForSearch
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Utility.Companion.toIntent
import com.akshay.weatherapp.model.Place
import com.akshay.weatherapp.viewmodel.WeatherViewModel

/**
 * Kotlin comment for SearchTemplate:
 *
 * The Search template facilitates user searches by presenting a comprehensive interface consisting of a search bar, keyboard, and results list, particularly for destination searches.
 *
 * Key Points:
 * 1. **During Drives:**
 *    - Users are restricted from accessing the keyboard for safety reasons.
 *    - The Search template allows users to view past or suggested search results during drives.
 *
 * 2. **Template Components:**
 *    - Search bar header featuring an optional action strip.
 *    - List rows for presenting search results within defined limits.
 *    - Keyboard functionality is available when the vehicle is parked, and apps can dynamically collapse or expand it.
 *
 * 3. **UX Requirements for Search Template:**
 *    - **MUST:** Update the list promptly when users input keywords.
 *    - **SHOULD:** Dynamically refresh content (screen refresh) exclusively to display search results during user input.
 *    - **SHOULD:** Display content or launch the keyboard (in the absence of content) when opening the template.
 *    - **MAY:** Display the keyboard in either expanded or collapsed state when users open the template in a parked state (keyboard unavailable during driving).
 *    - **MAY:** Provide hint text on the search bar.
 *    - **MAY:** Display a default list of past results or other relevant content.
 *
 * Developers should adhere to these UX requirements to ensure a seamless and user-friendly experience with the Search template.
 */

class SearchTemplateExample(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val locationViewModel = WeatherViewModel()
    private var searchResults: MutableSet<Place> = mutableSetOf()
    private var previousSearchResults: MutableSet<Place> = mutableSetOf()
    val randomPlace = locationViewModel.getLocationData()

    init {
        lifecycle.addObserver(this)
    }

    /**
     * The host may ignore the color specified in the ForegroundCarColorSpan and instead use
     * a default color.
     *
     * CAUTION: Avoid using ForegroundCarColorSpan for the title, as it may lead to a crash in the app.
     * ERROR: java.lang.IllegalArgumentException: CarSpan type is not allowed: ForegroundCarColorSpan
     */
    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
            .clearItems()
            .setNoItemsMessage(carContext.getString(R.string.no_places_to_show))

        searchResults.forEach { result ->
            val status =
                if (result.isOpen) carContext.getString(R.string.available) else carContext.getString(
                    R.string.closed
                )

            val statusWithDesc =
                SpannableString(carContext.getString(R.string.distance_with_availability, status))
            statusWithDesc.setSpan(
                DistanceSpan.create(
                    Distance.create(
                        result.distance,
                        Distance.UNIT_KILOMETERS
                    )
                ),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            statusWithDesc.setSpan(
                ForegroundCarColorSpan.create(CarColor.BLUE),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (result.isOpen) {
                statusWithDesc.setSpan(
                    ForegroundCarColorSpan.create(CarColor.GREEN),
                    2,
                    11,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                statusWithDesc.setSpan(
                    ForegroundCarColorSpan.create(CarColor.RED),
                    2,
                    8,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }


            listBuilder.addItem(
                Row.Builder()
                    .setTitle(result.name)
                    .addText(statusWithDesc)
                    .setBrowsable(true)
                    .setOnClickListener {
                        previousSearchResults.add(result)
                        carContext.startCarApp(randomPlace.toIntent(CarContext.ACTION_NAVIGATE))
                    }
                    .build()
            )
        }

        val searchListener = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                searchResults = mutableSetOf()
                if (searchText.isNotEmpty()) {
                    searchPlaces(searchText)
                    invalidate()
                } else {
                    searchResults = previousSearchResults
                    invalidate()
                }
            }

            override fun onSearchSubmitted(searchText: String) {
                if (searchText.isNotEmpty()) {
                    previousSearchResults.add(searchResults.first())
                    carContext.startCarApp(randomPlace.toIntent(CarContext.ACTION_NAVIGATE))
                }
            }
        }

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.filter))
                    .setOnClickListener {
                        screenManager.push(FilterForSearch(carContext))
                    }
                    .build()
            )
            .build()

        return SearchTemplate.Builder(searchListener)
            .setSearchHint(carContext.getString(R.string.search_hint))
            .setHeaderAction(Action.BACK)
//           .setInitialSearchText("puttur") //We can add optional initial search text instead of using hint
            .setShowKeyboardByDefault(false) // By default, setShowKeyboardByDefault is true, causing the keyboard to appear even if the user doesn't focus on the search.
            .setItemList(listBuilder.build())
            .setActionStrip(actionStrip)
            .build()

    }

    fun searchPlaces(query: String) {
        val newResults = locationViewModel.getGasStations()
            .filter { it.name.contains(query, ignoreCase = true) }
            .toSet()
        searchResults.addAll(newResults)
    }
}