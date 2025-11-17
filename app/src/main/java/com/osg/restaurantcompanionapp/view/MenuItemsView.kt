import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel

@Composable
fun MenuItemsView(viewModel: MenuItemViewModel) {
    val menuItems = viewModel.menuItemsLiveData.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMenuItems()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            menuItems.value == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            menuItems.value!!.isEmpty() -> {
                Text(
                    text = "No menu items available",
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(menuItems.value!!) { menuItem ->
                        MenuItemItem(menuItem = menuItem)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemItem(menuItem: MenuItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = menuItem.name)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Price: $${menuItem.price}")
        }
    }
}