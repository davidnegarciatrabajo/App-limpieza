package com.dngarcia.tareasdiarias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dngarcia.tareasdiarias.presentation.navigation.AppNavigation
import com.dngarcia.tareasdiarias.ui.theme.AppLimpiezaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppLimpiezaTheme {
                AppNavigation()
            }
        }
    }
} 