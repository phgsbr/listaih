package com.listaih.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentHouseholdId = MutableStateFlow<String?>(null)
    val currentHouseholdId = _currentHouseholdId.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            // TODO: Check DataStore for saved auth token
            // _isLoggedIn.value = true
        }
    }

    fun onLoginSuccess(householdId: String) {
        _isLoggedIn.value = true
        _currentHouseholdId.value = householdId
    }

    fun onLogout() {
        _isLoggedIn.value = false
        _currentHouseholdId.value = null
    }
}