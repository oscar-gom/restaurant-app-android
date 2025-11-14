package com.osg.restaurantcompanionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.osg.restaurantcompanionapp.navigation.NavItem
import com.osg.restaurantcompanionapp.ui.theme.RestaurantCompanionAppTheme
import com.osg.restaurantcompanionapp.view.OrdersView
import com.osg.restaurantcompanionapp.view.component.BaseScaffold
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestaurantCompanionAppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavItem.Orders.route) {
        composable(NavItem.Orders.route) {
            val vm: OrderViewModel = viewModel()
            BaseScaffold(
                navController = navController,
                current = NavItem.Orders,
                onAdd = { vm.onAdd() }
            ) {
                OrdersView(
                    modifier = it,
                    viewModel = vm
                )
            }
        }
    }
}