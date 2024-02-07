package com.akshay.weatherapp.templates

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarText
import androidx.car.app.model.Header
import androidx.car.app.model.OnClickListener
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.RoutePreviewNavigationTemplate
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants
import com.akshay.weatherapp.common.TemplateUtility.createGenericAction
import com.akshay.weatherapp.common.TemplateUtility.getIconByResource
import com.akshay.weatherapp.common.Utility.Companion.showErrorMessage
import com.akshay.weatherapp.misc.RoutingMapAction
import com.akshay.weatherapp.misc.SamplePlaces

/**
 * RoutePreviewTemplateFragment displays up to 3 available routes for a selected destination,
 * along with a visual representation on the map.
 *
 * **Requirements:**
 * - Header and optional Action strip in a card layout.
 * - List rows displaying route information (duration or distance).
 * - Base map drawn by the app in full-screen mode.
 * - Optional map action strip with up to 4 buttons for interactivity.
 *
 * **UX Guidelines:**
 * - Must present at least 1 route, with 1 route selected by default.
 * - Show either duration or distance for routes.
 * - Do not present more than 3 routes.
 * - Highlight the selected route on the map for user clarity.
 *
 * Note: This template is exclusively for navigation apps.
 */
class RouteTemplateExample(carContext: CarContext) : Screen(carContext) {
    private val samplePlaces = SamplePlaces.create(this, Constants.ROUTE_PREVIEW_TEMPLATE)
    private var mIsFavorite: Boolean = false

    /**
     * - CAUTION: All rows must have either a distance or duration span attached to either its title or texts
     * - ERROR: java.lang.IllegalArgumentException: All rows must have either a distance or duration span attached to either its title or texts
     *
     * The number of items in the ItemList should be smaller or equal than the limit provided by ConstraintManager.CONTENT_LIMIT_TYPE_ROUTE_LIST
     * Host will ignore any items over that limit also it must contain onClick listener set.
     */
    override fun onGetTemplate(): Template {
        /**
         * Only CarSpan type spans are allowed in a CarText, other spans will be
         * removed from the provided CharSequence.
         * The text variants should be added in order of preference, from most to least
         * preferred (for instance, from longest to shortest). If the text provided via
         * Builder does not fit in the screen, the host will display the
         * first variant that fits in the screen.
         * For instance, if the variant order is ["long string", "shorter", "short"], and the
         * screen can fit 7 characters, "shorter" will be chosen. However, if the order is
         * ["short", "shorter", "long string"], "short" will be chosen, because "short" fits
         * within the 7 character limit.
         */
        val navigateActionText =
            CarText.Builder(carContext.getString(R.string.continue_start_nav))
                .addVariant(carContext.getString(R.string.continue_route))
                .build()

        val secondEndHeaderAction = createGenericAction(
            icon = getIconByResource(R.drawable.ic_close_white_24dp, carContext),
            onClickListener = OnClickListener { finish() }
        )

        val firstEndHeaderAction = createGenericAction(
            icon = getIconByResource(
                if (mIsFavorite) R.drawable.ic_favorite_filled_white_24dp
                else R.drawable.ic_favorite_white_24dp, carContext
            ),
            onClickListener = OnClickListener {
                showErrorMessage(
                    carContext,
                    if (mIsFavorite) carContext.getString(R.string.removed_from_favourites)
                    else carContext.getString(R.string.added_to_favourites),
                )
                mIsFavorite = !mIsFavorite
                invalidate()
            }
        )

        val navigationAction = Action.Builder()
            .apply {
                setTitle(navigateActionText)
                setOnClickListener {
                    showErrorMessage(
                        carContext,
                        carContext.getString(R.string.nav_requested_toast_msg)
                    )
                }
            }

        /**
         * NOTE: Header can have maximum of 2 actions
         * ERROR: java.lang.IllegalArgumentException: Action list exceeded max number of 2 actions
         */
        val header = Header.Builder()
            .run {
                setStartHeaderAction(Action.BACK)
                addEndHeaderAction(firstEndHeaderAction)
                addEndHeaderAction(secondEndHeaderAction)
                setTitle(carContext.getString(R.string.navigate))
                build()
            }
        /**
         * The template itself does not expose a drawing surface. In order to draw on the canvas, use
         * androidx.car.app.AppManager.setSurfaceCallback(SurfaceCallback).
         * Navigation list can only have 3 items if we add more than that it will be ignored (for ex: based on car location we can show 3 places0
         * App should not use this template to continuously refresh the routes as the car moves.
         */
        return RoutePreviewNavigationTemplate.Builder()
            .run {
                setItemList(samplePlaces.getPlaceList())
                setNavigateAction(navigationAction.build())
                setMapActionStrip(RoutingMapAction.getMapActionStrip(carContext))
                setHeader(header)
                build()
            }
    }
}