package com.dngarcia.tareasdiarias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dngarcia.tareasdiarias.presentation.navigation.AppNavigation
import com.dngarcia.tareasdiarias.ui.theme.AppLimpiezaTheme
import com.dngarcia.tareasdiarias.widget.today.TodayWidgetIntentFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingNavigationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNavigationRoute = intent.extractExternalRoute()
        enableEdgeToEdge()
        setContent {
            AppLimpiezaTheme {
                AppNavigation(
                    externalRoute = pendingNavigationRoute,
                    onExternalRouteConsumed = { pendingNavigationRoute = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigationRoute = intent.extractExternalRoute()
    }
}

private fun android.content.Intent?.extractExternalRoute(): String? {
    return this?.getStringExtra(TodayWidgetIntentFactory.EXTRA_NAV_ROUTE)
}
