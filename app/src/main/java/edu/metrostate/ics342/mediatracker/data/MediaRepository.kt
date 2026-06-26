package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.data.model.Media

interface MediaRepository {
    suspend fun searchMedia(query: String?, type: String?, after: String?): SearchPage
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
