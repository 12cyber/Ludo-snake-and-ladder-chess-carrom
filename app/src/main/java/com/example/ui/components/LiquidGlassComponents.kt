package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MainNavTab
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Royal Indigo background with soft floating light particles and subtle radial aura.
 */
@Composable
fun PlayverseAnimatedBackground(
    darkMode: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    val topColor = if (darkMode) Color(0xFF070C1F) else Color(0xFFE0E7FF)
    val midColor = if (darkMode) Color(0xFF0E183D) else Color(0xFFEEF2FF)
    val bottomColor = if (darkMode) Color(0xFF161342) else Color(0xFFDbeafe)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, midColor, bottomColor)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Ambient lighting orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3B82F6).copy(alpha = if (darkMode) 0.18f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.12f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.85f, h * 0.12f),
                radius = w * 0.65f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = if (darkMode) 0.15f else 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.55f),
                    radius = w * 0.6f
                ),
                center = Offset(w * 0.15f, h * 0.55f),
                radius = w * 0.6f
            )

            // Soft animated particles
            val particlePositions = listOf(
                Triple(0.15f, 0.18f, 3.5f),
                Triple(0.78f, 0.24f, 4.5f),
                Triple(0.32f, 0.42f, 2.8f),
                Triple(0.86f, 0.52f, 3.8f),
                Triple(0.18f, 0.72f, 4.2f),
                Triple(0.64f, 0.78f, 3.0f),
                Triple(0.48f, 0.12f, 3.2f),
                Triple(0.55f, 0.64f, 2.5f)
            )

            particlePositions.forEachIndexed { idx, (rx, ry, radiusDp) ->
                val dx = cos(phase + idx * 0.9f) * 14.dp.toPx()
                val dy = sin(phase + idx * 1.1f) * 14.dp.toPx()
                val center = Offset(w * rx + dx, h * ry + dy)
                val pColor = if (idx % 2 == 0) GoldLight else CyanGlow
                drawCircle(
                    color = pColor.copy(alpha = if (darkMode) 0.28f else 0.18f),
                    radius = radiusDp.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = pColor.copy(alpha = if (darkMode) 0.08f else 0.05f),
                    radius = (radiusDp * 2.6f).dp.toPx(),
                    center = center
                )
            }
        }
        content()
    }
}

/**
 * Reusable Liquid Glass Card with specular border, soft reflection, and inner glow.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    accentGlow: Color = Color(0xFF38BDF8),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .shadow(
            elevation = 14.dp,
            shape = shape,
            ambientColor = accentGlow.copy(alpha = 0.25f),
            spotColor = accentGlow.copy(alpha = 0.35f)
        )
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E2D63).copy(alpha = 0.78f),
                    Color(0xFF111B42).copy(alpha = 0.86f)
                )
            )
        )
        .drawBehind {
            // Top liquid glass specular reflection sheen
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.45f
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
            // Subtle glass rim border
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.42f),
                        accentGlow.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                style = Stroke(width = 1.3.dp.toPx())
            )
        }

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Box(modifier = clickableModifier, content = content)
}

/**
 * Floating Liquid Glass Bottom Navigation Bar inspired by Apple's Liquid Glass design language.
 */
@Composable
fun LiquidGlassBottomNav(
    selectedTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_nav_shimmer")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_shift"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        val pillShape = RoundedCornerShape(34.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = pillShape,
                    ambientColor = Color(0xFF06B6D4).copy(alpha = 0.4f),
                    spotColor = Color(0xFFF59E0B).copy(alpha = 0.35f)
                )
                .clip(pillShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A2758).copy(alpha = 0.86f),
                            Color(0xFF0F183B).copy(alpha = 0.90f),
                            Color(0xFF1D295E).copy(alpha = 0.86f)
                        )
                    )
                )
                .drawBehind {
                    // Top liquid glass specular refraction arc
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.55f
                        ),
                        cornerRadius = CornerRadius(34.dp.toPx(), 34.dp.toPx())
                    )
                    // Dynamic light reflection sweep along the glass border
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color(0xFF38BDF8).copy(alpha = 0.45f),
                                Color(0xFFFBBF24).copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.30f)
                            ),
                            start = Offset(size.width * shimmerShift * 0.5f, 0f),
                            end = Offset(size.width * (0.5f + shimmerShift * 0.5f), size.height)
                        ),
                        cornerRadius = CornerRadius(34.dp.toPx(), 34.dp.toPx()),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_scale"
                )
                val iconOffsetY by animateDpAsState(
                    targetValue = if (isSelected) (-1).dp else 0.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tab_offset"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF94A3B8),
                    label = "tab_text_color"
                )

                val iconVector = when (tab) {
                    MainNavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                    MainNavTab.GAMES -> if (isSelected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports
                    MainNavTab.STATISTICS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
                    MainNavTab.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                }

                val activeShape = RoundedCornerShape(26.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .scale(scale)
                        .clip(activeShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.22f),
                                                Color(0xFF38BDF8).copy(alpha = 0.20f),
                                                Color(0xFFF59E0B).copy(alpha = 0.18f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.65f),
                                                GoldPrimary.copy(alpha = 0.45f)
                                            )
                                        ),
                                        shape = activeShape
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectTab(tab) }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = tab.label,
                            tint = if (isSelected) GoldLight else Color(0xFFCBD5E1),
                            modifier = Modifier
                                .size(22.dp)
                                .offset(y = iconOffsetY)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = textColor
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(GoldLight, CyanGlow)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
