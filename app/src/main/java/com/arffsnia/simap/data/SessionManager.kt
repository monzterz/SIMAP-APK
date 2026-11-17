package com.arffsnia.simap.data

import android.content.Context
import com.arffsnia.simap.data.dto.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("simap_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun saveSession(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun currentUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(json, User::class.java) }.getOrNull()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER = "user_json"
    }
}