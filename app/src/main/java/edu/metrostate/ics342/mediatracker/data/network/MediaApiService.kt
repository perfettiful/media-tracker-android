package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.AddLibraryRequest
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MediaApiService {

    // Response<List<Media>> so we can read the X-Next-Cursor / X-Has-More headers
    @GET("media")
    suspend fun searchMedia(
        @Query("query") query: String? = null,
        @Query("type")  type: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("after") after: String? = null,
    ): Response<List<Media>>

    @GET("media/{id}")
    suspend fun getMediaDetail(@Path("id") id: Int): Response<MediaDetail>

    @GET("reviews")
    suspend fun getReviews(@Query("mediaId") mediaId: Int): Response<List<Review>>

    // Response so a 404 (not in library, totally normal) doesnt throw
    @GET("library/{mediaId}")
    suspend fun getLibraryItem(@Path("mediaId") mediaId: Int): Response<LibraryItem>

    @POST("library")
    suspend fun addToLibrary(@Body request: AddLibraryRequest): Response<LibraryItem>
}
