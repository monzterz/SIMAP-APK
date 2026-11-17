package com.arffsnia.simap.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.repo.AppResult
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repo = SimapApp.instance.authRepo

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit) {
        if (loading) return
        if (username.isBlank() || password.isBlank()) {
            error = "Username dan password wajib diisi"
            return
        }
        viewModelScope.launch {
            loading = true
            error = null
            when (val r = repo.login(username, password)) {
                is AppResult.Success -> onSuccess()
                is AppResult.Error -> error = com.arffsnia.simap.ui.mapError(r.message)
            }
            loading = false
        }
    }

    fun clearError() { error = null }
}