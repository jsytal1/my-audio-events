package dev.syta.myaudioevents.services

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext

interface AudioRecorderServiceManager {
    fun startRecording()
    fun stopRecording()
}

class AudioRecorderServiceManagerImpl(
    @ApplicationContext private val context: Context,
) : AudioRecorderServiceManager {
    override fun startRecording() {
        Log.d("AudioRecorderServiceManager", "Starting recording")
        context.startService(Intent(context, AudioRecorderService::class.java))
    }

    override fun stopRecording() {
        context.stopService(Intent(context, AudioRecorderService::class.java))
    }
}