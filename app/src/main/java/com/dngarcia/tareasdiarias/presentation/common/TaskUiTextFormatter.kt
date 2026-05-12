package com.dngarcia.tareasdiarias.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dngarcia.tareasdiarias.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val lastDoneFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
private val dueDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun formatLastCompletionLabel(lastCompletedAt: LocalDateTime?): String {
    return if (lastCompletedAt == null) {
        stringResource(id = R.string.task_last_done_never)
    } else {
        stringResource(
            id = R.string.task_last_done_value,
            lastCompletedAt.format(lastDoneFormatter),
        )
    }
}

@Composable
fun formatDueDateLabel(dueDate: LocalDate?): String {
    if (dueDate == null) {
        return stringResource(id = R.string.task_due_date_unknown)
    }
    val locale = Locale.getDefault()
    val dayName = dueDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { char -> char.titlecase(locale) }
    return stringResource(
        id = R.string.task_due_date_value,
        dayName,
        dueDate.format(dueDateFormatter),
    )
}
