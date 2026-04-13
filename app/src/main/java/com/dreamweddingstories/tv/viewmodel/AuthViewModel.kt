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
        loadCurrentUser()
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signIn(email.trim(), password)
            _authState.value = result.fold(
                onSuccess = {
                    _currentUser.value = it
                    UiState.Success(it)
                },
                onFailure = {
                    UiState.Error(it.message ?: "Unable to sign in")
                }
            )
        }
    }

    fun signInAsDemoUser() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            // Random email to ensure a fresh demo every time
            val randomId = (1000..9999).random()
            val demoEmail = "demo_$randomId@weddingstories.com"
            val demoPassword = "password123"
            
            // 1. Create account
            val demoVideoIds = listOf("demo_1", "demo_2", "demo_3")
            val signupResult = authRepository.signUp(demoEmail, demoPassword, "Demo Guest", demoVideoIds)
            
            signupResult.fold(
                onSuccess = { user ->
                    // 2. Populate sample data
                    val dataResult = authRepository.populateDemoData(user.uid)
                    dataResult.onSuccess {
                        _currentUser.value = user
                        _authState.value = UiState.Success(user)
                    }.onFailure {
                        _authState.value = UiState.Error("Account created but failed to load data: ${it.message}")
                    }
                },
                onFailure = {
                    _authState.value = UiState.Error("Failed to create demo account: ${it.message}")
                }
            )
        }
    }

    fun loadCurrentUser() {
        val firebaseUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            val result = authRepository.getUserProfile(firebaseUser.uid)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = UiState.Success(user)
            }.onFailure {
                // If profile not found, just clear session
                signOut()
            }
        }
    }

    fun clearAuthError() {
        if (_authState.value is UiState.Error) {
            _authState.value = UiState.Idle
        }
    }

    fun hasActiveSession(): Boolean = authRepository.getCurrentUser() != null

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _authState.value = UiState.Idle
    }
}

