package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LongTaskViewScreen
import com.example.ui.screens.QuestVaultScreen
import com.example.ui.screens.StandaloneTimerScreen
import com.example.ui.screens.WeeklyPlannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room database with automatic fallback recovery on migrations
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, 
            "adhd_mastery_room_db"
        ).fallbackToDestructiveMigration().build()

        val longTaskDao = db.longTaskDao()
        val dailyRoutineDao = db.dailyRoutineDao()
        val completionLogDao = db.completionLogDao()
        val repository = TaskRepository(longTaskDao, dailyRoutineDao, completionLogDao)

        setContent {
            MyApplicationTheme {
                val factory = MainViewModelFactory(application, repository)
                val mainViewModel: MainViewModel = viewModel(factory = factory)
                
                AppShell(viewModel = mainViewModel)
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
    object Vault : BottomNavItem(
        route = "vault",
        title = "Vault",
        selectedIcon = Icons.Filled.WorkspacePremium,
        unselectedIcon = Icons.Outlined.WorkspacePremium
    )
    object WeeklyPlanner : BottomNavItem(
        route = "planner",
        title = "Planner",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
    object Timer : BottomNavItem(
        route = "timer",
        title = "Timer",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )
}

@Composable
fun AppShell(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Vault,
        BottomNavItem.WeeklyPlanner,
        BottomNavItem.Timer
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().background(ObsidianBg).testTag("app_scaffold"),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Hide bottom navigation bar when viewing the detailed quest screen to focus attention
            val showBottomBar = navBackStackEntry?.destination?.route?.startsWith("task_view/") != true

            if (showBottomBar) {
                NavigationBar(
                    containerColor = ObsidianBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_bottom_nav_bar")
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        NavigationBarItem(
                            selected = isSelected,
                            label = { 
                                Text(
                                    text = screen.title, 
                                    color = if (isSelected) NeonPurpleLight else TextSecondary,
                                    modifier = Modifier.testTag("nav_label_${screen.route}")
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    tint = if (isSelected) Color.White else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonPurple,
                                selectedIconColor = Color.White,
                                unselectedIconColor = TextSecondary
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(navController = navController, viewModel = viewModel)
            }
            
            composable(BottomNavItem.Vault.route) {
                QuestVaultScreen(navController = navController, viewModel = viewModel)
            }
            
            composable(BottomNavItem.WeeklyPlanner.route) {
                WeeklyPlannerScreen(viewModel = viewModel)
            }
            
            composable(BottomNavItem.Timer.route) {
                StandaloneTimerScreen(viewModel = viewModel)
            }
            
            composable(
                route = "task_view/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
                LongTaskViewScreen(navController = navController, viewModel = viewModel, taskId = taskId)
            }
        }
    }
}
