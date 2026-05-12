package com.dngarcia.tareasdiarias.widget.today

import android.appwidget.AppWidgetManager
import android.os.Bundle

enum class TodayWidgetSizeMode {
    COMPACT,
    EXPANDED;

    companion object {
        fun fromOptions(options: Bundle?): TodayWidgetSizeMode {
            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
            val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
            return if (minWidth >= 180 && minHeight >= 160) {
                EXPANDED
            } else {
                COMPACT
            }
        }

        fun fromStoredValue(rawValue: String?): TodayWidgetSizeMode {
            return entries.firstOrNull { it.name == rawValue } ?: COMPACT
        }
    }
}
