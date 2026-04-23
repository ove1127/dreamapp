package com.dreamweddingstories.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Attempt to restore previous session silently on launch
        if (authRepository.hasActiveSession()) {
            restoreSession()
        }
    }

    // ── Sign in with 4-char code ─────────────────────────────────────────────

    fun signInWithCode(code: String) {
        if (code.isBlank() || code.length < 4) {
            _authState.value = UiState.Error("Please enter a valid 4-character code")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signInWithCode(code)
            _authState.value = result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    UiState.Success(user)
                },
                onFailure = { UiState.Error(it.message ?: "Invalid code") }
            )
        }
    }

    // ── Demo mode ─────────────────────────────────────────────────────────────

    fun signInAsDemoUser() {
        signInWithCode("DEMO")
    }

    // ── Session restore ───────────────────────────────────────────────────────

    fun restoreSession() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.restoreSession()
            _authState.value = result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    UiState.Success(user)
                },
                onFailure = {
                    // Session expired or invalid — go back to login
                    authRepository.clearSession()
                    UiState.Idle
                }
            )
        }
    }

    fun hasActiveSession(): Boolean = authRepository.hasActiveSession()

    // ── Misc ──────────────────────────────────────────────────────────────────

    fun clearAuthError() {
        if (_authState.value is UiState.Error) {
            _authState.value = UiState.Idle
        }
    }

    fun signOut() {
        authRepository.clearSession()
        _currentUser.value = null
        _authState.value = UiState.Idle
    }
}
