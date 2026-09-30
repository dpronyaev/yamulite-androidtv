package dev.pdv.yamulite.tv.data.settings

enum class CodecPreference(val label: String) {
    Mp3Only("Только MP3"),
    AacPreferred("AAC / MP3"),
    Unrestricted("Лучший доступный (вкл. FLAC)"),
}
