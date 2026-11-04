package com.example.chatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.chatbot.resources.Res
import com.example.chatbot.resources.app_name
import com.example.chatbot.resources.ic_arrow_left
import com.example.chatbot.resources.ic_arrow_up_right
import com.example.chatbot.resources.ic_copy
import com.example.chatbot.resources.ic_layers
import com.example.chatbot.resources.ic_search
import com.example.chatbot.resources.ic_sparkles
import com.example.chatbot.ui.theme.ChatbotTheme
import com.example.chatbot.ui.theme.Dimens
import com.example.chatbot.ui.theme.DocuMindTheme

/**
 * Violet pill button used for every primary action (Start, View original page, Sign in, Generate).
 * While [loading] it shows a spinner instead of the leading icon and ignores clicks.
 */
@Composable
fun DmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = DocuMindTheme.shapes.pill
    val clickable = enabled && !loading
    Row(
        modifier = modifier
            .heightIn(min = Dimens.primaryButtonHeight)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clickable(enabled = clickable, role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.space20, vertical = Dimens.space8),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space10, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = MaterialTheme.colorScheme.onPrimary
        if (loading) {
            CircularProgressIndicator(
                color = tint,
                strokeWidth = Dimens.hairline * 2,
                modifier = Modifier.size(Dimens.iconXl),
            )
        } else {
            leadingIcon?.let { Icon(it, contentDescription = null, tint = tint, modifier = Modifier.size(Dimens.iconXl)) }
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = tint)
        trailingIcon?.let { Icon(it, contentDescription = null, tint = tint, modifier = Modifier.size(Dimens.iconXl)) }
    }
}

private const val DISABLED_ALPHA = 0.5f

/** Violet text link (secondary actions). */
@Composable
fun DmTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.space8),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}

enum class DmIconButtonStyle { Plain, Outlined, Filled }

/** 48 dp icon-only button: back, more options, copy, close, page navigation, send (filled). */
@Composable
fun DmIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DmIconButtonStyle = DmIconButtonStyle.Plain,
    iconSize: Dp = Dimens.icon,
    tint: Color = if (style == DmIconButtonStyle.Filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
) {
    val shape = if (style == DmIconButtonStyle.Outlined) MaterialTheme.shapes.small else DocuMindTheme.shapes.pill
    Box(
        modifier = modifier
            .size(Dimens.minTouchTarget)
            .clip(shape)
            .then(
                when (style) {
                    DmIconButtonStyle.Outlined -> Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(Dimens.hairline, MaterialTheme.colorScheme.outline, shape)
                    DmIconButtonStyle.Filled -> Modifier.background(MaterialTheme.colorScheme.primary)
                    DmIconButtonStyle.Plain -> Modifier
                },
            )
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

enum class DmIconTileStyle { Accent, Soft, Neutral, Outlined }

/** Rounded tile holding an icon or a short label: brand mark, assistant mark, [1] marker, tab icons. */
@Composable
fun DmIconTile(
    modifier: Modifier = Modifier,
    size: Dp = Dimens.iconTileSmall,
    style: DmIconTileStyle = DmIconTileStyle.Accent,
    shape: Shape = DocuMindTheme.shapes.tile,
    content: @Composable BoxScope.(contentColor: Color) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val (container, contentColor) = when (style) {
        DmIconTileStyle.Accent -> colors.primary to colors.onPrimary
        DmIconTileStyle.Soft -> colors.primaryContainer to colors.onPrimaryContainer
        DmIconTileStyle.Neutral -> colors.background to colors.onSurfaceVariant
        DmIconTileStyle.Outlined -> colors.background to colors.primary
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(container)
            .then(if (style == DmIconTileStyle.Outlined) Modifier.border(Dimens.hairline, colors.outline, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        content(contentColor)
    }
}

/** Icon variant of [DmIconTile]. */
@Composable
fun DmIconTile(
    icon: Painter,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.iconTileSmall,
    iconSize: Dp = Dimens.iconM,
    style: DmIconTileStyle = DmIconTileStyle.Accent,
    shape: Shape = DocuMindTheme.shapes.tile,
) {
    DmIconTile(modifier = modifier, size = size, style = style, shape = shape) { tint ->
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Circular avatar with the account initials. */
@Composable
fun DmAvatar(
    initials: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    // 40 dp circle inside a 48 dp touch target.
    Box(
        modifier = modifier
            .size(Dimens.minTouchTarget)
            .clip(DocuMindTheme.shapes.pill)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        DmIconTile(size = Dimens.avatar, style = DmIconTileStyle.Soft, shape = DocuMindTheme.shapes.pill) { color ->
            Text(text = initials, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

/** DocuMind brand mark followed by the app name. */
@Composable
fun BrandLockup(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DmIconTile(
            icon = painterResource(Res.drawable.ic_layers),
            size = Dimens.brandTile,
            iconSize = Dimens.iconXl,
        )
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

enum class DmPillFieldStyle { Surface, Recessed }

/**
 * Pill-shaped text field: search, chat composer and login fields.
 * [Surface] is white (on cream screens); [Recessed] is cream (on white bars).
 * A non-null [errorText] draws the border in the error colour, shows the message below the
 * field and exposes it to screen readers.
 */
@Composable
fun DmPillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    style: DmPillFieldStyle = DmPillFieldStyle.Surface,
    minHeight: Dp = Dimens.fieldHeight,
    leadingIcon: Painter? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    contentPadding: PaddingValues = PaddingValues(start = Dimens.space16, end = Dimens.space16),
    enabled: Boolean = true,
    errorText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    if (errorText == null) {
        PillTextField(
            value, onValueChange, placeholder, modifier, style, minHeight, leadingIcon, singleLine, maxLines,
            keyboardOptions, keyboardActions, visualTransformation, contentPadding, enabled, isError = false, trailing,
        )
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.space4)) {
            PillTextField(
                value, onValueChange, placeholder,
                Modifier.fillMaxWidth().semantics { error(errorText) },
                style, minHeight, leadingIcon, singleLine, maxLines,
                keyboardOptions, keyboardActions, visualTransformation, contentPadding, enabled, isError = true, trailing,
            )
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = Dimens.space16),
            )
        }
    }
}

@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    style: DmPillFieldStyle,
    minHeight: Dp,
    leadingIcon: Painter?,
    singleLine: Boolean,
    maxLines: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation,
    contentPadding: PaddingValues,
    enabled: Boolean,
    isError: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val shape = DocuMindTheme.shapes.pill
    val textStyle = MaterialTheme.typography.bodyMedium
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = textStyle.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .clip(shape)
                    .background(if (style == DmPillFieldStyle.Surface) colors.surface else colors.background)
                    .border(if (isError) Dimens.hairline * 2 else Dimens.hairline, if (isError) colors.error else colors.outline, shape)
                    .alpha(if (enabled) 1f else DISABLED_ALPHA)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.let {
                    Icon(it, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(Dimens.iconXl))
                }
                Box(modifier = Modifier.weight(1f).padding(vertical = Dimens.space12)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                trailing?.invoke()
            }
        },
    )
}

@Preview
@Composable
private fun ControlsPreview() {
    ChatbotTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.space16),
        ) {
            BrandLockup()
            DmAvatar(initials = "AL", contentDescription = "Account")
            DmPrimaryButton(text = "Start", onClick = {}, modifier = Modifier.fillMaxWidth())
            DmPrimaryButton(
                text = "View original page",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = painterResource(Res.drawable.ic_arrow_up_right),
            )
            DmTextButton(text = "Forgot your password?", onClick = {})
            DmPillField(
                value = "",
                onValueChange = {},
                placeholder = "Search your documents",
                leadingIcon = painterResource(Res.drawable.ic_search),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.space8)) {
                DmIconButton(painterResource(Res.drawable.ic_arrow_left), "Back", onClick = {})
                DmIconButton(painterResource(Res.drawable.ic_copy), "Copy", onClick = {}, style = DmIconButtonStyle.Outlined)
                DmIconTile(icon = painterResource(Res.drawable.ic_sparkles))
                DmIconTile(style = DmIconTileStyle.Soft, size = Dimens.citationMarker) { color ->
                    Text("[1]", style = MaterialTheme.typography.labelSmall, color = color)
                }
            }
        }
    }
}
