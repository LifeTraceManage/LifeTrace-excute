package com.lifetrace.execute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lifetrace.execute.ui.LifeTraceExecuteApp
import com.lifetrace.execute.ui.theme.LifeTraceExecuteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeTraceExecuteTheme {
                LifeTraceExecuteApp()
            }
        }
    }
}
