package com.listaih.wear.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.listaih.wear.WearMainViewModel
import com.listaih.wear.ui.screens.checkout.WearCheckoutScreen
import com.listaih.wear.ui.screens.complete.WearCompleteScreen
import com.listaih.wear.ui.screens.home.WearHomeScreen
import com.listaih.wear.ui.screens.select.WearSelectScreen
import com.listaih.wear.ui.screens.shopping.WearShoppingScreen
import com.listaih.wear.ui.screens.voice.WearVoiceScreen

@Composable
fun WearNavHost(
    navController: NavHostController,
    viewModel: WearMainViewModel
) {
    SwipeDismissableNavHost(navController, startDestination = "home") {
        composable("home") {
            WearHomeScreen(
                onListClick = { listId, listName ->
                    viewModel.setCurrentList(listId)
                    navController.navigate("shopping/$listId/${Uri.encode(listName)}")
                },
                onSelectClick = { navController.navigate("select") },
                onVoiceClick = { navController.navigate("voice") }
            )
        }

        composable(
            route = "shopping/{listId}/{listName}",
            arguments = listOf(
                androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("listName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            WearShoppingScreen(
                viewModel = viewModel,
                listId = listId,
                listName = listName,
                onCheckout = { count, total ->
                    navController.navigate("checkout/$listId/${Uri.encode(listName)}/$count/$total")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "checkout/{listId}/{listName}/{count}/{total}",
            arguments = listOf(
                androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("listName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("count") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("total") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            val count = backStackEntry.arguments?.getInt("count") ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toDoubleOrNull() ?: 0.0
            WearCheckoutScreen(
                listName = listName,
                checkedCount = count,
                estimatedTotal = total,
                initialPayment = "",
                onConfirm = { method, amountStr, _ ->
                    val safeMethod = if (method.isEmpty()) "SEM_PAGAMENTO" else method
                    navController.navigate("complete/$listId/${Uri.encode(listName)}/$count/$amountStr/${Uri.encode(safeMethod)}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "complete/{listId}/{listName}/{count}/{total}/{method}",
            arguments = listOf(
                androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("listName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("count") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("total") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("method") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val listName = backStackEntry.arguments?.getString("listName") ?: ""
            val count = backStackEntry.arguments?.getInt("count") ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toDoubleOrNull() ?: 0.0
            val method = backStackEntry.arguments?.getString("method") ?: ""
            WearCompleteScreen(
                listId = listId,
                listName = listName,
                checkedCount = count,
                total = total,
                paymentMethod = method,
                onHomeClick = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        composable("select") {
            WearSelectScreen(
                onListClick = { listId, listName ->
                    viewModel.setCurrentList(listId)
                    navController.navigate("shopping/$listId/${Uri.encode(listName)}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("voice") {
            WearVoiceScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}