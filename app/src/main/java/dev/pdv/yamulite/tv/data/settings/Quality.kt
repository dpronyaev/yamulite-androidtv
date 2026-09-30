package dev.pdv.yamulite.tv.data.settings

enum class Quality(val bitrate: Int, val label: String) {
    Low(64, "Низкое (64 kbps)"),
    Normal(128, "Среднее (128 kbps)"),
    High(192, "Высокое (192 kbps)"),
    Best(320, "Максимум (320 kbps)"),
}
