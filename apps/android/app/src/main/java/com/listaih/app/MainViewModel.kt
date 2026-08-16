package com.listaih.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import com.listaih.app.ui.screens.home.ShoppingListUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentHouseholdId = MutableStateFlow<String?>(null)
    val currentHouseholdId = _currentHouseholdId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Observe lists from Room database (offline-first) converted to UI model
    @OptIn(ExperimentalCoroutinesApi::class)
    val shoppingLists = _currentHouseholdId.flatMapLatest { householdId ->
        if (householdId == null || householdId.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.getAllListsUiFlow(householdId)
        }
    }

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = preferences.getAccessToken()
            val householdId = preferences.getHouseholdId()
            if (token != null && householdId != null && householdId.isNotBlank()) {
                _isLoggedIn.value = true
                _currentHouseholdId.value = householdId
                _isLoading.value = true
                repository.syncLists(householdId)
                _isLoading.value = false
            }
        }
    }

    fun onLoginSuccess(householdId: String) {
        _isLoggedIn.value = true
        _currentHouseholdId.value = householdId
        _isLoading.value = true
        viewModelScope.launch {
            repository.syncLists(householdId)
            _isLoading.value = false
        }
    }

    fun onLogout() {
        _isLoggedIn.value = false
        _currentHouseholdId.value = null
    }

    fun addShoppingList(householdId: String, name: String, category: String?, listType: String = "PONTUAL") {
        viewModelScope.launch {
            repository.createList(householdId, name, category, listType).onSuccess { entity ->
                // List created and saved to Room, will appear via Flow
            }
        }
    }
}
