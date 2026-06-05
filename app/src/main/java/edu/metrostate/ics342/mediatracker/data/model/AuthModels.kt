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
data class CreateUserResponse(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val trackedCount: Int = 0,
    val createdAt: String? = null,
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
    val user: CreateUserResponse,
)
