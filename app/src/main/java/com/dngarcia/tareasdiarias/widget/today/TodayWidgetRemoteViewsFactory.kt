package com.dngarcia.tareasdiarias.widget.today

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.usecase.TodayWidgetTask
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking

class TodayWidgetRemoteViewsFactory(
    private val context: Context,
    private val sizeMode: TodayWidgetSizeMode,
) : RemoteViewsService.RemoteViewsFactory {
    private var items: List<TodayWidgetTask> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TodayWidgetEntryPoint::class.java,
        )
        items = runBlocking {
            entryPoint.getTodayWidgetTasksUseCase()(LocalDateTime.now())
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items.getOrNull(position) ?: return RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        val layoutId = when (sizeMode) {
            TodayWidgetSizeMode.COMPACT -> R.layout.widget_today_item_compact
            TodayWidgetSizeMode.EXPANDED -> R.layout.widget_today_item_expanded
        }
        val actionIntent = Intent().apply {
            action = if (item.completedToday) {
                TodayWidgetIntentFactory.ACTION_UNDO
            } else {
                TodayWidgetIntentFactory.ACTION_COMPLETE
            }
            putExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, item.task.id)
        }
        val editIntent = Intent().apply {
            action = TodayWidgetIntentFactory.ACTION_EDIT
            putExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, item.task.id)
        }
        val postponeIntent = Intent().apply {
            action = TodayWidgetIntentFactory.ACTION_POSTPONE
            putExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, item.task.id)
        }

        return RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.widget_row_title, formatTitle(item))
            setTextViewText(
                R.id.widget_row_subtitle,
                item.task.subtitulo.ifBlank { item.task.notas.ifBlank { "" } },
            )
            setViewVisibility(
                R.id.widget_row_subtitle,
                if (item.task.subtitulo.isNotBlank() || item.task.notas.isNotBlank()) View.VISIBLE else View.GONE,
            )
            setTextViewText(R.id.widget_row_meta, formatMeta(item))
            setViewVisibility(R.id.widget_row_last_modified, View.GONE)
            setImageViewResource(
                R.id.widget_row_status_dot,
                when {
                    item.completedToday -> R.drawable.widget_status_ok
                    item.status == TaskStatus.VENCIDA -> R.drawable.widget_status_overdue
                    else -> R.drawable.widget_status_upcoming
                },
            )
            setImageViewResource(
                R.id.widget_row_check,
                if (item.completedToday) {
                    R.drawable.ic_widget_undo
                } else {
                    R.drawable.ic_widget_checkbox_empty
                },
            )
            setInt(
                R.id.widget_row_check,
                "setBackgroundResource",
                if (item.completedToday) {
                    R.drawable.bg_widget_today_check_done
                } else {
                    R.drawable.bg_widget_today_check_pending
                },
            )
            setTextViewText(
                R.id.widget_row_check_label,
                context.getString(
                    if (item.completedToday) {
                        R.string.widget_today_check_undo
                    } else {
                        R.string.widget_today_check_ready
                    },
                ),
            )
            setTextColor(
                R.id.widget_row_check_label,
                Color.parseColor(
                    if (item.completedToday) "#2C7E4B" else "#7A6141",
                ),
            )
            setTextViewText(
                R.id.widget_row_postpone,
                context.getString(R.string.today_action_postpone),
            )
            setViewVisibility(
                R.id.widget_row_postpone,
                if (item.completedToday) View.GONE else View.VISIBLE,
            )
            setOnClickFillInIntent(R.id.widget_row_check_container, actionIntent)
            setOnClickFillInIntent(R.id.widget_row_check, actionIntent)
            setOnClickFillInIntent(R.id.widget_row_edit, editIntent)
            setOnClickFillInIntent(R.id.widget_row_postpone, postponeIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2

    override fun getItemId(position: Int): Long = items.getOrNull(position)?.task?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun formatTitle(item: TodayWidgetTask): CharSequence {
        if (!item.completedToday) return item.task.nombre
        return SpannableString(item.task.nombre).apply {
            setSpan(StrikethroughSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                ForegroundColorSpan(Color.parseColor("#6E5F46")),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun formatMeta(item: TodayWidgetTask): String {
        val category = item.categoryName.ifBlank {
            context.getString(R.string.task_field_category)
        }
        val periodicity = periodicityLabel(item.task.tipoPeriodicidad)
        val status = if (item.completedToday) {
            context.getString(R.string.widget_today_done_today)
        } else {
            when (item.status) {
                TaskStatus.VENCIDA -> if ((item.daysDelta ?: 0L) == 0L) {
                    context.getString(R.string.widget_today_status_due_today)
                } else {
                    context.getString(
                        R.string.widget_today_status_overdue_days,
                        item.daysDelta ?: 0L,
                    )
                }
                TaskStatus.OK -> context.getString(R.string.widget_today_status_ok)
            }
        }
        return context.getString(
            R.string.widget_today_meta_format,
            category,
            periodicity,
            status,
        )
    }

    private fun periodicityLabel(periodicidad: Periodicidad): String = when (periodicidad) {
        Periodicidad.DIARIA -> context.getString(R.string.task_periodicity_daily)
        Periodicidad.SEMANAL -> context.getString(R.string.task_periodicity_weekly)
        Periodicidad.MENSUAL -> context.getString(R.string.task_periodicity_monthly)
        Periodicidad.SEMESTRAL -> context.getString(R.string.task_periodicity_semiannual)
        Periodicidad.PERSONALIZADA -> context.getString(R.string.task_periodicity_custom)
        Periodicidad.UNICA -> context.getString(R.string.task_periodicity_unique)
    }
}
