package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    val password: String,
    val username: String,
    val displayName: String,
    val clientId: String,
    val clientSecret: String,
)

@Serializable
data class TokenRequest(
    val grantType: String,
    val email: String,
    val password: String,
    val clientId: String,
    val clientSecret: String,
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile,
)
