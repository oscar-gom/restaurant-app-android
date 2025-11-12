package com.osg.restaurantcompanionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.osg.restaurantcompanionapp.navigation.NavItem
import com.osg.restaurantcompanionapp.ui.theme.RestaurantCompanionAppTheme
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
                OrderListView(
                    modifier = it,
                    viewModel = vm
                )
            }
        }
    }
}