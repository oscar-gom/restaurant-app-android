package com.osg.restaurantcompanionapp.view.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import com.osg.restaurantcompanionapp.navigation.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScaffold(
    navController: NavController,
    current: NavItem,
    onAdd: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.title) },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                NavItem.all.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}