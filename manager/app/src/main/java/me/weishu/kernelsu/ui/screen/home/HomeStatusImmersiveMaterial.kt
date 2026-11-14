package me.weishu.kernelsu.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import java.io.File
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.HomeCardStickerOverlays
import me.weishu.kernelsu.ui.theme.CustomTextKey
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import me.weishu.kernelsu.ui.theme.backgroundAwareColor
import me.weishu.kernelsu.ui.theme.customText
import me.weishu.kernelsu.ui.theme.isInDarkTheme
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.shouldShowHomeCardImage

@Composable
fun ImmersiveStatusCardMaterial(
    state: HomeUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = HomeStatusStyle.coerceScale(state.homeStatusScale)
    val pillScale = HomeStatusStyle.coercePillScale(state.homePillScale)
    val widthFraction = HomeStatusStyle.coerceWidth(state.homeStatusWidthFraction)
    val colors = MaterialTheme.colorScheme
    val hasBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val showImage = shouldShowHomeCardImage(
        path = state.homeCardImagePath,
        hasCustomBackground = hasBackground,
        showWithoutBackground = state.showHomeCardImageWithoutBackground,
    )
    val containerColor = backgroundAwareColor(colors.surfaceContainer)
    val borderColor = if (isInDarkTheme()) {
        Color.White.copy(alpha = 0.3f)
    } else {
        colors.outline.copy(alpha = 0.28f)
    }
    val dotColor = when {
        state.isLateLoadMode -> colors.error
        state.isSafeMode -> colors.tertiary
        else -> colors.primary
    }
    val label = when {
        state.isLateLoadMode -> stringResource(R.string.jailbreak_mode)
        state.isSafeMode -> customText(CustomTextKey.HOME_TAG_SAFEMODE)
        else -> customText(CustomTextKey.HOME_STATUS_WORKING)
    }
    val mode = state.workingModeLabel
    val version = BuildConfig.VERSION_DISPLAY
    val meta = if (mode.isBlank()) version else "$mode · $version"
    val clickEnabled = !state.isLateLoadMode
    val bannerShape = RoundedCornerShape(HomeStatusStyle.BannerCorner)
    val banner = state.homeCardImageStyle == HomeCardImageStyle.BANNER && showImage

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (state.homeStatusAlignment == HomeStatusAlignment.END) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(HomeStatusStyle.ClusterGap),
    ) {
        Surface(
            modifier = Modifier
                .heightIn(min = HomeStatusStyle.PillTouchTarget),
            shape = CircleShape,
            color = containerColor,
            contentColor = colors.onSurface,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.18f)),
            enabled = clickEnabled,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier
                    .heightIn(
                        min = maxOf(
                            HomeStatusStyle.PillTouchTarget,
                            HomeStatusStyle.pillDp(HomeStatusStyle.PillMinHeight, pillScale),
                        )
                    )
                    .padding(
                        horizontal = HomeStatusStyle.pillDp(
                            HomeStatusStyle.PillHorizontalPadding,
                            pillScale,
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    HomeStatusStyle.pillDp(HomeStatusStyle.PillContentGap, pillScale)
                ),
            ) {
                Box(
                    modifier = Modifier
                        .size(HomeStatusStyle.pillDp(HomeStatusStyle.DotSize, pillScale))
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    color = colors.onSurface,
                    fontSize = HomeStatusStyle.pillSp(HomeStatusStyle.LabelSizeSp, pillScale),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .width(HomeStatusStyle.pillDp(HomeStatusStyle.DividerWidth, pillScale))
                        .height(HomeStatusStyle.pillDp(HomeStatusStyle.DividerHeight, pillScale))
                        .background(colors.onSurface.copy(alpha = 0.18f))
                )
                Text(
                    text = meta,
                    modifier = Modifier.weight(1f, fill = false),
                    color = colors.onSurfaceVariant,
                    fontSize = HomeStatusStyle.pillSp(HomeStatusStyle.MetaSizeSp, pillScale),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (banner) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(HomeStatusStyle.scaledDp(HomeStatusStyle.BannerHeight, scale))
                    .clip(bannerShape)
                    .background(containerColor)
                    .border(1.dp, borderColor, bannerShape)
                    .clickable(enabled = clickEnabled, onClick = onClick)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(state.homeCardImagePath))
                        .memoryCacheKey(homeCardImageCacheKey(state.homeCardImagePath))
                        .diskCacheKey(homeCardImageCacheKey(state.homeCardImagePath))
                        .crossfade(true)
                        .precision(Precision.EXACT)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier.fillMaxSize(),
                )
                HomeCardStickerOverlays(
                    stickers = state.homeCardStickers,
                    cardWidth = maxWidth,
                    cardHeight = maxHeight,
                    heightNormalized = true,
                )
            }
        }
    }
}
