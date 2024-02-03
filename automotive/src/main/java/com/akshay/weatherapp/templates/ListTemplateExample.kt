package com.akshay.weatherapp.templates

import android.text.SpannableString
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.*
import androidx.car.app.model.CarColor.GREEN
import androidx.car.app.model.CarColor.YELLOW
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.CLOUD
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SYSTEM_INFORMATION
import com.akshay.weatherapp.common.Constants.Companion.TEMPERATURE
import com.akshay.weatherapp.common.Constants.Companion.WEATHER_CONDITION
import com.akshay.weatherapp.common.Constants.Companion.WIND
import com.akshay.weatherapp.common.RepositoryUtils
import com.akshay.weatherapp.common.RepositoryUtils.getRetryAction
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.colorize
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.model.WeatherResponseModel
import com.akshay.weatherapp.ui.WeatherDetailsScreen


/**
 * The List template is designed to present information items in a list layout.
 *
 * Key Features:
 * - Lists may include sections through sub lists.
 *
 * Structure:
 * - Header with an optional action strip .
 * - List items with a dynamic number and variable text per row:
 *    - The number of items depends on the vehicle, use ConstraintManager API to get the limit.
 *    - Secondary text can be longer than 2 rows when parked, check More list text while parked.
 * - Optional floating action button.
 *
 * Guidelines:
 * - Sections must include a section header.
 * - Avoid mixing selectable list rows (radio buttons) with other row types or separating them with sections.
 * - Default selections should be presented on selectable lists.
 * - Each list item should have an associated action (information-only rows are not recommended).
 * - Place content in secondary text intended for driving near the beginning to avoid truncation.
 * - Avoid including both an action strip and a floating action button simultaneously.
 * - You may divide list content into sections and mix rows with toggle switches as needed.
 * - Update row text, image, or icon asset when the user changes the toggle state.
 *
 * Note: The amount of secondary text allowed in each list row varies depending on driving status.
 * For safe driving, text is truncated to 2 lines while driving.
 * Ensure content meant to be read while driving is placed at the beginning of the secondary text.
 *
 * For content limit [check](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:car/app/app/src/main/res/values/integers.xml)
 *
 */
class ListTemplateExample(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private var weatherResponseModelData: WeatherResponseModel? = null
    private var mIsLoading = true
    private var errorMessage: String? = null
    private var mIsEnabled = true

    /**
     * Adds an extra action to the end of the row using addAction().
     * The background color of the action may or may not be displayed based on the host.

     * CAUTION: Avoid using both Toggle and addAction simultaneously to prevent app crashes.
     * ERROR: Caused by: java.lang.IllegalStateException - If a row contains a toggle, it must not have a secondary action set.

     * CAUTION: If setBrowsable is true, a ">" symbol will appear at the end of the row.
     * In this case, use setOnClickListener and avoid using Toggle or addAction.
     * ERROR: java.lang.IllegalStateException - A browsable row must have its onClickListener set.
     *
     * CAUTION: The number of lines of texts for the row cannot be more than 2.
     * ERROR: java.lang.IllegalArgumentException: The number of lines of texts for the row exceeded the supported max of 2
     *
     * Numeric decorations typically represent a quantity of unseen content. For example, a
     * decoration might represent a number of missed notifications, or a number of unread
     * messages in a conversation.(appears before Action view)
     *
     * Use a FAB for the most important action on the screen. Be sure that the icon is easy to understand, since there is no text label.
     */
    private fun createWeatherRow(title: String, icon: IconCompat): Row {
        val rowIcon = CarIcon.Builder(icon).build()
        val onClickListener: () -> Unit = {
            weatherResponseModelData?.let {
                screenManager.push(WeatherDetailsScreen(carContext, it, title))
            } ?: run {
                Utility.showErrorMessage(
                    carContext,
                    carContext.getString(R.string.failed_to_fetch_weather_data)
                )
            }
        }
        return Row.Builder()
            .apply {
                setImage(rowIcon)
                setBrowsable(true)
//              setOnClickListener{}
//              setToggle(Toggle.Builder {
//                if (it) {
//                    println("On")
//                } else {
//                    println("Off")
//                }
//            }
//                .setChecked(false)
//                .build())
//             addText("additional text")
//             addAction(
//                Action.Builder()
//                    .setIcon(CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_arrow_icon))
//                .build())
//                    .setOnClickListener{}
//                    .build()
//            )
//            setNumericDecoration(4)
                setOnClickListener(ParkedOnlyOnClickListener.create {
                    onClickListener()
                }) //Items that belong to selectable lists can't have an onClickListener.
                setTitle(title) // Colored spannable cannot be applied to the title
                addText(
                    getColoredString(
                        carContext.getString(R.string.title_details, title),
                        mIsEnabled
                    )
                )
                addText(
                    getColoredString(
                        carContext.getString(R.string.optional_text),
                        mIsEnabled,
                        GREEN
                    )
                )
                setEnabled(mIsEnabled)
            }
            .build()
    }

    private fun getColoredString(
        str: String,
        isEnabled: Boolean,
        color: CarColor = YELLOW
    ): CharSequence {
        if (isEnabled && str.isNotEmpty()) {
            val ss = SpannableString(str)
            colorize(ss, color, 0, str.length)
            return ss
        }
        return str
    }

    private val loadingCallback: (Boolean) -> Unit = { isLoading ->
        mIsLoading = isLoading
        if (isLoading) {
            invalidate()
        }
    }

    private val errorCallback: (String?) -> Unit = { errorData ->
        errorMessage = errorData
        invalidate()
    }

    private val weatherDataCallback: (WeatherResponseModel?) -> Unit = { weatherResponse ->
        weatherResponseModelData = weatherResponse
        mIsLoading = false
        errorMessage = null
        invalidate()
    }

    /**
     * setOnSelectedListener{} adds a radio button to each list item.
     * CAUTION: When using setOnSelectedListener{}, toggling is not allowed.
     * ERROR: java.lang.IllegalStateException: Items in selectable lists cannot have a toggle.
     */
    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        RepositoryUtils.setUpObserversAndCallApi(
            carContext,
            this,
            loadingCallback,
            errorCallback,
            weatherDataCallback
        )
//        if (carContext.carAppApiLevel > CarAppApiLevels.LEVEL_1) {
//            val listLimit = Integer.min(
//                Constants.MAX_LIST_ITEMS,
//                carContext.getCarService(ConstraintManager::class.java).getContentLimit(
//                    ConstraintManager.CONTENT_LIMIT_TYPE_LIST
//                )
//            )
//            for (i in 2..listLimit) {
//                // For row text, set text variants that fit best in different screen sizes.
//                val secondTextStr = "second line text"
//                val secondText = CarText.Builder(
//                    "================= $secondTextStr ================"
//                )
//                    .addVariant(
//                        "--------------------- " + secondTextStr
//                                + " ----------------------"
//                    )
//                    .addVariant(secondTextStr)
//                    .build()
//                itemListBuilder.addItem(
//                    Row.Builder()
//                        .setOnClickListener { println("clicked content list $i") }
//                        .setTitle(
//                            "clicked $i"
//                        )
//                        .addText("first line")
//                        .addText(secondText)
//                        .build())
//            }
//        }
        val itemListBuilder = ItemList.Builder()
            .setNoItemsMessage(carContext.getString(R.string.no_data_found))

        itemListBuilder.apply {
            clearItems() // If an item has associated actions, it is advisable to clear the items to avoid duplications.
            // Some hosts may allow more items in the list than others, so create more.
            addItem(
                createWeatherRow(
                    COORDINATES,
                    IconCompat.createWithResource(carContext, R.drawable.ic_coordinate)
                )
            )
            addItem(
                createWeatherRow(
                    WEATHER_CONDITION,
                    IconCompat.createWithResource(carContext, R.drawable.ic_weather)
                )
            )
            addItem(
                createWeatherRow(
                    TEMPERATURE,
                    IconCompat.createWithResource(carContext, R.drawable.ic_temperature)
                )
            )
            addItem(
                createWeatherRow(
                    CLOUD,
                    IconCompat.createWithResource(carContext, R.drawable.ic_clouds)
                )
            )
            addItem(
                createWeatherRow(
                    WIND,
                    IconCompat.createWithResource(carContext, R.drawable.ic_wind)
                )
            )
            addItem(
                createWeatherRow(
                    SYSTEM_INFORMATION,
                    IconCompat.createWithResource(carContext, R.drawable.ic_system)
                )
            )
//            setSelectedIndex(3) //Used to select default item in the list
//            setOnSelectedListener {
//                println("$it selected")
//            }
        }
        val weatherListBuilder = ListTemplate.Builder()
        if (mIsLoading) {
            return weatherListBuilder.apply {
                setLoading(true)
                setTitle(LIST_TEMPLATE)
                setHeaderAction(Action.BACK)
            }
                .build()
        }

        errorMessage?.let {
            return MessageTemplate.Builder(it)
                .setTitle(LIST_TEMPLATE)
                .setIcon(CarIcon.ERROR)
                .setHeaderAction(Action.BACK)
                .setActionStrip(Utility.goToHome(carContext, this))
                .addAction(getRetryAction(carContext, this))
                .build()
        }

        /**
         * Note: You cannot use both a sectioned list and a normal list simultaneously. If attempted, priority will be given to the last declared list.
         *
         * FABs are supported by the Grid template and the List template.
         * Note: FAB and its color can be ignored by the host.
         */
        return weatherListBuilder.apply {
            setTitle(LIST_TEMPLATE)
            setHeaderAction(Action.BACK)
            addAction(Action.Builder()
                .setIcon(
                    CarIcon.Builder(
                        IconCompat.createWithResource(
                            carContext,
                            R.drawable.ic_add
                        )
                    ).build()

                )
                .setOnClickListener {
                    showToast(
                        carContext,
                        carContext.getString(R.string.floating_icon_pressed)
                    )
                }
                .setBackgroundColor(YELLOW)
                .build())
            setActionStrip(
                ActionStrip.Builder().addAction(
                    Action.Builder()
                        .setTitle(
                            carContext.getString(
                                if (mIsEnabled) R.string.enable_all_rows else R.string.disable_all_rows
                            )
                        )
                        .setOnClickListener {
                            showToast(
                                carContext,
                                carContext.getString(
                                    if (!mIsEnabled) R.string.interactive_mode_enabled else R.string.enabled_read_only_mode
                                )
                            )
                            mIsEnabled = !mIsEnabled
                            setLoading(true)
                            invalidate() // Invalidates the current template, triggering a call to onGetTemplate for rendering a new screen.
                        }
                        .build()
                ).build()
            )
            setSingleList(itemListBuilder.build())
        }.build()
    }
}