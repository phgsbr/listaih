package com.listaih.wear.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.Compose.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navGraph
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import com.listaih.wear.WearMainViewModel
import com.listaih.wear.ui.screens.home.WearHomeScreen
import com.listaih.wear.ui.screens.shopping.WearShoppingScreen
import com.listaih.wear.ui.screens.complete.WearCompleteScreen
import com.listaih.wear.ui.screens.select.WearSelectScreen
import com.listaih.wear.ui.screens.voice.WearVoiceScreen

@Composable
fun WearNavHost(
    navController: NavController,
    viewModel: WearMainViewModel
) {
    SwipeDismissableNavHost(navController, startDestination = "home") {
        composable("home") {
            WearHomeScreen(
                onListClick = { listId, listName ->
                    viewModel.setCurrentList(listId)
                    navController.navigate("shopping/$listId/$listName")
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
            val listId = backStackEntry.getString()!!
            val listName = backStackEntry.getString()!!
            WearShoppingScreen(
                listId = listId,
                listName = listName,
                onItemCheck = { /* TODO: Handle check */ },
                onComplete = { navController.navigate("complete/$listId/$listName") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "complete/{listId}/{listName}",
            arguments = listOf(
                androidx.navigation.navArgument("listId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("listName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.getString()!!
            val listName = backStackEntry.getString()!!
            WearCompleteScreen(
                listId = listId,
                listName = listName,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("select") {
            WearSelectScreen(
                onListClick = { listId, listName ->
                    viewModel.setCurrentList(listId)
                    navController.navigate("shopping/$listId/$listName")
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