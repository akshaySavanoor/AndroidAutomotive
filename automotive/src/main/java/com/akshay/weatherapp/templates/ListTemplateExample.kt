package com.akshay.weatherapp.templates

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.text.SpannableString
import androidx.annotation.OptIn
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.annotations.ExperimentalCarApi
import androidx.car.app.model.*
import androidx.car.app.model.CarColor.YELLOW
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.HomeScreen
import com.akshay.weatherapp.R
import com.akshay.weatherapp.app_secrets.ApiKey
import com.akshay.weatherapp.common.Constants.Companion.CLOUD
import com.akshay.weatherapp.common.Constants.Companion.COORDINATES
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.SYSTEM_INFORMATION
import com.akshay.weatherapp.common.Constants.Companion.TEMPERATURE
import com.akshay.weatherapp.common.Constants.Companion.WEATHER_CONDITION
import com.akshay.weatherapp.common.Constants.Companion.WIND
import com.akshay.weatherapp.common.Utility
import com.akshay.weatherapp.common.Utility.Companion.colorize
import com.akshay.weatherapp.common.Utility.Companion.requestPermission
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.model.WeatherResponse
import com.akshay.weatherapp.service.RetrofitInstance
import com.akshay.weatherapp.ui.MyLocationListener
import com.akshay.weatherapp.ui.WeatherDetailsScreen
import com.akshay.weatherapp.viewmodel.WeatherViewModel
import retrofit2.Call


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
class ListTemplateExample(carContext: CarContext) : Screen(carContext) {

    private val weatherViewModel = WeatherViewModel()
    private var weatherResponseData: WeatherResponse? = null

    private lateinit var myLocationListener: MyLocationListener
    private var locationManager: LocationManager? = null
    private var hasPermissionLocation: Boolean = false
    private var currentLocation: Location? = null
    private var call: Call<WeatherResponse>? = null

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
     * Numeric decorations typically represent a quantity of unseen content. For example, a
     * decoration might represent a number of missed notifications, or a number of unread
     * messages in a conversation.(appears before Action view)
     */
    private fun createWeatherRow(title: String, icon: IconCompat): Row {
        val rowIcon = CarIcon.Builder(icon).build()
        val onClickListener: () -> Unit = {
            weatherResponseData?.let {
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
                setTitle(title)
                addText(
                    getColoredString(
                        carContext.getString(R.string.title_details, title),
                        mIsEnabled
                    )
                ) // Colored spannable cannot be applied to the title
                setEnabled(mIsEnabled)
            }
            .build()
    }

    private fun getColoredString(str: String, isEnabled: Boolean): CharSequence {
        if (isEnabled && str.isNotEmpty()) {
            val ss = SpannableString(str)
            colorize(ss, YELLOW, 0, str.length)
            return ss
        }
        return str
    }

    private fun setUpObserversAndCallApi() {
        weatherViewModel.apply {
            weatherData.observe(this@ListTemplateExample) { weatherResponse ->
                weatherResponseData = weatherResponse
                invalidate()
            }

            isLoading.observe(this@ListTemplateExample) {
                mIsLoading = it
                if (it) {
                    invalidate()
                }
            }
            mError.observe(this@ListTemplateExample) {
                errorMessage = it
                invalidate()
            }
        }

        myLocationListener = MyLocationListener { location ->
            if (location.latitude != currentLocation?.latitude && location.longitude != currentLocation?.longitude) {
                currentLocation = location
                call =
                    RetrofitInstance.weatherApiService.getCurrentWeather(
                        location.latitude,
                        location.longitude,
                        ApiKey.API_KEY
                    )
                weatherViewModel.fetchWeatherData(
                    carContext,
                    call ?: weatherViewModel.getDefaultCall()
                )
            }
        }

        locationManager = carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasPermissionLocation =
            ContextCompat.checkSelfPermission(
                carContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        carContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED

        if (hasPermissionLocation) {
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                myLocationListener
            )
        } else {
            requestPermission(carContext) { approved, rejected ->
                if (approved.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.approved))
                    invalidate()

                } else if (rejected.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.rejected))
                    screenManager.pop()
                }
            }
        }
    }

    /**
     * setOnSelectedListener{} adds a radio button to each list item.
     * CAUTION: When using setOnSelectedListener{}, toggling is not allowed.
     * ERROR: java.lang.IllegalStateException: Items in selectable lists cannot have a toggle.
     */
    @OptIn(ExperimentalCarApi::class)
    override fun onGetTemplate(): Template {
        setUpObserversAndCallApi()
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

        val goToHome = Action.Builder()
            .setTitle(carContext.getString(R.string.home))
            .setOnClickListener {
                screenManager.push(HomeScreen(carContext))
            }
            .build()

        val retryAction = Action.Builder()
            .setTitle(carContext.getString(R.string.retry))
            .setFlags(Action.FLAG_PRIMARY)
            .setOnClickListener {
                weatherViewModel.fetchWeatherData(
                    carContext,
                    call ?: weatherViewModel.getDefaultCall()
                )
                mIsLoading = true
                invalidate()
            }.build()

        errorMessage?.let {
            return MessageTemplate.Builder(it)
                .setTitle(LIST_TEMPLATE)
                .setIcon(CarIcon.ERROR)
                .setHeaderAction(Action.BACK)
                .setActionStrip(
                    ActionStrip.Builder()
                        .addAction(goToHome)
                        .build()
                )
                .addAction(retryAction)
                .build()
        }

        /**
         * Note: You cannot use both a sectioned list and a normal list simultaneously. If attempted, priority will be given to the last declared list.
         */
        return weatherListBuilder.apply {
            setTitle(LIST_TEMPLATE)
            setHeaderAction(Action.BACK)
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