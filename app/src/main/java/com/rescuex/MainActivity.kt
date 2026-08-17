package com.rescuex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.rememberNavController
import com.rescuex.ui.navigation.AppNavigation
import com.rescuex.ui.theme.RescueXAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RescueXAITheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
