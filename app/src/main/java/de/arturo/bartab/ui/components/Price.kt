package de.arturo.bartab.ui.components

import java.text.NumberFormat
import java.util.Locale

fun Int.toEuroString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return format.format(this / 100.0)
}
