package com.akshay.weatherapp.misc

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.constraints.ConstraintManager.CONTENT_LIMIT_TYPE_GRID
import androidx.car.app.constraints.ConstraintManager.CONTENT_LIMIT_TYPE_LIST
import androidx.car.app.constraints.ConstraintManager.CONTENT_LIMIT_TYPE_PANE
import androidx.car.app.constraints.ConstraintManager.CONTENT_LIMIT_TYPE_PLACE_LIST
import androidx.car.app.constraints.ConstraintManager.CONTENT_LIMIT_TYPE_ROUTE_LIST
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.akshay.weatherapp.R
import com.akshay.weatherapp.common.Constants.Companion.GRID_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.LIST_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.PANE_TEMPLATE
import com.akshay.weatherapp.common.Constants.Companion.PLACE_LIST
import com.akshay.weatherapp.common.Constants.Companion.ROUTE_LIST

/**
 * Depending on the host the app is connected to, there could be different various limits that
 * apply, such as the number of items that could be in a list in different templates.
 */
class TemplateRestrictionUi(carContext: CarContext) : Screen(carContext) {
    private val contentLimitedTemplates = listOf(
        CONTENT_LIMIT_TYPE_LIST,
        CONTENT_LIMIT_TYPE_GRID,
        CONTENT_LIMIT_TYPE_PLACE_LIST,
        CONTENT_LIMIT_TYPE_ROUTE_LIST,
        CONTENT_LIMIT_TYPE_PANE
    )

    override fun onGetTemplate(): Template {
        val listItemBuilder =
            ItemList.Builder().setNoItemsMessage(carContext.getString(R.string.no_data_found))

        contentLimitedTemplates.forEachIndexed { index, contentType ->
            val templateName = when (index) {
                0 -> LIST_TEMPLATE
                1 -> GRID_TEMPLATE
                2 -> PLACE_LIST
                3 -> ROUTE_LIST
                4 -> PANE_TEMPLATE
                else -> return@forEachIndexed
            }

            val contentLimit =
                carContext.getCarService(ConstraintManager::class.java).getContentLimit(contentType)

            listItemBuilder.addItem(createRowFromContentLimitData(templateName, contentLimit))
        }

        return ListTemplate.Builder()
            .setHeaderAction(Action.BACK)
            .setTitle(carContext.getString(R.string.template_restriction))
            .setSingleList(listItemBuilder.build())
            .build()
    }

    private fun createRowFromContentLimitData(currentTemplate: String, contentLimit: Int): Row {
        return Row.Builder()
            .setTitle(currentTemplate)
            .addText(carContext.getString(R.string.content_limit, contentLimit))
            .build()
    }
}
