package com.listaih.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.scanner.BtScannerManager
import com.listaih.app.data.scanner.LocalBtScanner
import com.listaih.app.navigation.AppNavHost
import com.listaih.app.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var preferences: AppPreferences

    @Inject
    lateinit var btScannerManager: BtScannerManager

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (btScannerManager.handleKeyEvent(event)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var theme by androidx.compose.runtime.remember { mutableStateOf("system") }

            LaunchedEffect(Unit) {
                preferences.getTheme().subscribe { value ->
                    theme = value
                }
            }

            val darkTheme = when (theme) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            CompositionLocalProvider(LocalBtScanner provides btScannerManager) {
                Theme(darkTheme = darkTheme) {
                    Surface {
                        AppNavHost(
                            navController = rememberNavController(),
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}