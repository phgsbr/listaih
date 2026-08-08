package com.listaih.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.MutableStateFlow
import androidx.lifecycle.viewmodel.viewModelScope
import com.listaih.app.di.AppModule
import com.listaih.app.navigation.AppNavHost
import com.listaih.app.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Theme {
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