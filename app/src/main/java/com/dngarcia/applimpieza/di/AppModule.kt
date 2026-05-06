package com.dngarcia.tareasdiarias.di

import com.dngarcia.tareasdiarias.data.repository.CategoriaRepositoryImpl
import com.dngarcia.tareasdiarias.data.repository.EjecucionRepositoryImpl
import com.dngarcia.tareasdiarias.data.repository.TareaRepositoryImpl
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.data.reminder.WorkManagerTaskReminderScheduler
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindTareaRepository(impl: TareaRepositoryImpl): TareaRepository

    @Binds
    @Singleton
    abstract fun bindCategoriaRepository(impl: CategoriaRepositoryImpl): CategoriaRepository

    @Binds
    @Singleton
    abstract fun bindEjecucionRepository(impl: EjecucionRepositoryImpl): EjecucionRepository

    @Binds
    @Singleton
    abstract fun bindTaskReminderScheduler(impl: WorkManagerTaskReminderScheduler): TaskReminderScheduler
}
