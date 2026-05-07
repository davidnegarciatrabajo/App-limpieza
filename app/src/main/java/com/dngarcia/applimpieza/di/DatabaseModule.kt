package com.dngarcia.tareasdiarias.di

import android.content.Context
import androidx.room.Room
import com.dngarcia.tareasdiarias.BuildConfig
import com.dngarcia.tareasdiarias.data.local.TareasDatabase
import com.dngarcia.tareasdiarias.data.local.TareasMigrations
import com.dngarcia.tareasdiarias.data.local.dao.CategoriaDao
import com.dngarcia.tareasdiarias.data.local.dao.EjecucionDao
import com.dngarcia.tareasdiarias.data.local.dao.TareaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TareasDatabase {
        val builder = Room.databaseBuilder(
            context,
            TareasDatabase::class.java,
            TareasDatabase.DB_NAME,
        )
            .addMigrations(
                TareasMigrations.MIGRATION_1_2,
                TareasMigrations.MIGRATION_2_3,
            )
        if (BuildConfig.DEBUG) {
            // Si la BD se recrea asi pero el flag de seed sigue en prefs, borrar datos de la app para repetir el seed.
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }
        return builder.build()
    }

    @Provides
    fun provideTareaDao(database: TareasDatabase): TareaDao = database.tareaDao()

    @Provides
    fun provideCategoriaDao(database: TareasDatabase): CategoriaDao = database.categoriaDao()

    @Provides
    fun provideEjecucionDao(database: TareasDatabase): EjecucionDao = database.ejecucionDao()
}
