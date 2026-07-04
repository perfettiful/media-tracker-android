package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.DetailResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.SearchPage
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
}
