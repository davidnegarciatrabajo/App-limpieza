package com.dngarcia.tareasdiarias.widget.today

import com.dngarcia.tareasdiarias.domain.usecase.CompleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.GetTodayWidgetTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.UndoTaskCompletionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TodayWidgetEntryPoint {
    fun getTodayWidgetTasksUseCase(): GetTodayWidgetTasksUseCase
    fun completeTaskUseCase(): CompleteTaskUseCase
    fun undoTaskCompletionUseCase(): UndoTaskCompletionUseCase
    fun postponeTaskUseCase(): PostponeTaskUseCase
}
