package dev.pdv.yamulite.tv.data.music.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadInfoDto(
    val codec: String,
    @SerialName("bitrateInKbps") val bitrateInKbps: Int,
    @SerialName("downloadInfoUrl") val downloadInfoUrl: String,
    val direct: Boolean? = null,
)
