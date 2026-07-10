package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review

interface MediaRepository {
    suspend fun searchMedia(query: String?, type: String?, after: String?): SearchPage
    suspend fun getMediaDetail(id: Int): DetailResult

    // null means not in the library (or we couldnt check, same ui either way)
    suspend fun getLibraryStatus(mediaId: Int): LibraryStatus?

    // returns the resulting status, null if the add failed
    suspend fun addToLibrary(mediaId: Int, status: LibraryStatus): LibraryStatus?

    suspend fun postReview(
        mediaId: Int,
        rating: Int,
        reviewText: String?,
        shareToFeed: Boolean,
    ): PostReviewResult
}

sealed interface PostReviewResult {
    data object Success : PostReviewResult
    // the api allows one review per user per media, a 409 means yours already exists
    data object AlreadyReviewed : PostReviewResult
    data object NetworkError : PostReviewResult
    data object UnknownError : PostReviewResult
}

sealed interface DetailResult {
    data class Success(
        val detail: MediaDetail,
        val reviews: List<Review>,
    ) : DetailResult

    data object NetworkError : DetailResult
    data object UnknownError : DetailResult
}

sealed interface SearchPage {
    data class Success(
        val items: List<Media>,
        val nextCursor: String?,
        val hasMore: Boolean,
    ) : SearchPage

    data object NetworkError : SearchPage
    data object UnknownError : SearchPage
}
