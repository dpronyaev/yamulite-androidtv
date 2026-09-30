package dev.pdv.yamulite.tv.ui.tv

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Renders [text] as a QR code. On a remote-only TV, this is what lets someone finish the
 * Yandex OAuth device-flow login on their phone instead of typing a URL with a D-pad.
 */
fun encodeQrCode(text: String, sizePx: Int = 480): ImageBitmap? = runCatching {
    val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 1)
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap[x, y] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    bitmap.asImageBitmap()
}.getOrNull()

@Composable
fun rememberQrCode(text: String, sizePx: Int = 480): ImageBitmap? =
    remember(text, sizePx) { encodeQrCode(text, sizePx) }
