package dev.pdv.yamulite.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TVs are viewed from ~3 meters ("10-foot UI") — every type scale step below is bumped up
// from the phone app's defaults so body text stays legible from the couch.
private val TvTypography = Typography().let { base ->
    base.copy(
        headlineSmall = base.headlineSmall.copy(fontSize = 30.sp, lineHeight = 38.sp),
        titleLarge = base.titleLarge.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontSize = 20.sp, lineHeight = 26.sp),
        bodyLarge = base.bodyLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
        bodySmall = base.bodySmall.copy(fontSize = 15.sp, lineHeight = 20.sp),
        labelLarge = base.labelLarge.copy(fontSize = 17.sp),
    )
}

private val TvDarkScheme = darkColorScheme(
    primary = Color(0xFFFFCC00),
    onPrimary = Color.Black,
    onPrimaryContainer = Color(0xFFFFE680),
    secondaryContainer = Color(0xFF3A3A3A),
    background = Color(0xFF121212),
    surface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFF2A2A2A),
)

private val TvLightScheme = lightColorScheme(
    primary = Color(0xFFB8860B),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF7A5B00),
    secondaryContainer = Color(0xFFE8E8E8),
)

@Composable
fun YaMuLiteTvTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TvDarkScheme else TvLightScheme
    MaterialTheme(colorScheme = colors, typography = TvTypography, content = content)
}
