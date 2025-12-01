package com.osg.restaurantcompanionapp.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object MinimalistElevation {
    val none = 0.dp
    val extraSmall = 0.25.dp
    val small = 0.5.dp
    val medium = 1.dp
    val large = 1.5.dp
    val extraLarge = 2.dp
}

@Composable
fun minimalistCardElevation(): CardElevation {
    return CardDefaults.cardElevation(
        defaultElevation = MinimalistElevation.extraSmall,
        pressedElevation = MinimalistElevation.small,
        focusedElevation = MinimalistElevation.small,
        hoveredElevation = MinimalistElevation.small,
        draggedElevation = MinimalistElevation.medium,
        disabledElevation = MinimalistElevation.none
    )
}

