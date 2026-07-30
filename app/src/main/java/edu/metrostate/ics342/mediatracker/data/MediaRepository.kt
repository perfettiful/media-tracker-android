package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.data.model.Favorite
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Priority
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.UpdatePriorityRequest

interface MediaRepository {
    suspend fun searchMedia(query: String?, type: String?, after: String?): SearchPage
    suspend fun getMediaDetail(id: Int): DetailResult

    // best effort, failures degrade to an empty list instead of sinking the screen
    suspend fun getReviews(mediaId: Int): List<Review>

    // null means not in the library (or we couldnt check, same ui either way)
    suspend fun getLibraryStatus(mediaId: Int): LibraryStatus?

    // returns the resulting status, null if the add failed
    suspend fun addToLibrary(mediaId: Int, status: LibraryStatus): LibraryStatus?

    // favorites are a flag, not a shelf. 404 reads as false
    suspend fun isFavorited(mediaId: Int): Boolean

    // true once its saved, including the already-saved 409 case
    suspend fun addFavorite(mediaId: Int): Boolean

    // true when the server confirms, a 404 counts since its already gone
    suspend fun removeFavorite(mediaId: Int): Boolean

    // null means the fetch failed, the profile just hides the section
    suspend fun getFavorites(): List<Favorite>?

    suspend fun getLibrary(status: LibraryStatus?): LibraryResult

    suspend fun updateLibraryStatus(mediaId: Int, status: LibraryStatus): Boolean

    suspend fun removeFromLibrary(mediaId: Int): Boolean

    // null means the fetch failed, sorted by orderIndex before it comes back
    suspend fun getPriorities(): List<Priority>?

    suspend fun setPriority(request: UpdatePriorityRequest): Boolean

    suspend fun postReview(
        mediaId: Int,
        rating: Int,
        reviewText: String?,
        shareToFeed: Boolean,
    ): PostReviewResult
}

sealed interface LibraryResult {
    data class Success(val items: List<LibraryItem>) : LibraryResult
    data object NetworkError : LibraryResult
    data object UnknownError : LibraryResult
}

sealed interface PostReviewResult {
    data object Success : PostReviewResult
    // the api allows one review per user per media, a 409 means yours already exists
    data object AlreadyReviewed : PostReviewResult
    data object NetworkError : PostReviewResult
    data object UnknownError : PostReviewResult
}

sealed interface DetailResult {
    data class Success(val detail: MediaDetail) : DetailResult

    // a 404 on /media/{id} is a real "this doesnt exist", unlike the library check
    data object NotFound : DetailResult
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
