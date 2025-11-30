package com.osg.restaurantcompanionapp.repository

import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.network.ApiService

class MenuItemRepository(val apiService: ApiService) {

    suspend fun getMenuItems(): List<MenuItem>? {
        val response = apiService.getMenuItems()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getMenuItemById(id: Int): MenuItem? {
        val response = apiService.getMenuItemById(id)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createMenuItem(menuItem: MenuItem): MenuItem? {
        val response = apiService.createMenuItem(menuItem)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateMenuItem(id: Int, menuItem: MenuItem): MenuItem? {
        val response = apiService.updateMenuItem(id, menuItem)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteMenuItem(id: Int): Boolean {
        val response = apiService.deleteMenuItem(id)
        return response.isSuccessful
    }

    suspend fun deleteAllMenuItems(): String? {
        val response = apiService.deleteAllMenuItems()
        return if (response.isSuccessful) response.body() else null
    }
}