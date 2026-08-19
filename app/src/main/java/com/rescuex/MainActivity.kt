package com.rescuex

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.rescuex.ui.navigation.AppNavigation
import com.rescuex.ui.theme.RescueXAITheme

class MainActivity : ComponentActivity() {
    private val TAG = "VapiLifecycle"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onCreate")
        enableEdgeToEdge()
        setContent {
            RescueXAITheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "[VAPI LIFECYCLE] Activity onDestroy")
    }
}
