package com.yash.Speachr.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHost
import kotlinx.serialization.Serializable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yash.Speachr.ui.screens.home.HomeScreen
import com.yash.Speachr.ui.screens.about.AboutScreen
import com.yash.Speachr.ui.screens.dictionary.DictionaryScreen
import com.yash.Speachr.ui.screens.settings.SettingsScreen
import com.yash.Speachr.ui.screens.snippets.SnippetsScreen
import com.yash.Speachr.ui.screens.style.StyleScreen


@Serializable
object HomeRoute

@Serializable
object DictionaryRoute

@Serializable
object StyleRoute

@Serializable
object SnippetsRoute

@Serializable
object SettingsRoute

@Serializable
object AboutRoute


enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: Any
) {
    HOME("Home", Icons.Outlined.Home, HomeRoute),
    DICTIONARY("Dictionary", Icons.Outlined.AttachFile, DictionaryRoute),
    STYLE("Style", Icons.Outlined.TextFields, StyleRoute),
    SNIPPETS("Snippets", Icons.Outlined.ContentCut, SnippetsRoute),
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = Modifier
    ) {
        composable<HomeRoute> {
            HomeScreen()
        }
        composable<DictionaryRoute> {
            DictionaryScreen()
        }
        composable<StyleRoute> {
            StyleScreen()
        }
        composable<SnippetsRoute> {
            SnippetsScreen()
        }
        composable<SettingsRoute> {
            SettingsScreen()
        }
        composable<AboutRoute> {
            AboutScreen()
        }
    }
}