package dev.syta.myaudioevents.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.syta.myaudioevents.UserPreferences
import dev.syta.myaudioevents.data.datastore.MaePreferencesDataSource
import dev.syta.myaudioevents.data.datastore.UserPreferencesSerializer
import dev.syta.myaudioevents.data.local.MaeDatabase
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.repository.AudioClassRepository
import dev.syta.myaudioevents.data.repository.AudioRecordingRepository
import dev.syta.myaudioevents.data.repository.AudioRecordingRepositoryImpl
import dev.syta.myaudioevents.data.repository.CompositeUserAudioClassRepository
import dev.syta.myaudioevents.data.repository.OfflineFirstAudioClassRepository
import dev.syta.myaudioevents.data.repository.OfflineFirstUserDataRepository
import dev.syta.myaudioevents.data.repository.UserAudioClassRepository
import dev.syta.myaudioevents.data.repository.UserDataRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun providesMaeDatabase(
        @ApplicationContext context: Context
    ): MaeDatabase = Room.databaseBuilder(
        context,
        MaeDatabase::class.java,
        "mae-database",
    ).build()

    @Provides
    @Singleton
    fun providesAudioClassDao(
        database: MaeDatabase
    ): AudioClassDao = database.audioClassDao()

    @Provides
    @Singleton
    fun providesAudioClassRepository(
        audioClassDao: AudioClassDao
    ): AudioClassRepository = OfflineFirstAudioClassRepository(
        audioClassDao = audioClassDao
    )

    @Provides
    @Singleton
    internal fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
        userPreferencesSerializer: UserPreferencesSerializer,
    ): DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = userPreferencesSerializer,
    ) {
        context.dataStoreFile("user_preferences.pb")
    }

    @Provides
    @Singleton
    internal fun providesUserDataRepository(
        maePreferencesDataSource: MaePreferencesDataSource,
    ): UserDataRepository = OfflineFirstUserDataRepository(
        maePreferencesDataSource = maePreferencesDataSource
    )

    @Provides
    @Singleton
    internal fun providesUserAudioClassRepository(
        audioClassRepository: AudioClassRepository,
        userDataRepository: UserDataRepository,
    ): UserAudioClassRepository = CompositeUserAudioClassRepository(
        audioClassRepository = audioClassRepository, userDataRepository = userDataRepository
    )

    @Provides
    @Singleton
    internal fun providesAudioRecordingRepository(
        database: MaeDatabase
    ): AudioRecordingRepository = AudioRecordingRepositoryImpl(database.audioRecordingDao())
}