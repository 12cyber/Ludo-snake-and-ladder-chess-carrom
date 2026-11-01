package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ALL_LUDO_PAWN_SKINS
import com.example.data.ContinueGameSession
import com.example.data.GameType
import com.example.data.PLAYER_AVATARS
import com.example.data.PlayerProfileState
import com.example.ui.components.CustomPawnPreviewBadge
import com.example.ui.components.GameCard3DArtwork
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun HomeScreen(
    profile: PlayerProfileState,
    totalGames: Int,
    totalWins: Int,
    winPercentage: Int,
    continueSession: ContinueGameSession?,
    onLaunchGame: (GameType) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    onOpenPawnVault: () -> Unit
) {
    val avatar = PLAYER_AVATARS.getOrElse(profile.avatarId) { PLAYER_AVATARS.first() }
    val selectedPawn = ALL_LUDO_PAWN_SKINS.find { it.id == profile.selectedPawnSkinId }
        ?: ALL_LUDO_PAWN_SKINS.first()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP PROFILE SECTION
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                accentGlow = GoldPrimary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onOpenProfileEdit)
                                .testTag("home_profile_header")
                        ) {
                            // Avatar Orb
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(avatar.primaryColor, avatar.secondaryColor)
                                        )
                                    )
                                    .border(2.dp, GoldLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = avatar.symbol,
                                    fontSize = 26.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.username,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = GoldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.countryFlag,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldPrimary.copy(alpha = 0.22f))
                                            .border(1.dp, GoldLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LVL ${profile.level}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldLight
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // XP Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PLAYVERSE RANK • ${avatar.title.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow
                        )
                        Text(
                            text = "${profile.xpInCurrentLevel} / 200 XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LinearProgressIndicator(
                        progress = { profile.xpInCurrentLevel / 200f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = GoldLight,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Unified Quick Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileMetricPill(
                            label = "Matches",
                            value = totalGames.toString(),
                            color = CyanGlow,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetricPill(
                            label = "Victories",
                            value = totalWins.toString(),
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMetricPill(
                            label = "Win Rate",
                            value = "$winPercentage%",
                            color = GoldLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. CONTINUE LAST GAME SECTION
        item {
            val activeContinue = continueSession ?: ContinueGameSession(
                gameType = GameType.SNAKES,
                modeSummary = "Quick Match • Vs Computer",
                statusSummary = "Ready to roll • Tap to jump straight in",
                updatedAtMs = System.currentTimeMillis()
            )
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_last_game_card"),
                cornerRadius = 22.dp,
                accentGlow = activeContinue.gameType.accentColor,
                onClick = { onLaunchGame(activeContinue.gameType) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(activeContinue.gameType.accentColor.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    activeContinue.gameType.accentColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            GameCard3DArtwork(
                                gameType = activeContinue.gameType,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (continueSession != null) "CONTINUE LAST GAME" else "QUICK LAUNCH",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeContinue.gameType.accentColor
                            )
                            Text(
                                text = activeContinue.gameType.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeContinue.modeSummary} • ${activeContinue.statusSummary}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = Color(0xFFCBD5E1),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onLaunchGame(activeContinue.gameType) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeContinue.gameType.accentColor,
                            contentColor = Color(0xFF070C1F)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("resume_last_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (continueSession != null) "Resume" else "Start",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 3. PAWN VAULT BANNER
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_pawn_vault_banner"),
                cornerRadius = 20.dp,
                accentGlow = Color(0xFFF59E0B),
                onClick = onOpenPawnVault
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        CustomPawnPreviewBadge(
                            skinId = selectedPawn.id,
                            baseColor = selectedPawn.accentColor,
                            size = 44.dp,
                            isHighlighted = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "EQUIPPED LUDO PAWN • ${selectedPawn.category.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight
                            )
                            Text(
                                text = "${selectedPawn.name} (Tap to browse 13 3D Pawns)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Customize Pawns",
                        tint = GoldLight
                    )
                }
            }
        }

        // 4. SECTION HEADER: MAIN GAME SELECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYVERSE ARENA",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                    Text(
                        text = "Select Your Board Game",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "4 Full Games • Offline AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                }
            }
        }

        // 5. FOUR LARGE VISUALLY IMPRESSIVE GAME CARDS
        items(GameType.entries.size) { index ->
            val game = GameType.entries[index]
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                ShowcaseGameCard(
                    gameType = game,
                    onPlayClick = { onLaunchGame(game) }
                )
            }
        }
    }
}

@Composable
private fun ProfileMetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun ShowcaseGameCard(
    gameType: GameType,
    onPlayClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${gameType.id}"),
        cornerRadius = 26.dp,
        accentGlow = gameType.accentColor,
        onClick = onPlayClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Procedural Artwork Container
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                gameType.accentColor.copy(alpha = 0.32f),
                                Color(0xFF090F26)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(gameType.accentColor, Color.White.copy(alpha = 0.25f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                GameCard3DArtwork(
                    gameType = gameType,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(gameType.accentColor.copy(alpha = 0.18f))
                        .border(1.dp, gameType.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = gameType.badge.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = gameType.accentColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = gameType.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = gameType.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gameType.accentColor,
                        contentColor = Color(0xFF070C1F)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("play_button_${gameType.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play ${gameType.title}",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PLAY NOW",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
