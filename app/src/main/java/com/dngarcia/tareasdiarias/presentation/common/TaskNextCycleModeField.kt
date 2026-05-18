package com.dngarcia.tareasdiarias.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.ModoProximoCiclo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskNextCycleModeField(
    selected: ModoProximoCiclo,
    onSelected: (ModoProximoCiclo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        ModoProximoCiclo.ANCLADO_FECHA_INICIO ->
            stringResource(id = R.string.task_next_cycle_anchored)
        ModoProximoCiclo.INTERVALO_DESDE_COMPLETADO ->
            stringResource(id = R.string.task_next_cycle_floating)
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.task_field_next_cycle_mode)) },
            supportingText = { Text(stringResource(id = R.string.task_next_cycle_only_recurring)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                )
                .fillMaxWidth(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.task_next_cycle_anchored)) },
                onClick = {
                    onSelected(ModoProximoCiclo.ANCLADO_FECHA_INICIO)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.task_next_cycle_floating)) },
                onClick = {
                    onSelected(ModoProximoCiclo.INTERVALO_DESDE_COMPLETADO)
                    expanded = false
                },
            )
        }
    }
}
