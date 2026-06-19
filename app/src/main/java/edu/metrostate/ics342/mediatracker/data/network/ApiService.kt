package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.CreateUserRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // register doesnt need the body back, just the status
    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<Unit>

    @POST("tokens")
    suspend fun login(@Body request: TokenRequest): Response<TokenResponse>
}
