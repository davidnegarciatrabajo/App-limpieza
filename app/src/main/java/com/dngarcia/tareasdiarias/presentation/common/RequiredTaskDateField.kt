package com.dngarcia.tareasdiarias.presentation.common

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dngarcia.tareasdiarias.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val TaskDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun RequiredTaskDateField(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = selectedDate.format(TaskDateFormatter),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.task_field_start_date)) },
            supportingText = {
                Text(text = stringResource(id = R.string.task_start_date_helper))
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth,
                    ).show()
                },
            ) {
                Text(text = stringResource(id = R.string.task_start_date_action_change))
            }
        }
    }
}
