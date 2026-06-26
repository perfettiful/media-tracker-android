package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.TokenStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    // MediaTrackerApp sets this on startup so the interceptor can read the token
    lateinit var tokenStore: TokenStore

    private val json = Json { ignoreUnknownKeys = true }

    // lazy so tokenStore is in place before the client gets built on first call
    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

    val mediaApiService: MediaApiService by lazy { retrofit.create(MediaApiService::class.java) }
}
