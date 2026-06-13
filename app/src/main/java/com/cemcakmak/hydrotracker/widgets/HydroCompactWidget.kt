package com.cemcakmak.hydrotracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cemcakmak.hydrotracker.MainActivity
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.database.DatabaseInitializer
import com.cemcakmak.hydrotracker.utils.VolumeUnitConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * HydroTracker Compact Widget (1x1)
 * Shows progress in a compact circular format
 */
class HydroCompactWidget : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateService.scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateService.cancelUpdates(context)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                val userRepository = UserRepository(context)
                val waterRepository = DatabaseInitializer.getWaterIntakeRepository(context, userRepository)
                
                val progress = waterRepository.getTodayProgress().first()
                val userProfile = userRepository.userProfile.first()
                val themePreferences = userRepository.themePreferences.first()
                val volumeUnit = userProfile?.volumeUnit
                    ?: com.cemcakmak.hydrotracker.data.models.VolumeUnit.MILLILITRES

                val views = RemoteViews(context.packageName, R.layout.widget_hydro_compact)

                // Update progress text - compact format
                val currentText = formatCompact(context, progress.currentIntake, volumeUnit)
                val goalText = userProfile?.let {
                    formatCompact(context, it.dailyWaterGoal, volumeUnit)
                } ?: formatCompact(context, 2700.0, volumeUnit)
                val progressText = "$currentText / $goalText"
                
                views.setTextViewText(R.id.widget_progress_text, progressText)
                
                // Update progress percentage
                val progressPercent = (progress.progress * 100).toInt()
                views.setTextViewText(
                    R.id.widget_progress_percent,
                    context.getString(R.string.percent_format, progressPercent)
                )
                
                // Update time - compact format
                val lastUpdatedTime = java.time.Instant.ofEpochMilli(System.currentTimeMillis())
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                views.setTextViewText(
                    R.id.widget_last_updated,
                    context.getString(
                        R.string.widget_updated_at,
                        com.cemcakmak.hydrotracker.utils.DateTimeFormatters.formatTime(
                            context,
                            lastUpdatedTime,
                            themePreferences.timeFormat
                        )
                    )
                )
                
                // Set circular progress
                views.setProgressBar(R.id.widget_progress_circle, 100, progressPercent, false)
                
                // Set click intent
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                // Apply Material 3 theme
                WidgetTheme.applyTheme(
                    context = context,
                    views = views,
                    textViewIds = listOf(R.id.widget_progress_text, R.id.widget_last_updated),
                    accentTextViewIds = listOf(R.id.widget_progress_percent)
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)

            } catch (_: Exception) {
                updateWidgetWithDefaults(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun updateWidgetWithDefaults(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_hydro_compact)
        
        val defaultUnit = com.cemcakmak.hydrotracker.data.models.VolumeUnit.MILLILITRES
        views.setTextViewText(
            R.id.widget_progress_text,
            "${formatCompact(context, 0.0, defaultUnit)} / ${formatCompact(context, 2700.0, defaultUnit)}"
        )
        views.setTextViewText(
            R.id.widget_progress_percent,
            context.getString(R.string.percent_format, 0)
        )
        views.setTextViewText(R.id.widget_last_updated, context.getString(R.string.widget_tap_to_open))
        views.setProgressBar(R.id.widget_progress_circle, 100, 0, false)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun formatCompact(
        context: Context,
        amount: Double,
        volumeUnit: com.cemcakmak.hydrotracker.data.models.VolumeUnit
    ): String {
        return VolumeUnitConverter.format(context, amount, volumeUnit)
    }
    
}