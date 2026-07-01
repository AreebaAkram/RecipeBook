package com.recipebook.community.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.recipebook.community.ui.screens.*
import com.recipebook.community.viewmodel.AuthState
import com.recipebook.community.viewmodel.AuthViewModel
import com.recipebook.community.viewmodel.CommunityViewModel
import com.recipebook.community.viewmodel.RecipeViewModel

sealed class Screen(val route: String) {
    object Splash         : Screen("splash")
    object Login          : Screen("login")
    object SignUp         : Screen("signup")
    object CommunitySetup : Screen("community_setup")
    object Home           : Screen("home")
    object AddRecipe      : Screen("add_recipe")
    object Approvals      : Screen("approvals")
    object Activity       : Screen("activity")
    object Profile        : Screen("profile")
    object RecipeDetail   : Screen("recipe_detail/{recipeId}") {
        fun createRoute(id: String) = "recipe_detail/$id"
    }
    object EditRecipe     : Screen("edit_recipe/{recipeId}") {
        fun createRoute(id: String) = "edit_recipe/$id"
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun SplashScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍳", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Recipe Book",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RecipeBookApp() {
    val authVM  : AuthViewModel      = viewModel()
    val recipeVM: RecipeViewModel    = viewModel()
    val commVM  : CommunityViewModel = viewModel()
    val authState   by authVM.authState.collectAsState()
    val currentUser by authVM.currentUser.collectAsState()

    LaunchedEffect(authState, currentUser) {
        if (authState is AuthState.Authenticated && currentUser != null) {
            recipeVM.init(currentUser!!.communityId)
            commVM.init(currentUser!!.communityId)
        }
    }

    val navController = rememberNavController()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Loading        -> { /* show splash, do nothing */ }
            is AuthState.Idle           -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            is AuthState.NeedsCommunity -> navController.navigate(Screen.CommunitySetup.route) {
                popUpTo(0) { inclusive = true }
            }
            is AuthState.Authenticated  -> navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
            else -> {}
        }
    }

    val bottomItems = listOf(
        BottomNavItem(Screen.Home,      "Home",      Icons.Default.Home),
        BottomNavItem(Screen.AddRecipe, "Add",       Icons.Default.AddCircle),
        BottomNavItem(Screen.Approvals, "Approvals", Icons.Default.CheckCircle),
        BottomNavItem(Screen.Activity,  "Activity",  Icons.Default.List),
        BottomNavItem(Screen.Profile,   "Profile",   Icons.Default.Person)
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute     = currentBackStack?.destination?.route
    val showBottomBar    = bottomItems.any { it.screen.route == currentRoute }
    val pendingCount     by recipeVM.pendingRecipes.collectAsState()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val navDest = currentBackStack?.destination
                    bottomItems.forEach { item ->
                        val isAdmin = currentUser?.role == "admin"
                        if (item.screen == Screen.Approvals && !isAdmin) return@forEach
                        NavigationBarItem(
                            selected = navDest?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true,
                            onClick  = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                if (item.screen == Screen.Approvals && pendingCount.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text("${pendingCount.size}") } }) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(padding),
            enterTransition  = {
                slideInHorizontally(tween(300)) { it / 2 } + fadeIn(tween(300))
            },
            exitTransition   = {
                slideOutHorizontally(tween(300)) { -it / 2 } + fadeOut(tween(300))
            }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen()
            }
            composable(Screen.Login.route) {
                LoginScreen(authVM) { navController.navigate(Screen.SignUp.route) }
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(authVM) { navController.popBackStack() }
            }
            composable(Screen.CommunitySetup.route) {
                CommunitySetupScreen(authVM)
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    recipeVM       = recipeVM,
                    user           = currentUser,
                    onRecipeClick  = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                    onAddClick     = { navController.navigate(Screen.AddRecipe.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.AddRecipe.route) {
                AddRecipeScreen(recipeVM, currentUser) { navController.popBackStack() }
            }
            composable(Screen.Approvals.route) {
                ApprovalsScreen(recipeVM, currentUser)
            }
            composable(Screen.Activity.route) {
                ActivityFeedScreen(recipeVM)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(authVM, recipeVM, commVM, currentUser)
            }
            composable(Screen.RecipeDetail.route) { back ->
                val recipeId = back.arguments?.getString("recipeId") ?: ""
                RecipeDetailScreen(
                    recipeId = recipeId,
                    recipeVM = recipeVM,
                    user     = currentUser,
                    onBack   = { navController.popBackStack() },
                    onEdit   = { id -> navController.navigate(Screen.EditRecipe.createRoute(id)) }
                )
            }
            composable(Screen.EditRecipe.route) { back ->
                val recipeId = back.arguments?.getString("recipeId") ?: ""
                EditRecipeScreen(
                    recipeId = recipeId,
                    recipeVM = recipeVM,
                    user     = currentUser,
                    onDone   = { navController.popBackStack() }
                )
            }
        }
    }
}