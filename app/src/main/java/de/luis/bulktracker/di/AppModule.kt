package de.luis.bulktracker.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.luis.bulktracker.data.db.AppDatabase
import de.luis.bulktracker.data.repo.TrainingRepositoryImpl
import de.luis.bulktracker.domain.repo.TrainingRepository
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase = AppDatabase.build(context)

    /** Eine Uhr statt `Instant.now()` überall – damit Tests eine feste Zeit setzen können. */
    @Provides
    @Singleton
    fun clock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun trainingRepository(impl: TrainingRepositoryImpl): TrainingRepository
}
