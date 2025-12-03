package com.osg.restaurantcompanionapp.view.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.navigation.NavItem
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun SwipeableScreens(
    currentRoute: String,
    navController: NavController,
    ordersContent: @Composable () -> Unit,
    menuItemsContent: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    var currentPage by remember { mutableIntStateOf(if (currentRoute == NavItem.Orders.route) 0 else 1) }
    val offsetX = remember { Animatable(-currentPage * screenWidthPx) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentRoute) {
        val targetPage = if (currentRoute == NavItem.Orders.route) 0 else 1
        if (targetPage != currentPage) {
            currentPage = targetPage
            offsetX.snapTo(-currentPage * screenWidthPx)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var isHorizontalDrag: Boolean? = null
                    var lastPosition = down.position

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        val dragX = change.position.x - lastPosition.x
                        val dragY = change.position.y - lastPosition.y

                        totalDragX += dragX
                        totalDragY += dragY

                        if (isHorizontalDrag == null && (abs(totalDragX) > 10f || abs(totalDragY) > 10f)) {
                            val angle = Math.toDegrees(atan2(abs(totalDragY).toDouble(), abs(totalDragX).toDouble()))
                            isHorizontalDrag = angle < 45
                        }

                        if (isHorizontalDrag == true) {
                            dragOffset += dragX
                            scope.launch {
                                val newOffset = (-currentPage * screenWidthPx) + dragOffset
                                val clampedOffset = newOffset.coerceIn(-screenWidthPx, 0f)
                                offsetX.snapTo(clampedOffset)
                            }
                            change.consume()
                        }

                        lastPosition = change.position
                    } while (event.changes.any { it.pressed })

                    if (isHorizontalDrag == true) {
                        val threshold = screenWidthPx * 0.3f

                        val targetPage = when {
                            currentPage == 0 && totalDragX < -threshold -> 1
                            currentPage == 1 && totalDragX > threshold -> 0
                            dragOffset.absoluteValue > threshold -> {
                                if (dragOffset < 0) 1 else 0
                            }
                            else -> currentPage
                        }

                        scope.launch {
                            offsetX.animateTo(
                                targetValue = -targetPage * screenWidthPx,
                                animationSpec = tween(durationMillis = 300)
                            )

                            if (targetPage != currentPage) {
                                currentPage = targetPage
                                val newRoute = if (targetPage == 0) NavItem.Orders.route else NavItem.MenuItem.route
                                navController.navigate(newRoute) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = -currentPage * screenWidthPx,
                                animationSpec = tween(durationMillis = 300)
                            )
                        }
                    }

                    dragOffset = 0f
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            ordersContent()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((screenWidthPx + offsetX.value).roundToInt(), 0) }
        ) {
            menuItemsContent()
        }
    }
}

