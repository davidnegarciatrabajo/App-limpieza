package com.dngarcia.tareasdiarias.presentation.common

import android.app.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dngarcia.tareasdiarias.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskPostponeDialog(
    taskName: String,
    onDismiss: () -> Unit,
    onPostpone: (LocalDate) -> Unit,
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var customDateToConfirm by remember { mutableStateOf<LocalDate?>(null) }
    var validationMessage by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.today_postpone_dialog_title, taskName)) },
        text = {
            androidx.compose.foundation.layout.Column {
                TextButton(onClick = { onPostpone(today.plusDays(1)) }) {
                    Text(text = stringResource(id = R.string.today_postpone_option_tomorrow))
                }
                TextButton(onClick = { onPostpone(today.plusWeeks(1)) }) {
                    Text(text = stringResource(id = R.string.today_postpone_option_next_week))
                }
                TextButton(onClick = { onPostpone(today.plusDays(3)) }) {
                    Text(text = stringResource(id = R.string.today_postpone_option_three_days))
                }
                TextButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                if (selectedDate.isAfter(today)) {
                                    customDateToConfirm = selectedDate
                                } else {
                                    validationMessage = R.string.today_postpone_custom_invalid_date
                                }
                            },
                            today.plusDays(1).year,
                            today.plusDays(1).monthValue - 1,
                            today.plusDays(1).dayOfMonth,
                        ).apply {
                            datePicker.minDate = Instant
                                .from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()))
                                .toEpochMilli()
                        }.show()
                    },
                ) {
                    Text(text = stringResource(id = R.string.today_postpone_option_custom))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.task_cancel))
            }
        },
    )

    customDateToConfirm?.let { selectedDate ->
        AlertDialog(
            onDismissRequest = { customDateToConfirm = null },
            title = { Text(text = stringResource(id = R.string.today_postpone_custom_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.today_postpone_custom_confirm_message,
                        selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPostpone(selectedDate)
                        customDateToConfirm = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.task_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { customDateToConfirm = null }) {
                    Text(text = stringResource(id = R.string.task_cancel))
                }
            },
        )
    }

    validationMessage?.let { messageResId ->
        AlertDialog(
            onDismissRequest = { validationMessage = null },
            title = { Text(text = stringResource(id = R.string.today_postpone_custom_invalid_title)) },
            text = { Text(text = stringResource(id = messageResId)) },
            confirmButton = {
                TextButton(onClick = { validationMessage = null }) {
                    Text(text = stringResource(id = R.string.task_confirm))
                }
            },
        )
    }
}
