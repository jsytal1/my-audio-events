package dev.syta.myaudioevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.syta.myaudioevents.ui.MaeApp
import dev.syta.myaudioevents.ui.rememberMaeAppState
import dev.syta.myaudioevents.ui.theme.MyAudioEventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberMaeAppState()
            MyAudioEventsTheme {
                MaeApp(appState)
            }
        }
    }
}

