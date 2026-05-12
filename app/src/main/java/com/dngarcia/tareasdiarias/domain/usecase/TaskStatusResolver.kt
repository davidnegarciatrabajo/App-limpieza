package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.TaskStatusInfo
import com.dngarcia.tareasdiarias.domain.model.Tarea
import java.time.LocalDateTime

object TaskStatusResolver {
    fun resolve(
        task: Tarea,
        now: LocalDateTime,
        completedForExpectedCycle: Boolean = false,
    ): TaskStatusInfo {
        val dueDate = TaskTimelinePolicy.expectedCycleDate(task)
        val today = now.toLocalDate()
        val status = when {
            dueDate == null -> TaskStatus.OK
            completedForExpectedCycle -> TaskStatus.OK
            !dueDate.isAfter(today) -> TaskStatus.VENCIDA
            else -> TaskStatus.OK
        }

        val daysDelta = dueDate?.let {
            when (status) {
                TaskStatus.VENCIDA -> TaskTimelinePolicy.overdueDays(task, today)
                TaskStatus.OK -> null
            }
        }

        return TaskStatusInfo(
            status = status,
            daysDelta = daysDelta,
        )
    }
}
