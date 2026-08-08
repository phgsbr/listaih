package com.listaih.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Compose.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navGraph
import androidx.navigation.compose.rememberNavController
import com.listaih.app.MainViewModel
import com.listaih.app.ui.screens.additem.AddItemBottomSheet
import com.listaih.app.ui.screens.detail.ListDetailScreen
import com.listaih.app.ui.screens.home.HomeScreen
import com.listaih.app.ui.screens.login.LoginScreen
import com.listaih.app.ui.screens.onboarding.OnboardingScreen
import com.listaih.app.ui.screens.settings.SettingsScreen
import com.listaih.app.ui.screens.shopping.ShoppingModeScreen

@Composable
fun AppNavHost(
    navController: NavController,
    viewModel: MainViewModel
) {
    navController.navigate(if (viewModel.isLoggedIn.collectAsState().value) "home" else "onboarding") {
        popUpTo("onboarding") { inclusive = true }
    }

    NavHost(navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(onFinish = { navController.navigate("login") { popUpTo("onboarding") { inclusive = true } } })
        }

        composable("login") {
            LoginScreen(onLoginSuccess = { householdId ->
                viewModel.onLoginSuccess(householdId)
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }

        navigation(startDestination = "home", route = "main") {
            composable("home") {
                HomeScreen(
                    onListClick = { listId, listName ->
                        navController.navigate("detail/$listId/$listName")
                    },
                    onAddListClick = { navController.navigate("add_list") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                route = "detail/{listId}/{listName}",
                arguments = listOf(navArgument("listId") { type = NavType.StringType }, navArgument("listName") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.getString()!!
                val listName = backStackEntry.getString()!!
                ListDetailScreen(
                    listId = listId,
                    listName = listName,
                    onAddItemClick = { AddItemBottomSheet.show(listId) },
                    onBackClick = { navController.popBackStack() },
                    onShoppingModeClick = { navController.navigate("shopping/$listId/$listName") }
                )
            }

            composable("settings") {
                SettingsScreen(onLogout = { viewModel.onLogout(); navController.navigate("login") { popUpTo("main") { inclusive = true } } })
            }

            composable(
                route = "shopping/{listId}/{listName}",
                arguments = listOf(navArgument("listId") { type = NavType.StringType }, navArgument("listName") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.getString()!!
                val listName = backStackEntry.getString()!!
                ShoppingModeScreen(
                    listId = listId,
                    listName = listName,
                    onBackClick = { navController.popBackStack() },
                    onCheckoutComplete = { navController.popBackStack() }
                )
            }
        }
    }
}