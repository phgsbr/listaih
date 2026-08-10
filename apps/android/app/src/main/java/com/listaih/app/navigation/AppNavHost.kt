package com.listaih.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.listaih.app.MainViewModel
import com.listaih.app.ui.screens.addlist.AddListScreen
import com.listaih.app.ui.screens.detail.ListDetailScreen
import com.listaih.app.ui.screens.home.HomeScreen
import com.listaih.app.ui.screens.login.LoginScreen
import com.listaih.app.ui.screens.onboarding.OnboardingScreen
import com.listaih.app.ui.screens.purchases.PurchasesScreen
import com.listaih.app.ui.screens.settings.SettingsScreen
import com.listaih.app.ui.screens.shopping.ShoppingModeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val shoppingLists = viewModel.shoppingLists.collectAsState(initial = emptyList()).value

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(onFinish = { navController.navigate("login") { popUpTo("onboarding") { inclusive = true } } })
        }

        composable("login") {
            LoginScreen(onLoginSuccess = { householdId ->
                viewModel.onLoginSuccess(householdId)
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        navigation(startDestination = "home", route = "main") {
            composable("home") {
                HomeScreen(
                    lists = shoppingLists,
                    onListClick = { listId, listName ->
                        navController.navigate("detail/$listId/$listName")
                    },
                    onAddListClick = { navController.navigate("add_list") },
                    onSettingsClick = { navController.navigate("settings") },
                    onHistoryClick = { navController.navigate("purchases") }
                )
            }

            composable("purchases") {
                PurchasesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("add_list") {
                AddListScreen(
                    onBackClick = { navController.popBackStack() },
                    onCreate = { name, listType, icon ->
                        viewModel.currentHouseholdId.value?.let { householdId ->
                            val category = when (icon) {
                                "medication" -> "Farmacia"
                                else -> "Alimentos"
                            }
                            viewModel.addShoppingList(householdId, name, category, listType)
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(
                route = "detail/{listId}/{listName}",
                arguments = listOf(navArgument("listId") { type = NavType.StringType }, navArgument("listName") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId")
                val listName = backStackEntry.arguments?.getString("listName")
                ListDetailScreen(
                    listId = listId ?: "",
                    listName = listName ?: "",
                    onBackClick = { navController.popBackStack() },
                    onShoppingModeClick = { navController.navigate("shopping/$listId/$listName") }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = { viewModel.onLogout(); navController.navigate("login") { popUpTo("main") { inclusive = true } } }
                )
            }

            composable(
                route = "shopping/{listId}/{listName}",
                arguments = listOf(navArgument("listId") { type = NavType.StringType }, navArgument("listName") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId")
                val listName = backStackEntry.arguments?.getString("listName")
                ShoppingModeScreen(
                    listId = listId ?: "",
                    listName = listName ?: "",
                    onBackClick = { navController.popBackStack() },
                    onCheckoutComplete = { navController.popBackStack() }
                )
            }
        }
    }
}