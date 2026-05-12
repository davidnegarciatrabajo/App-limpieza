package com.dngarcia.tareasdiarias.widget.today.postpone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.usecase.GetTaskByIdUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import com.dngarcia.tareasdiarias.ui.theme.AppLimpiezaTheme
import com.dngarcia.tareasdiarias.widget.today.TodayWidgetIntentFactory
import com.dngarcia.tareasdiarias.widget.today.TodayWidgetUpdater
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostponeTaskDialogActivity : ComponentActivity() {

    @Inject
    lateinit var getTaskByIdUseCase: GetTaskByIdUseCase

    @Inject
    lateinit var postponeTaskUseCase: PostponeTaskUseCase

    @Inject
    lateinit var todayWidgetUpdater: TodayWidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(true)

        val taskId = intent.getLongExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, -1L)
        if (taskId <= 0L) {
            finish()
            return
        }

        bindContent(taskId)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val taskId = intent.getLongExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, -1L)
        if (taskId <= 0L) {
            finish()
            return
        }
        bindContent(taskId)
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun bindContent(taskId: Long) {
        setContent {
            AppLimpiezaTheme {
                PostponeTaskDialogRoute(
                    taskId = taskId,
                    getTaskById = getTaskByIdUseCase,
                    postponeTask = postponeTaskUseCase,
                    onRefreshWidget = todayWidgetUpdater::refreshAll,
                    onClose = ::finish,
                )
            }
        }
    }
}

@Composable
private fun PostponeTaskDialogRoute(
    taskId: Long,
    getTaskById: GetTaskByIdUseCase,
    postponeTask: PostponeTaskUseCase,
    onRefreshWidget: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var saveErrorResId by remember { mutableStateOf<Int?>(null) }

    val loadState by produceState<PostponeDialogLoadState>(
        initialValue = PostponeDialogLoadState.Loading,
        key1 = taskId,
    ) {
        value = runCatching {
            getTaskById(taskId)
        }.fold(
            onSuccess = { task ->
                if (task == null) {
                    PostponeDialogLoadState.NotFound
                } else {
                    PostponeDialogLoadState.Ready(task)
                }
            },
            onFailure = { throwable ->
                PostponeDialogLoadState.Error(
                    throwable.toUserError().messageResId,
                )
            },
        )
    }

    when (val state = loadState) {
        PostponeDialogLoadState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        PostponeDialogLoadState.NotFound -> {
            LaunchedEffect(Unit) {
                Toast.makeText(
                    context,
                    context.getString(R.string.task_not_found),
                    Toast.LENGTH_SHORT,
                ).show()
                onClose()
            }
        }

        is PostponeDialogLoadState.Error -> {
            LaunchedEffect(state.messageResId) {
                Toast.makeText(
                    context,
                    context.getString(state.messageResId),
                    Toast.LENGTH_SHORT,
                ).show()
                onClose()
            }
        }

        is PostponeDialogLoadState.Ready -> {
            saveErrorResId?.let { messageResId ->
                LaunchedEffect(messageResId) {
                    Toast.makeText(
                        context,
                        context.getString(messageResId),
                        Toast.LENGTH_SHORT,
                    ).show()
                    saveErrorResId = null
                }
            }

            WidgetPostponeSheet(
                taskName = state.task.nombre,
                onDismiss = onClose,
                onPostpone = { selectedDate ->
                    coroutineScope.launch {
                        runCatching {
                            postponeTask(taskId = taskId, postponedUntil = selectedDate)
                        }.onSuccess {
                            onRefreshWidget()
                            onClose()
                        }.onFailure { throwable ->
                            saveErrorResId = throwable.toUserError().messageResId
                        }
                    }
                },
            )
        }
    }
}

private sealed interface PostponeDialogLoadState {
    data object Loading : PostponeDialogLoadState
    data object NotFound : PostponeDialogLoadState
    data class Error(val messageResId: Int) : PostponeDialogLoadState
    data class Ready(val task: Tarea) : PostponeDialogLoadState
}

@Composable
private fun WidgetPostponeSheet(
    taskName: String,
    onDismiss: () -> Unit,
    onPostpone: (LocalDate) -> Unit,
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var customDateToConfirm by remember { mutableStateOf<LocalDate?>(null) }
    var validationMessageResId by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.today_postpone_dialog_title, taskName),
                    style = MaterialTheme.typography.titleLarge,
                )
                TextButton(onClick = { onPostpone(today.plusDays(1)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.today_postpone_option_tomorrow))
                }
                TextButton(onClick = { onPostpone(today.plusDays(3)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.today_postpone_option_three_days))
                }
                TextButton(onClick = { onPostpone(today.plusWeeks(1)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.today_postpone_option_next_week))
                }
                TextButton(
                    onClick = {
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                if (selectedDate.isAfter(today)) {
                                    customDateToConfirm = selectedDate
                                } else {
                                    validationMessageResId = R.string.today_postpone_custom_invalid_date
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.today_postpone_option_custom))
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.task_cancel))
                }
            }
        }
    }

    customDateToConfirm?.let { selectedDate ->
        androidx.compose.material3.AlertDialog(
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

    validationMessageResId?.let { messageResId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { validationMessageResId = null },
            title = { Text(text = stringResource(id = R.string.today_postpone_custom_invalid_title)) },
            text = { Text(text = stringResource(id = messageResId)) },
            confirmButton = {
                TextButton(onClick = { validationMessageResId = null }) {
                    Text(text = stringResource(id = R.string.task_confirm))
                }
            },
        )
    }
}
