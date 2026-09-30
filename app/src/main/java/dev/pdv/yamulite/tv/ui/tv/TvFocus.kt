package dev.pdv.yamulite.tv.ui.tv

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Base building block for every clickable row/card in the app. Compose's own `clickable`
 * modifier already fires onClick for a D-pad OK/Enter press once focused — the only thing
 * missing on a remote-only (no touchscreen) TV is a *visible* focus state, since the default
 * ripple is easy to miss on a 10-foot display. This makes focus loud: a scale bump + a bright
 * border + a tinted background, all driven off [onFocusChanged] rather than any TV-specific
 * library.
 */
@Composable
fun TvSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(10.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    focusedColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    focusedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    focusRequester: FocusRequester? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f, label = "tvScale")
    val bg by animateColorAsState(if (isFocused) focusedColor else color, label = "tvBg")
    val fg by animateColorAsState(if (isFocused) focusedContentColor else contentColor, label = "tvFg")
    val borderColor by animateColorAsState(
        if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f) else Color.Transparent,
        label = "tvBorder",
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bg)
            .border(if (isFocused) 3.dp else 0.dp, borderColor, shape)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides fg) {
            content()
        }
    }
}

/** Requests focus once, the first time this composable enters the composition. */
@Composable
fun FocusRequester.requestOnce() {
    LaunchedEffect(Unit) { requestFocus() }
}
