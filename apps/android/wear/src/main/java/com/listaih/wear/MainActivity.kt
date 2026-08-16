package com.listaih.wear

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.listaih.wear.navigation.WearNavHost
import com.listaih.wear.ui.scanpopup.WearScanPopupHost
import com.listaih.wear.ui.theme.WearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WearMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTheme {
                val popup by viewModel.scanPopup.collectAsState()
                LaunchedEffect(popup) {
                    if (popup != null) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                    }
                }
                Scaffold(
                    timeText = { TimeText() },
                    vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberSwipeDismissableNavController()
                        WearNavHost(
                            navController = navController,
                            viewModel = viewModel
                        )
                        WearScanPopupHost(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (imeVisible()) return super.onKeyDown(keyCode, event)
        if (viewModel.onHidKey(keyCode, event.eventTime)) return true
        return super.onKeyDown(keyCode, event)
    }

    private fun imeVisible(): Boolean {
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(window.decorView)
            ?: return false
        return insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (imeVisible()) return super.onKeyUp(keyCode, event)
        if (keyCode == 66 || keyCode == 134) return true
        return super.onKeyUp(keyCode, event)
    }
}