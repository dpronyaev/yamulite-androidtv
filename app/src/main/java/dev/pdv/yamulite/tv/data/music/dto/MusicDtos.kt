package dev.pdv.yamulite.tv.data.music.dto

import androidx.compose.runtime.Immutable
import dev.pdv.yamulite.tv.data.network.FlexibleStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(val result: T)

@Serializable
data class AccountStatusDto(val account: AccountDto)

@Serializable
data class AccountDto(val uid: Long, val displayName: String? = null)

@Serializable
data class SearchDto(
    val tracks: SearchSectionDto<TrackDto>? = null,
    val artists: SearchSectionDto<ArtistDto>? = null,
    val albums: SearchSectionDto<AlbumDto>? = null,
)

@Serializable
data class SearchSectionDto<T>(val results: List<T> = emptyList())

@Immutable
@Serializable
data class TrackDto(
    @Serializable(with = FlexibleStringSerializer::class) val id: String,
    val title: String? = null,
    val coverUri: String? = null,
    val durationMs: Long? = null,
    val artists: List<ArtistShortDto> = emptyList(),
    val albums: List<AlbumShortDto> = emptyList(),
    val available: Boolean? = null,
)

@Immutable
@Serializable
data class ArtistShortDto(
    val id: Long = 0,
    val name: String = "",
)

@Immutable
@Serializable
data class AlbumShortDto(
    val id: Long = 0,
    val title: String? = null,
    val coverUri: String? = null,
)

@Immutable
@Serializable
data class ArtistDto(
    val id: Long = 0,
    val name: String = "",
    val cover: CoverDto? = null,
    val ogImage: String? = null,
)

@Immutable
@Serializable
data class AlbumDto(
    val id: Long = 0,
    val title: String = "",
    val coverUri: String? = null,
    val artists: List<ArtistShortDto> = emptyList(),
    val year: Int? = null,
)

@Immutable
@Serializable
data class CoverDto(val uri: String? = null, val type: String? = null)

@Serializable
data class LibraryDto(val library: LibraryTracksDto)

@Serializable
data class LibraryTracksDto(val tracks: List<LikedTrackRefDto> = emptyList())

@Serializable
data class LikedTrackRefDto(
    val id: String,
    val albumId: String? = null,
    val timestamp: String? = null,
)

@Serializable
data class RevisionDto(val revision: Long = 0)

@Serializable
data class ArtistBriefDto(
    val artist: ArtistDto = ArtistDto(),
    val popularTracks: List<TrackDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
)

@Serializable
data class AlbumWithTracksDto(
    val id: Long = 0,
    val title: String = "",
    val coverUri: String? = null,
    val artists: List<ArtistShortDto> = emptyList(),
    val year: Int? = null,
    val volumes: List<List<TrackDto>> = emptyList(),
)
