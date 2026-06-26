package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Media
import retrofit2.Response
import retrofit2.http.GET
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
}
