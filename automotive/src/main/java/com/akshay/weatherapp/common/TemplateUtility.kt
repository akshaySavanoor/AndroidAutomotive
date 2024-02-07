package com.akshay.weatherapp.common

import androidx.annotation.DrawableRes
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.OnClickListener
import androidx.core.graphics.drawable.IconCompat
import com.akshay.weatherapp.R

object TemplateUtility {

    fun goToHome(carContext: CarContext, screen: Screen): ActionStrip {
        return createGenericActionStrip(createGenericAction(
            title = carContext.getString(R.string.home),
            onClickListener = { screen.screenManager.popToRoot() }
        )
        )
    }

    fun getIconCompatByResource(@DrawableRes icon: Int, carContext: CarContext): IconCompat {
        return IconCompat.createWithResource(carContext, icon)
    }

    fun getIconByResource(@DrawableRes icon: Int, carContext: CarContext): CarIcon {
        return CarIcon.Builder(IconCompat.createWithResource(carContext, icon)).build()
    }

    fun createGenericAction(
        title: String? = null,
        flag: Int? = null,
        icon: CarIcon? = null,
        backgroundColor: CarColor? = null,
        onClickListener: OnClickListener
    ): Action {
        val builder = Action.Builder()
            .setOnClickListener(onClickListener)

        flag?.let {
            builder.setFlags(flag)
        }

        title?.let {
            builder.setTitle(it)
        }

        icon?.let {
            builder.setIcon(it)
        }

        backgroundColor?.let {
            builder.setBackgroundColor(backgroundColor)
        }

        return builder.build()
    }

    fun createGenericActionStrip(action: Action): ActionStrip {
        return ActionStrip.Builder()
            .addAction(action)
            .build()
    }

}