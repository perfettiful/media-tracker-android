package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val userId: String,
    val mediaId: Int,
    val rating: Int,
    val reviewText: String? = null,
    val createdAt: String,
    val user: UserProfile? = null,
    val media: Media? = null
)

// body for POST /reviews
@Serializable
data class AddReviewRequest(
    val mediaId: Int,
    val rating: Int,
    val reviewText: String? = null,
    val shareToFeed: Boolean = true,
)
