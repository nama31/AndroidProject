package com.example.mytest.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mytest.AppGraph
import com.example.mytest.ui.alarm.AlarmEvent
import com.example.mytest.ui.alarm.AlarmViewModel
import com.example.mytest.ui.alarm.dismiss.DismissScreen
import com.example.mytest.ui.alarm.list.AlarmListScreen

/**
 * Top-level navigation graph.
 *
 * Routes:
 * - `list` — [AlarmListScreen]
 * - `dismiss/{alarmId}` — [DismissScreen]
 *
 * The [AlarmViewModel] is stored at the activity scope (via [viewModel]
 * with no `viewModelStoreOwner` override) so both screens share the same
 * state and event stream. [AlarmEvent.NavigateToDismiss] /
 * [AlarmEvent.NavigateBackToList] drive the controller.
 */
object AlarmXRoutes {
    const val LIST = "list"
    const val DISMISS = "dismiss/{alarmId}"
    const val ALARM_ID_ARG = "alarmId"

    fun dismiss(alarmId: Long) = "dismiss/$alarmId"
}

@Composable
fun AlarmXNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val alarmViewModel: AlarmViewModel = viewModel(factory = AppGraph.alarmViewModelFactory)

    LaunchedEffect(navController) {
        alarmViewModel.events.collect { event ->
            when (event) {
                is AlarmEvent.NavigateToDismiss -> {
                    if (navController.currentDestination?.route != AlarmXRoutes.DISMISS) {
                        navController.navigate(AlarmXRoutes.dismiss(event.alarmId))
                    }
                }
                AlarmEvent.NavigateBackToList -> {
                    navController.popBackStack(AlarmXRoutes.LIST, inclusive = false)
                }
                else -> Unit
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AlarmXRoutes.LIST,
        modifier = modifier,
    ) {
        composable(AlarmXRoutes.LIST) {
            AlarmListScreen(viewModel = alarmViewModel)
        }
        composable(
            route = AlarmXRoutes.DISMISS,
            arguments = listOf(
                navArgument(AlarmXRoutes.ALARM_ID_ARG) { type = NavType.LongType },
            ),
        ) { entry ->
            val alarmId = entry.arguments?.getLong(AlarmXRoutes.ALARM_ID_ARG) ?: 0L
            LaunchedEffect(alarmId) {
                alarmViewModel.onAlarmTriggered(alarmId)
            }
            DismissScreen(viewModel = alarmViewModel)
        }
    }
}
