package me.weishu.kernelsu.ui.component.splash

import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.theme.isInDarkTheme

private const val EXIT_DURATION_MS = 360

/**
 * In-app animated splash shown after the system splash window.
 *
 * The platform splash icon animation is short and several OEM skins never start it, so the app
 * plays the original KernelSU outline animation itself: the vector draws its border and slides
 * the two squares together inside a scaling, fading card. Colors are theme-independent on
 * purpose: the theme is still settling while the process starts.
 */
@Composable
fun SplashScreen(
    isReady: Boolean,
    minDurationMs: Long = 1000L,
    onDismissed: () -> Unit = {},
) {
    var contentVisible by remember { mutableStateOf(false) }
    var exitTriggered by remember { mutableStateOf(false) }
    var splashDismissed by remember { mutableStateOf(false) }
    val currentOnDismissed by rememberUpdatedState(onDismissed)

    val dark = isInDarkTheme()

    val logoScale = remember { Animatable(0.78f) }
    val logoAlpha = remember { Animatable(0f) }

    // Mirrors splash_window_background for the active app theme so the handoff has no step.
    val background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA)
    val onBackground = if (dark) Color(0xFFF2F2F2) else Color(0xFF1A1A1A)
    val iconCard = if (dark) Color(0xFF1E1E1E) else Color.White

    LaunchedEffect(Unit) {
        contentVisible = true
        launch { logoAlpha.animateTo(1f, tween(260, easing = FastOutSlowInEasing)) }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    LaunchedEffect(isReady) {
        if (isReady) {
            delay(minDurationMs)
            exitTriggered = true
            delay(EXIT_DURATION_MS.toLong() + 20L)
            splashDismissed = true
            currentOnDismissed()
        }
    }

    if (splashDismissed) return

    AnimatedVisibility(
        visible = !exitTriggered,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(EXIT_DURATION_MS, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 1.05f,
                animationSpec = tween(EXIT_DURATION_MS, easing = FastOutSlowInEasing),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .pointerInput(Unit) {}
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top placeholder to balance the column layout
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = logoScale.value
                                scaleY = logoScale.value
                                alpha = logoAlpha.value
                            }
                            .size(112.dp)
                            .shadow(12.dp, shape = RoundedCornerShape(28.dp), clip = false)
                            .clip(RoundedCornerShape(28.dp))
                            .background(iconCard),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                ImageView(context).apply {
                                    contentDescription = context.getString(R.string.app_name)
                                    // The animated vector keeps its artwork inside a 60% safe area;
                                    // scale it up so the drawn logo fills the rounded card the way
                                    // the system splash icon does.
                                    scaleX = 1.55f
                                    scaleY = 1.55f
                                    ContextCompat.getDrawable(context, R.drawable.ic_launcher_splash)
                                        ?.let { drawable ->
                                            setImageDrawable(drawable)
                                            // Start after the view is attached so the drawable's
                                            // bounds are known and the animation is not dropped.
                                            post { (drawable as? android.graphics.drawable.Animatable)?.start() }
                                        }
                                }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(320, delayMillis = 120, easing = FastOutSlowInEasing)),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                color = onBackground,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.splash_tagline),
                                color = onBackground.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(380, delayMillis = 200, easing = FastOutSlowInEasing)),
                ) {
                    Text(
                        text = stringResource(id = R.string.splash_motto),
                        color = onBackground.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
