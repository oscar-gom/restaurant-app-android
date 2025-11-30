package com.osg.restaurantcompanionapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, locale: Locale = Locale.getDefault()): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        return format.format(amount)
    }
}

