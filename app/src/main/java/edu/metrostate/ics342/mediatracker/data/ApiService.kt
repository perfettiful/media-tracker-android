package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.data.model.CreateUserRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenResponse
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): UserProfile

    @POST("tokens")
    suspend fun login(@Body request: TokenRequest): TokenResponse
}
