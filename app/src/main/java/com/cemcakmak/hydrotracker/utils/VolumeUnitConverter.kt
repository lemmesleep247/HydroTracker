package com.cemcakmak.hydrotracker.utils

import android.content.Context
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.models.VolumeUnit
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Converts hydration amounts between millilitres (the internal unit) and the user's preferred
 * display unit. All calculations and database storage continue to use millilitres.
 */
object VolumeUnitConverter {

    /** Converts a value in the given [unit] to millilitres. */
    fun toMillilitres(value: Double, unit: VolumeUnit): Double {
        return value * unit.toMillilitresFactor
    }

    /** Converts a value in millilitres to the given [unit]. */
    fun fromMillilitres(millilitres: Double, unit: VolumeUnit): Double {
        return millilitres / unit.toMillilitresFactor
    }

    /** Formats an amount in millilitres for display in the given [unit]. */
    fun format(context: Context, millilitres: Double, unit: VolumeUnit): String {
        val converted = fromMillilitres(millilitres, unit)
        val shortLabel = context.getString(unit.shortLabelResId)
        return when (unit) {
            VolumeUnit.MILLILITRES -> {
                // Compact to litres at or above 1 L, matching the app's established display convention.
                if (millilitres >= 1000.0) {
                    val litres = NumberFormat.getNumberInstance(Locale.getDefault())
                        .apply { maximumFractionDigits = 1 }
                        .format(millilitres / 1000.0)
                    context.getString(R.string.unit_liters_format, litres)
                } else {
                    "${converted.roundToInt()} $shortLabel"
                }
            }
            else -> "%.1f $shortLabel".format(Locale.getDefault(), converted)
        }
    }

    /** Formats an amount in millilitres without the unit label. */
    fun formatValue(millilitres: Double, unit: VolumeUnit): String {
        val converted = fromMillilitres(millilitres, unit)
        return when (unit) {
            VolumeUnit.MILLILITRES -> converted.roundToInt().toString()
            else -> "%.1f".format(Locale.getDefault(), converted)
        }
    }

    /**
     * Infers a sensible default display unit from a locale.
     * The United States defaults to US fluid ounces; the United Kingdom defaults to Imperial fluid
     * ounces; all other locales default to millilitres.
     */
    fun defaultUnitForLocale(locale: Locale = Locale.getDefault()): VolumeUnit {
        return when (locale.country.uppercase()) {
            "US" -> VolumeUnit.US_FLUID_OUNCE
            "GB", "UK" -> VolumeUnit.IMPERIAL_FLUID_OUNCE
            else -> VolumeUnit.MILLILITRES
        }
    }
}
