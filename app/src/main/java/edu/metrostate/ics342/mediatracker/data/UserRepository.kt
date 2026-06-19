package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.BuildConfig
import edu.metrostate.ics342.mediatracker.data.model.CreateUserRequest
import edu.metrostate.ics342.mediatracker.data.model.CreateUserResponse
import edu.metrostate.ics342.mediatracker.data.model.TokenRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val baseURL = "https://wjtzkgpxmxtzcczzbvrz.supabase.co/functions/v1/"

class UserRepository {

    // the api sends back fields we dont model (isFollowing etc), dont choke on them
    private val json = Json { ignoreUnknownKeys = true }

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(baseURL)
        .addConverterFactory(
            json.asConverterFactory(
                contentType = "application/json; charset=utf-8".toMediaType()
            )
        )
        .build()
        .create(ApiService::class.java)

    suspend fun createAccount(
        displayName: String,
        username: String,
        email: String,
        password: String,
    ): CreateUserResponse {
        val createUserRequest = CreateUserRequest(
            email        = email,
            password     = password,
            username     = username,
            displayName  = displayName,
            clientId     = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET,
        )
        return api.createUser(createUserRequest)
    }

    suspend fun login(email: String, password: String): TokenResponse {
        val tokenRequest = TokenRequest(
            grantType    = "password",
            email        = email,
            password     = password,
            clientId     = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET,
        )
        return api.login(tokenRequest)
    }
}
