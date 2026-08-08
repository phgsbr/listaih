package com.listaih.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.MutableStateFlow
import androidx.lifecycle.viewmodel.viewModelScope
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import com.listaih.app.data.repository.ShoppingRepository
import com.listaih.wear.navigation.WearNavHost
import com.listaih.wear.ui.theme.WearTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: WearMainViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearTheme {
                Surface {
                    WearNavHost(
                        navController = rememberNavController(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}