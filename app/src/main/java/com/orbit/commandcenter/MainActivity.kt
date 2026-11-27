package com.orbit.commandcenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.orbit.commandcenter.navigation.OrbitApp
import com.orbit.commandcenter.ui.theme.OrbitTheme

/**
 * The only activity in the application. It hosts the Compose tree, applies the ORBIT
 * theme and hands navigation to [OrbitApp].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OrbitTheme {
                OrbitApp()
            }
        }
    }
}
