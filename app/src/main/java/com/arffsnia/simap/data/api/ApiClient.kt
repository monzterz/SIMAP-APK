package com.arffsnia.simap.data.api

import com.arffsnia.simap.BuildConfig
import com.arffsnia.simap.data.SessionManager
import com.arffsnia.simap.data.dto.ApiError
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(session: SessionManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val auth = okhttp3.Interceptor { chain ->
            val token = session.token
            val request = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val ok = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(ok)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Ambil pesan error JSON {"error":"..."} dari respons HTTP non-2xx. */
    fun errorMessage(t: Throwable): String {
        if (t is HttpException) {
            val body = runCatching {
                t.response()?.errorBody()?.string()
            }.getOrNull()
            val apiErr = runCatching {
                body?.let { Gson().fromJson(it, ApiError::class.java) }
            }.getOrNull()
            if (apiErr?.error != null) return apiErr.error!!
            return when (t.code()) {
                401 -> "unauthorized"
                429 -> "too_many_attempts"
                else -> "http_${t.code()}"
            }
        }
        return "network_error"
    }
}