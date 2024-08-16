package dev.syta.myaudioevents.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.syta.myaudioevents.data.local.MaeDatabase
import dev.syta.myaudioevents.data.local.dao.AudioClassDao
import dev.syta.myaudioevents.data.repository.AudioClassStore
import dev.syta.myaudioevents.data.repository.LocalAudioClassStore
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
    fun providesAudioClassStore(
        audioClassDao: AudioClassDao
    ): AudioClassStore = LocalAudioClassStore(
        audioClassDao = audioClassDao
    )
}