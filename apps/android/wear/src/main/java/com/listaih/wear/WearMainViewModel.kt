package com.listaih.wear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearMainViewModel @Inject constructor() : ViewModel() {

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId = _currentListId.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    init {
        checkConnection()
    }

    private fun checkConnection() {
        viewModelScope.launch {
            // TODO: Check connection to phone app
            _isConnected.value = true
        }
    }

    fun setCurrentList(listId: String?) {
        _currentListId.value = listId
    }
}