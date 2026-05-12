package com.dngarcia.tareasdiarias.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dngarcia.tareasdiarias.presentation.categorias.CategoriesRoute
import com.dngarcia.tareasdiarias.presentation.categorias.EditarCategoriaRoute
import com.dngarcia.tareasdiarias.presentation.editar_tarea.EditarTareaRoute
import com.dngarcia.tareasdiarias.presentation.home.HomeRoute
import com.dngarcia.tareasdiarias.presentation.nueva_tarea.NuevaTareaRoute
import com.dngarcia.tareasdiarias.presentation.postpone_task.PostponeTaskRoute
import com.dngarcia.tareasdiarias.presentation.tareas.TareasRoute
import com.dngarcia.tareasdiarias.presentation.today.TodayRoute

object AppRoute {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val TODAY = "today"
    const val NEW_TASK = "new_task"
    const val EDIT_TASK = "edit_task"
    const val TASK_ID_ARG = "taskId"
    const val EDIT_TASK_ROUTE = "$EDIT_TASK/{$TASK_ID_ARG}"
    const val POSTPONE_TASK = "postpone_task"
    const val POSTPONE_TASK_ROUTE = "$POSTPONE_TASK/{$TASK_ID_ARG}"

    const val CATEGORIES = "categories"
    const val EDIT_CATEGORY = "edit_category"
    const val CATEGORY_ID_ARG = "categoryId"
    const val EDIT_CATEGORY_ROUTE = "$EDIT_CATEGORY/{$CATEGORY_ID_ARG}"

    fun editTaskRoute(taskId: Long): String = "$EDIT_TASK/$taskId"
    fun postponeTaskRoute(taskId: Long): String = "$POSTPONE_TASK/$taskId"

    fun editCategoryRoute(categoryId: Long): String = "$EDIT_CATEGORY/$categoryId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    externalRoute: String? = null,
    onExternalRouteConsumed: () -> Unit = {},
) {
    LaunchedEffect(externalRoute) {
        val route = externalRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            launchSingleTop = true
        }
        onExternalRouteConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.TODAY
    ) {
        composable(route = AppRoute.HOME) {
            HomeRoute(
                onTasksClick = {
                    navController.navigate(AppRoute.TASKS)
                },
                onNewTaskClick = {
                    navController.navigate(AppRoute.NEW_TASK)
                },
                onTodayClick = {
                    navController.navigate(AppRoute.TODAY)
                },
                onCategoriesClick = {
                    navController.navigate(AppRoute.CATEGORIES)
                },
            )
        }
        composable(route = AppRoute.CATEGORIES) {
            CategoriesRoute(
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryId ->
                    navController.navigate(AppRoute.editCategoryRoute(categoryId))
                },
            )
        }
        composable(route = AppRoute.TASKS) {
            TareasRoute(
                onOpenToday = {
                    navController.navigate(AppRoute.TODAY) {
                        launchSingleTop = true
                    }
                },
                onOpenMenu = {
                    navController.navigate(AppRoute.HOME) {
                        launchSingleTop = true
                    }
                },
                onAddTaskClick = {
                    navController.navigate(AppRoute.NEW_TASK)
                },
                onTaskClick = { taskId ->
                    navController.navigate(AppRoute.editTaskRoute(taskId))
                },
            )
        }
        composable(route = AppRoute.TODAY) {
            TodayRoute(
                onOpenHome = {
                    navController.navigate(AppRoute.TODAY) {
                        launchSingleTop = true
                    }
                },
                onOpenTasks = {
                    navController.navigate(AppRoute.TASKS) {
                        launchSingleTop = true
                    }
                },
                onOpenMenu = {
                    navController.navigate(AppRoute.HOME) {
                        launchSingleTop = true
                    }
                },
                onAddTask = {
                    navController.navigate(AppRoute.NEW_TASK)
                },
                onEditTask = { taskId ->
                    navController.navigate(AppRoute.editTaskRoute(taskId))
                },
            )
        }
        composable(route = AppRoute.NEW_TASK) {
            NuevaTareaRoute(
                onBack = { navController.popBackStack() },
                onOpenCategories = {
                    navController.navigate(AppRoute.CATEGORIES)
                },
                onTaskCreated = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoute.POSTPONE_TASK_ROUTE,
            arguments = listOf(
                navArgument(AppRoute.TASK_ID_ARG) {
                    type = NavType.LongType
                },
            ),
        ) {
            PostponeTaskRoute(
                onBack = { navController.popBackStack() },
                onTaskPostponed = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoute.EDIT_TASK_ROUTE,
            arguments = listOf(
                navArgument(AppRoute.TASK_ID_ARG) {
                    type = NavType.LongType
                },
            ),
        ) {
            EditarTareaRoute(
                onBack = { navController.popBackStack() },
                onTaskUpdated = { navController.popBackStack() },
            )
        }
        composable(
            route = AppRoute.EDIT_CATEGORY_ROUTE,
            arguments = listOf(
                navArgument(AppRoute.CATEGORY_ID_ARG) {
                    type = NavType.LongType
                },
            ),
        ) {
            EditarCategoriaRoute(
                onBack = { navController.popBackStack() },
                onCategoryUpdated = { navController.popBackStack() },
            )
        }
    }
}
