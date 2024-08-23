package dev.syta.myaudioevents.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.syta.myaudioevents.services.AudioRecorderServiceManager
import dev.syta.myaudioevents.services.AudioRecorderServiceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object ApplicationComponents {

    @Provides
    @Singleton
    fun provideAudioRecordingServiceManager(
        @ApplicationContext context: Context
    ): AudioRecorderServiceManager = AudioRecorderServiceManagerImpl(context)
}