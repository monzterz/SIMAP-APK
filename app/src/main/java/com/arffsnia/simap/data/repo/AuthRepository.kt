package com.arffsnia.simap.data.repo

import com.arffsnia.simap.data.SessionManager
import com.arffsnia.simap.data.api.ApiClient
import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.dto.LoginRequest
import com.arffsnia.simap.data.dto.User

class AuthRepository(
    private val api: ApiService,
    private val session: SessionManager,
) {
    suspend fun login(username: String, password: String): AppResult<User> {
        return try {
            val resp = api.login(LoginRequest(username.trim(), password))
            session.saveSession(resp.token, resp.user)
            AppResult.Success(resp.user)
        } catch (e: Exception) {
            AppResult.Error(ApiClient.errorMessage(e))
        }
    }

    /** Verifikasi token masih valid. */
    suspend fun validateToken(): Boolean {
        return try {
            api.me()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun currentUser(): User? = session.currentUser()

    fun logout() {
        session.clear()
    }
}