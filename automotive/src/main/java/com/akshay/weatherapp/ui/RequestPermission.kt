package com.akshay.weatherapp.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_AUTOMOTIVE
import android.location.LocationManager
import android.provider.Settings
import androidx.car.app.CarAppPermission
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Action.FLAG_PRIMARY
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.core.location.LocationManagerCompat
import com.akshay.weatherapp.HomeScreen
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.Constants.Companion.PACKAGE_PREFIX
import com.akshay.weatherapp.common.Utility.Companion.showToast
import com.akshay.weatherapp.templates.GridTemplateExample
import com.akshay.weatherapp.templates.ListTemplateExample
import com.akshay.weatherapp.templates.LongMessageTemplateExample
import com.akshay.weatherapp.templates.MapTemplateExample
import com.akshay.weatherapp.templates.MessageTemplateExample
import com.akshay.weatherapp.templates.PaneTemplateExample
import com.akshay.weatherapp.templates.PlaceListMapExample
import com.akshay.weatherapp.templates.SearchTemplateExample
import com.akshay.weatherapp.templates.SignInTemplateExample

class RequestPermissionScreen(
    carContext: CarContext,
    private val mPreSeedMode: Boolean = false,
    private val currentScreen: String? = null
) : Screen(carContext) {

    /**
     * Action which invalidates the template.
     *
     * This can give the user a chance to revoke the permissions and then refresh will pick up
     * the permissions that need to be granted.
     */
    private val mRefreshAction: Action = Action.Builder()
        .setIcon(CarIcon.ALERT)
        .setTitle(getCarContext().getString(R.string.retry))
        .setFlags(FLAG_PRIMARY)
        .setOnClickListener { invalidate() }
        .build()

    override fun onGetTemplate(): Template {
        val headerAction: Action = if (mPreSeedMode) Action.APP_ICON else Action.BACK
        val permissions = ArrayList<String>()
        val declaredPermissions: Array<String>
        try {
            val info = carContext.packageManager.getPackageInfo(
                carContext.packageName,
                PackageManager.GET_PERMISSIONS
            )
            declaredPermissions = info.requestedPermissions ?: emptyArray()
        } catch (e: PackageManager.NameNotFoundException) {
            return MessageTemplate.Builder(carContext.getString(R.string.package_not_found))
                .setHeaderAction(headerAction)
                .addAction(mRefreshAction)
                .build()
        }

        for (declaredPermission in declaredPermissions) {
            // Exclude permissions related to the car app host as they are considered normal
            // but might appear as un granted by the system.
            if (declaredPermission.startsWith(PACKAGE_PREFIX)) {
                continue
            }
            try {
                CarAppPermission.checkHasPermission(carContext, declaredPermission)
            } catch (e: SecurityException) {
                permissions.add(declaredPermission)
            }
        }

        if (permissions.isEmpty()) {
            return MessageTemplate.Builder(
                carContext.getString(R.string.permission_already_granted)
            ).setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.ic_route
                    )
                ).build()
            )
                .setHeaderAction(headerAction)
                .addAction(
                    Action.Builder()
                        .setTitle(carContext.getString(R.string.ok))
                        .setOnClickListener { finish() }
                        .build()
                ).build()
        }

        val listener = ParkedOnlyOnClickListener.create {
            carContext.requestPermissions(
                permissions.toTypedArray().toMutableList()
            ) { approved, rejected ->
                if (approved.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.approved))
                    currentScreen?.let {
                        screenManager.pop()
                        when (currentScreen) {
                            Constants.LIST_TEMPLATE -> screenManager.push(
                                ListTemplateExample(
                                    carContext
                                )
                            )

                            Constants.GRID_TEMPLATE -> screenManager.push(
                                GridTemplateExample(
                                    carContext
                                )
                            )

                            Constants.MESSAGE_TEMPLATE -> screenManager.push(
                                MessageTemplateExample(
                                    carContext
                                )
                            )

                            Constants.LONG_MESSAGE_TEMPLATE -> screenManager.push(
                                LongMessageTemplateExample(
                                    carContext
                                )
                            )

                            Constants.PANE_TEMPLATE -> screenManager.push(
                                PaneTemplateExample(
                                    carContext
                                )
                            )

                            Constants.MAP_TEMPLATE -> screenManager.push(
                                MapTemplateExample(
                                    carContext
                                )
                            )

                            Constants.PLACE_LIST_MAP_TEMPLATE -> screenManager.push(
                                PlaceListMapExample(carContext)
                            )

                            Constants.SEARCH -> screenManager.push(SearchTemplateExample(carContext))
                            Constants.SIGN_IN_TEMPLATE -> screenManager.push(
                                SignInTemplateExample(
                                    carContext
                                )
                            )
                        }
                    } ?: screenManager.push(HomeScreen(carContext))
                } else if (rejected.isNotEmpty()) {
                    showToast(carContext, carContext.getString(R.string.rejected))
                    screenManager.push(HomeScreen(carContext))
                }
            }
            if (!carContext.packageManager.hasSystemFeature(FEATURE_AUTOMOTIVE)) {
                showToast(carContext, carContext.getString(R.string.phone_screen_permission_msg))
            }
        }

        val action = Action.Builder()
            .setTitle(carContext.getString(R.string.ok))
            .setFlags(FLAG_PRIMARY)
            .setOnClickListener(listener)
            .build()

        var action2: Action? = null
        val locationManager =
            carContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {

            action2 = Action.Builder()
                .setTitle(carContext.getString(R.string.mobile))
                .setOnClickListener(
                    ParkedOnlyOnClickListener.create {
                        carContext.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        )
                        if (!carContext.packageManager.hasSystemFeature(
                                FEATURE_AUTOMOTIVE
                            )
                        ) {
                            showToast(
                                carContext,
                                carContext.getString(
                                    R.string.phone_screen_permission_msg
                                )
                            )
                        }
                    }
                ).build()
        }

        val builder = MessageTemplate.Builder(carContext.getString(R.string.request_permission_msg))
            .setTitle(carContext.getString(R.string.enable_permission_title))
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        carContext,
                        R.drawable.ic_route
                    )
                ).build()
            )
            .addAction(action)
            .addAction(Action.Builder()
                .setTitle(carContext.getString(R.string.cancel))
                .setOnClickListener {
                    showToast(carContext, carContext.getString(R.string.permission_cancelled))
                    screenManager.push(HomeScreen(carContext))
                }
                .build())
        action2?.let { builder.setActionStrip(ActionStrip.Builder().addAction(it).build()) }
        return builder.build()
    }
}
