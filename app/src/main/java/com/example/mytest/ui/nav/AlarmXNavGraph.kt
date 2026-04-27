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
 * The [AlarmViewModel] is hoisted at the activity scope (single instance
 * for the whole graph) so both screens share state and event stream.
 *
 * If [initialAlarmId] is non-null (set when MainActivity is launched via the
 * full-screen alarm intent), we navigate to the dismiss screen for that
 * alarm before the list is ever shown.
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
    initialAlarmId: Long? = null,
    navController: NavHostController = rememberNavController(),
) {
    val alarmViewModel: AlarmViewModel = viewModel(factory = AppGraph.alarmViewModelFactory)

    LaunchedEffect(initialAlarmId) {
        if (initialAlarmId != null) {
            navController.navigate(AlarmXRoutes.dismiss(initialAlarmId))
        }
    }

    LaunchedEffect(navController) {
        alarmViewModel.events.collect { event ->
            when (event) {
                is AlarmEvent.NavigateToDismiss -> {
                    val alreadyOnDismiss =
                        navController.currentDestination?.route == AlarmXRoutes.DISMISS
                    if (!alreadyOnDismiss) {
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
