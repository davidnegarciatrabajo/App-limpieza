package com.dngarcia.tareasdiarias.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dngarcia.tareasdiarias.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ReminderTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionalReminderTimeField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onClearTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayValue = selectedTime?.format(ReminderTimeFormatter)
        ?: stringResource(id = R.string.task_time_none)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.task_field_time_optional)) },
            supportingText = {
                Text(
                    text = stringResource(
                        id = if (selectedTime == null) {
                            R.string.task_time_helper_none
                        } else {
                            R.string.task_time_helper_set
                        },
                    ),
                )
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = stringResource(
                        id = if (selectedTime == null) {
                            R.string.task_time_action_set
                        } else {
                            R.string.task_time_action_change
                        },
                    ),
                )
            }
            if (selectedTime != null) {
                TextButton(onClick = onClearTime) {
                    Text(text = stringResource(id = R.string.task_time_action_clear))
                }
            }
        }
    }

    if (showPicker) {
        val initialTime = selectedTime ?: LocalTime.now().withSecond(0).withNano(0)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(text = stringResource(id = R.string.task_time_dialog_title)) },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showPicker = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.task_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(text = stringResource(id = R.string.task_cancel))
                }
            },
        )
    }
}
