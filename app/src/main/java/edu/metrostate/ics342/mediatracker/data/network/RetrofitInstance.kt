package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    // api returns fields we dont model (isFollowing etc), dont choke on them
    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json; charset=utf-8".toMediaType()))
        .build()
        .create(ApiService::class.java)
}
