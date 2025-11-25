package com.osg.restaurantcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.repository.MenuItemRepository
import com.osg.restaurantcompanionapp.network.ApiService
import com.osg.restaurantcompanionapp.network.retrofit
import kotlinx.coroutines.launch

class MenuItemViewModel : ViewModel() {
    private val _menuItemRepository = MenuItemRepository(retrofit.create(ApiService::class.java))

    private val _menuItemsLiveData = MutableLiveData<List<MenuItem>?>()
    val menuItemsLiveData: LiveData<List<MenuItem>?> = _menuItemsLiveData

    private val _menuItemLiveData = MutableLiveData<MenuItem?>()
    val menuItemLiveData: LiveData<MenuItem?> = _menuItemLiveData

    private val _createMenuItemResult = MutableLiveData<MenuItem?>()
    val createMenuItemResult: LiveData<MenuItem?> = _createMenuItemResult

    private val _updateMenuItemResult = MutableLiveData<MenuItem?>()
    val updateMenuItemResult: LiveData<MenuItem?> = _updateMenuItemResult

    private val _deleteMenuItemResult = MutableLiveData<Boolean>()
    val deleteMenuItemResult: LiveData<Boolean> = _deleteMenuItemResult

    private val _showAddScreen = MutableLiveData<Boolean>()
    val showAddScreen: LiveData<Boolean> = _showAddScreen

    fun onAdd() {
        _showAddScreen.value = true
    }

    fun onAddScreenShown() {
        _showAddScreen.value = false
    }

    fun fetchMenuItems() {
        viewModelScope.launch {
            val menuItems = _menuItemRepository.getMenuItems()
            _menuItemsLiveData.postValue(menuItems)
        }
    }

    fun fetchMenuItemById(id: Int) {
        viewModelScope.launch {
            val menuItem = _menuItemRepository.getMenuItemById(id)
            _menuItemLiveData.postValue(menuItem)
        }
    }

    fun createMenuItem(menuItem: MenuItem) {
        viewModelScope.launch {
            val result = _menuItemRepository.createMenuItem(menuItem)
            _createMenuItemResult.postValue(result)
        }
    }

    fun updateMenuItem(id: Int, menuItem: MenuItem) {
        viewModelScope.launch {
            val result = _menuItemRepository.updateMenuItem(id, menuItem)
            _updateMenuItemResult.postValue(result)
        }
    }

    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            val result = _menuItemRepository.deleteMenuItem(id)
            _deleteMenuItemResult.postValue(result)
        }
    }

    fun resetCreateMenuItemResult() {
        _createMenuItemResult.value = null
    }
}