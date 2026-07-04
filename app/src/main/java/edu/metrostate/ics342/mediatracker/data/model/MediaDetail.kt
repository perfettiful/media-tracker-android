package edu.metrostate.ics342.mediatracker.data.model

import android.content.Context
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.Serializable

/**
 * Full detail payload returned by GET /media/{id}.
 *
 * The API models this as `Media` + extra fields (OpenAPI `allOf`), but the JSON
 * arrives as one flat object, so this is a flat data class carrying every field.
 * Type-specific fields are nullable because only some apply to each media type:
 * books have [pageCount] / [isbn], movies have [runtimeMinutes], shows have
 * [seasonCount] / [episodeCount].
 */
@Serializable
data class MediaDetail(
    val id: Int,
    val mediaType: String,             // "book", "movie", or "show"
    val title: String,
    val author: String? = null,        // books
    val director: String? = null,      // movies
    val creator: String? = null,       // shows
    val network: String? = null,       // shows
    val coverUrl: String? = null,
    val publishedYear: Int? = null,
    val averageRating: Float = 0f,
    val ratingCount: Int = 0,
    val genres: List<String> = emptyList(),
    // ── MediaDetail-only fields ──────────────────────────────────────────────
    val description: String? = null,
    val pageCount: Int? = null,        // books
    val runtimeMinutes: Int? = null,   // movies
    val seasonCount: Int? = null,      // shows
    val episodeCount: Int? = null,     // shows
    val isbn: String? = null,          // books
    val reviewCount: Int = 0
)

/** Returns a human-readable credit line appropriate for the media type. */
fun MediaDetail.creatorCredit(context: Context): String = when (mediaType) {
    "book"  -> author   ?: context.getString(R.string.media_unknown_author)
    "movie" -> director ?: context.getString(R.string.media_unknown_director)
    "show"  -> creator  ?: context.getString(R.string.media_unknown_creator)
    else    -> ""
}
