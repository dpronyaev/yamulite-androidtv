package dev.pdv.yamulite.tv.data.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        require(decoder is JsonDecoder) { "FlexibleStringSerializer only supports JSON" }
        val el = decoder.decodeJsonElement()
        return (el as? JsonPrimitive)?.content ?: error("Expected JSON primitive, got $el")
    }

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}
