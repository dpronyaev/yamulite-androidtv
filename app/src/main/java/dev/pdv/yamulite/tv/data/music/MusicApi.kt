package dev.pdv.yamulite.tv.data.music

import dev.pdv.yamulite.tv.data.music.dto.AccountStatusDto
import dev.pdv.yamulite.tv.data.music.dto.AlbumWithTracksDto
import dev.pdv.yamulite.tv.data.music.dto.ApiResponse
import dev.pdv.yamulite.tv.data.music.dto.ArtistBriefDto
import dev.pdv.yamulite.tv.data.music.dto.DownloadInfoDto
import dev.pdv.yamulite.tv.data.music.dto.LibraryDto
import dev.pdv.yamulite.tv.data.music.dto.RevisionDto
import dev.pdv.yamulite.tv.data.music.dto.SearchDto
import dev.pdv.yamulite.tv.data.music.dto.TrackDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApi {

    @GET("account/status")
    suspend fun accountStatus(): ApiResponse<AccountStatusDto>

    @GET("search")
    suspend fun search(
        @Query("text") text: String,
        @Query("type") type: String,
        @Query("page") page: Int = 0,
        @Query("nocorrect") nocorrect: Boolean = true,
    ): ApiResponse<SearchDto>

    @GET("users/{uid}/likes/tracks")
    suspend fun likedTracks(@Path("uid") uid: Long): ApiResponse<LibraryDto>

    @FormUrlEncoded
    @POST("tracks")
    suspend fun tracksByIds(
        @Field("track-ids") trackIds: String,
        @Field("with-positions") withPositions: Boolean = false,
    ): ApiResponse<List<TrackDto>>

    @FormUrlEncoded
    @POST("users/{uid}/likes/tracks/add-multiple")
    suspend fun likeTracks(
        @Path("uid") uid: Long,
        @Field("track-ids") trackIds: String,
    ): ApiResponse<RevisionDto>

    @FormUrlEncoded
    @POST("users/{uid}/likes/tracks/remove")
    suspend fun unlikeTracks(
        @Path("uid") uid: Long,
        @Field("track-ids") trackIds: String,
    ): ApiResponse<RevisionDto>

    @GET("tracks/{id}/download-info")
    suspend fun downloadInfo(@Path("id") trackId: String): ApiResponse<List<DownloadInfoDto>>

    @GET("artists/{id}/brief-info")
    suspend fun artistBriefInfo(@Path("id") artistId: Long): ApiResponse<ArtistBriefDto>

    @GET("albums/{id}/with-tracks")
    suspend fun albumWithTracks(@Path("id") albumId: Long): ApiResponse<AlbumWithTracksDto>
}
