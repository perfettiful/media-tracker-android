package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.AddFavoriteRequest
import edu.metrostate.ics342.mediatracker.data.model.AddLibraryRequest
import edu.metrostate.ics342.mediatracker.data.model.AddReviewRequest
import edu.metrostate.ics342.mediatracker.data.model.Favorite
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.UpdateLibraryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("library")
    suspend fun getLibrary(@Query("status") status: String? = null): Response<List<LibraryItem>>

    @POST("library")
    suspend fun addToLibrary(@Body request: AddLibraryRequest): Response<LibraryItem>

    @PUT("library/{mediaId}")
    suspend fun updateLibraryItem(
        @Path("mediaId") mediaId: Int,
        @Body request: UpdateLibraryRequest,
    ): Response<LibraryItem>

    @DELETE("library/{mediaId}")
    suspend fun removeFromLibrary(@Path("mediaId") mediaId: Int): Response<Unit>

    @POST("reviews")
    suspend fun postReview(@Body request: AddReviewRequest): Response<Review>

    // same 404-means-no trick as the library check
    @GET("favorites/{mediaId}")
    suspend fun getFavorite(@Path("mediaId") mediaId: Int): Response<Favorite>

    @POST("favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<Favorite>

    @DELETE("favorites/{mediaId}")
    suspend fun removeFavorite(@Path("mediaId") mediaId: Int): Response<Unit>
}
