package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.DetailResult
import edu.metrostate.ics342.mediatracker.data.LibraryResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.PostReviewResult
import edu.metrostate.ics342.mediatracker.data.SearchPage
import edu.metrostate.ics342.mediatracker.data.model.AddFavoriteRequest
import edu.metrostate.ics342.mediatracker.data.model.AddLibraryRequest
import edu.metrostate.ics342.mediatracker.data.model.AddReviewRequest
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import java.io.IOException

class DefaultMediaRepository(
    private val service: MediaApiService = RetrofitInstance.mediaApiService,
) : MediaRepository {

    override suspend fun searchMedia(query: String?, type: String?, after: String?): SearchPage {
        return try {
            val response = service.searchMedia(
                query = query?.ifBlank { null },
                type  = type,
                after = after,
            )
            val items = response.body()
            if (response.isSuccessful && items != null) {
                SearchPage.Success(
                    items      = items,
                    nextCursor = response.headers()["X-Next-Cursor"],
                    hasMore    = response.headers()["X-Has-More"] == "true",
                )
            } else {
                SearchPage.UnknownError
            }
        } catch (e: IOException) {
            SearchPage.NetworkError
        } catch (e: Exception) {
            SearchPage.UnknownError
        }
    }

    override suspend fun getMediaDetail(id: Int): DetailResult {
        return try {
            val detailResponse = service.getMediaDetail(id)
            val detail = detailResponse.body()
            if (!detailResponse.isSuccessful || detail == null) {
                return DetailResult.UnknownError
            }
            // reviews failing shouldnt sink the whole screen, degrade to none
            val reviews = try {
                service.getReviews(id).body() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            DetailResult.Success(detail, reviews)
        } catch (e: IOException) {
            DetailResult.NetworkError
        } catch (e: Exception) {
            DetailResult.UnknownError
        }
    }

    override suspend fun getLibraryStatus(mediaId: Int): LibraryStatus? {
        return try {
            val response = service.getLibraryItem(mediaId)
            // 404 here is the normal "not added yet" answer, not an error
            if (response.isSuccessful) response.body()?.status else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addToLibrary(mediaId: Int, status: LibraryStatus): LibraryStatus? {
        return try {
            val response = service.addToLibrary(
                AddLibraryRequest(mediaId = mediaId, status = status.toApiString())
            )
            when {
                response.isSuccessful   -> response.body()?.status ?: status
                // 409 means its already in the library, go ask what status it has
                response.code() == 409  -> getLibraryStatus(mediaId)
                else                    -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun isFavorited(mediaId: Int): Boolean {
        return try {
            // 404 just means not favorited, same deal as the library check
            service.getFavorite(mediaId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addFavorite(mediaId: Int): Boolean {
        return try {
            val response = service.addFavorite(AddFavoriteRequest(mediaId))
            // 409 means already saved, which is the state we wanted anyway
            response.isSuccessful || response.code() == 409
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getLibrary(status: LibraryStatus?): LibraryResult {
        return try {
            val response = service.getLibrary(status?.toApiString())
            val items = response.body()
            if (response.isSuccessful && items != null) {
                LibraryResult.Success(items)
            } else {
                LibraryResult.UnknownError
            }
        } catch (e: IOException) {
            LibraryResult.NetworkError
        } catch (e: Exception) {
            LibraryResult.UnknownError
        }
    }

    override suspend fun postReview(
        mediaId: Int,
        rating: Int,
        reviewText: String?,
        shareToFeed: Boolean,
    ): PostReviewResult {
        return try {
            val response = service.postReview(
                AddReviewRequest(
                    mediaId     = mediaId,
                    rating      = rating,
                    reviewText  = reviewText?.ifBlank { null },
                    shareToFeed = shareToFeed,
                )
            )
            when {
                response.isSuccessful  -> PostReviewResult.Success
                response.code() == 409 -> PostReviewResult.AlreadyReviewed
                else                   -> PostReviewResult.UnknownError
            }
        } catch (e: IOException) {
            PostReviewResult.NetworkError
        } catch (e: Exception) {
            PostReviewResult.UnknownError
        }
    }
}
