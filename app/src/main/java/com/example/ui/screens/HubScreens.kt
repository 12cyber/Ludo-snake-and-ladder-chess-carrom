package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ALL_LUDO_PAWN_SKINS
import com.example.data.AchievementItem
import com.example.data.AiDifficulty
import com.example.data.AppSettingsState
import com.example.data.COUNTRY_FLAGS
import com.example.data.CarromBoardTheme
import com.example.data.CarromCoinStyle
import com.example.data.ChessBoardTheme
import com.example.data.ChessPieceSet
import com.example.data.GameSpecificStats
import com.example.data.GameType
import com.example.data.MatchHistoryItem
import com.example.data.PLAYER_AVATARS
import com.example.data.PlayerProfileState
import com.example.data.SnakesBoardTheme
import com.example.ui.components.CustomPawnPreviewBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RubyDanger

/**
 * Games Catalog Tab with instant theme & mode customizer for all 4 games.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GamesCatalogScreen(
    profile: PlayerProfileState,
    onLaunchGame: (GameType) -> Unit,
    onOpenPawnVault: () -> Unit,
    onSelectSnakesTheme: (SnakesBoardTheme) -> Unit,
    onSelectCarromThemes: (CarromBoardTheme, CarromCoinStyle) -> Unit,
    onSelectChessThemes: (ChessBoardTheme, ChessPieceSet) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("games_catalog_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "GAME ARENA & STUDIO",
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
            )
            Text(
                text = "All 4 Board Games & Themes",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        items(GameType.entries) { game ->
            ShowcaseGameCard(
                gameType = game,
                onPlayClick = { onLaunchGame(game) }
            )
        }

        // Quick Theme Studio Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentGlow = GoldPrimary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BOARD & PIECE CUSTOMIZATION STUDIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Snake & Ladders World (${profile.snakesTheme.displayName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SnakesBoardTheme.entries.forEach { theme ->
                            val selected = profile.snakesTheme == theme
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectSnakesTheme(theme) },
                                label = { Text(theme.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldSuccess.copy(alpha = 0.3f),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Carrom Board (${profile.carromBoardTheme.displayName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CarromBoardTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = profile.carromBoardTheme == theme,
                                onClick = { onSelectCarromThemes(theme, profile.carromCoinStyle) },
                                label = { Text(theme.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary.copy(alpha = 0.35f),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chess Board (${profile.chessBoardTheme.displayName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChessBoardTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = profile.chessBoardTheme == theme,
                                onClick = { onSelectChessThemes(theme, profile.chessPieceSet) },
                                label = { Text(theme.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.35f),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenPawnVault,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF090D16)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Ludo 3D Pawn Collection Vault (13 Skins)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Unified Statistics, Game Breakdown, Achievements & Recent Matches Screen.
 */
@Composable
fun StatisticsScreen(
    statsMap: Map<GameType, GameSpecificStats>,
    achievements: List<AchievementItem>,
    recentMatches: List<MatchHistoryItem>,
    favoriteGame: GameType
) {
    val totalGames = statsMap.values.sumOf { it.gamesPlayed }
    val totalWins = statsMap.values.sumOf { it.wins }
    val totalLosses = statsMap.values.sumOf { it.losses }
    val winPct = if (totalGames > 0) ((totalWins * 100f) / totalGames).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "CAREER ANALYTICS",
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
            )
            Text(
                text = "Statistics & Achievements",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        // Overall Career Summary Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                accentGlow = CyanGlow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn("Total Played", totalGames.toString(), CyanGlow)
                        StatColumn("Total Wins", totalWins.toString(), EmeraldSuccess)
                        StatColumn("Total Losses", totalLosses.toString(), RubyDanger)
                        StatColumn("Win Rate", "$winPct%", GoldLight)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Favourite Game: ${favoriteGame.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Per-Game Statistics Cards
        item {
            Text(
                text = "Game-Specific Performance",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        items(GameType.entries) { game ->
            val st = statsMap[game] ?: GameSpecificStats(game)
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                accentGlow = game.accentColor
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${st.winRate}% Win Rate",
                            style = MaterialTheme.typography.labelLarge,
                            color = game.accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Played: ${st.gamesPlayed}", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                        Text("Wins: ${st.wins}", color = EmeraldSuccess, fontSize = 13.sp)
                        Text("Losses: ${st.losses}", color = RubyDanger, fontSize = 13.sp)
                        Text("Streak: ${st.bestStreak}", color = GoldLight, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${st.specialMetricName}: ${st.specialMetricValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                }
            }
        }

        // Achievements Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldLight)
                Text(
                    text = "Playverse Achievements (${achievements.count { it.isUnlocked }}/${achievements.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        items(achievements) { ach ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                accentGlow = if (ach.isUnlocked) ach.accentColor else Color(0xFF475569)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) ach.accentColor.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                1.5.dp,
                                if (ach.isUnlocked) ach.accentColor else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = ach.iconSymbol, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (ach.isUnlocked) Color.White else Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (ach.isUnlocked) "UNLOCKED" else "${ach.currentProgress}/${ach.targetProgress}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ach.isUnlocked) EmeraldSuccess else GoldLight
                            )
                        }
                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (ach.currentProgress.toFloat() / ach.targetProgress).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = ach.accentColor,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }

        // Recent Matches History
        item {
            Text(
                text = "Recent Match History",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        if (recentMatches.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    accentGlow = Color(0xFF64748B)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎲", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No matches recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Play Snake & Ladders, Ludo, Carrom, or Chess to track your match history!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(recentMatches) { match ->
                val badgeColor = when (match.outcome) {
                    "VICTORY" -> EmeraldSuccess
                    "DRAW" -> GoldLight
                    else -> RubyDanger
                }
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    accentGlow = badgeColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${match.gameType.title} • ${match.modeLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${match.opponentLabel} • ${match.details}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = match.outcome,
                                style = MaterialTheme.typography.labelLarge,
                                color = badgeColor
                            )
                            Text(
                                text = match.timestampLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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

/**
 * Profile & Customization Tab with Avatar Picker, Username Editor, Country Flag selector,
 * and direct access to the Pawn Collection Vault and Settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: PlayerProfileState,
    totalWins: Int,
    onSaveProfile: (String, Int, String) -> Unit,
    onOpenPawnVault: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var editedName by remember(profile.username) { mutableStateOf(profile.username) }
    var selectedAvatarId by remember(profile.avatarId) { mutableIntStateOf(profile.avatarId) }
    var selectedFlag by remember(profile.countryFlag) { mutableStateOf(profile.countryFlag) }
    var savedFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYER IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                    Text(
                        text = "Profile & Customization",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        // Username & Country Editor
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                accentGlow = GoldPrimary
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customize Commander Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            savedFeedback = false
                        },
                        label = { Text("Player Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldLight,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = GoldLight,
                            unfocusedLabelColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_username_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select Avatar Emblem",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PLAYER_AVATARS.forEach { av ->
                            val isSelected = av.id == selectedAvatarId
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) av.primaryColor.copy(alpha = 0.28f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GoldLight else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedAvatarId = av.id
                                        savedFeedback = false
                                    }
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(av.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(av.symbol, fontSize = 22.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = av.name,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select Country Flag",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        COUNTRY_FLAGS.forEach { flag ->
                            FilterChip(
                                selected = selectedFlag == flag,
                                onClick = {
                                    selectedFlag = flag
                                    savedFeedback = false
                                },
                                label = { Text(flag, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanGlow.copy(alpha = 0.28f),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onSaveProfile(editedName, selectedAvatarId, selectedFlag)
                            savedFeedback = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color(0xFF052E16)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (savedFeedback) "Profile Saved!" else "Save Profile Changes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Ludo Pawn Collection Vault Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentGlow = GoldPrimary,
                onClick = onOpenPawnVault
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LUDO PAWN COLLECTION VAULT",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldLight
                        )
                        Text(
                            text = "Browse 13 Custom 3D Pawns",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Crowns, Knights, Robots, Dragons, Lions, Crystals & Space Helmets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Button(
                        onClick = onOpenPawnVault,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Open Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Ludo Pawn Collection Screen with 10 categories & 13 sculpted pawn previews.
 */
@Composable
fun PawnCollectionScreen(
    selectedPawnId: String,
    totalWins: Int,
    onSelectPawn: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pawn_collection_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "10 CATEGORIES • 13 CUSTOM PAWNS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = "Ludo Pawn Collection Vault",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
        }

        items(ALL_LUDO_PAWN_SKINS) { skin ->
            val isUnlocked = totalWins >= skin.requiredWins || skin.requiredWins == 0
            val isSelected = skin.id == selectedPawnId

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pawn_skin_${skin.id}"),
                cornerRadius = 20.dp,
                accentGlow = if (isSelected) GoldLight else skin.accentColor,
                onClick = {
                    if (isUnlocked) onSelectPawn(skin.id)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        CustomPawnPreviewBadge(
                            skinId = skin.id,
                            baseColor = skin.accentColor,
                            size = 54.dp,
                            isHighlighted = isSelected
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = skin.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(skin.accentColor.copy(alpha = 0.22f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = skin.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = skin.accentColor
                                    )
                                }
                            }
                            Text(
                                text = skin.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSuccess)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("EQUIPPED", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    } else if (isUnlocked) {
                        Button(
                            onClick = { onSelectPawn(skin.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Equip", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = GoldLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${skin.requiredWins} Wins",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Complete Settings Screen with Sound, Music, Vibration, Animation/Graphics Quality,
 * Appearance, Language selector, Default AI Difficulty, Reset Statistics, and Clear Data.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    onUpdateSettings: ((AppSettingsState) -> AppSettingsState) -> Unit,
    onResetStats: () -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val languages = listOf("English", "Español", "Français", "Deutsch", "हिंदी", "日本語")
    val animQualities = listOf("Smooth", "High", "Ultra")
    val gfxQualities = listOf("Standard", "High", "Ultra 3D")
    val diceSpeeds = listOf("Normal", "Fast", "Instant")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("settings_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                    Text(
                        text = "Settings & Preferences",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Audio & Haptics
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, accentGlow = CyanGlow) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Audio & Haptics", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    SettingsToggleRow(
                        title = "Sound Effects",
                        subtitle = "Synthesized board, dice, collision & victory acoustics",
                        checked = settings.soundEffects,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(soundEffects = v) } }
                    )
                    SettingsToggleRow(
                        title = "Background Music",
                        subtitle = "Ambient royal synth harmonic loop",
                        checked = settings.backgroundMusic,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(backgroundMusic = v) } }
                    )
                    SettingsToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback on dice rolls, strikes & captures",
                        checked = settings.vibration,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(vibration = v) } }
                    )
                }
            }
        }

        // Visuals, Performance & Appearance
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, accentGlow = GoldPrimary) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Graphics, Animation & Appearance", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    SettingsToggleRow(
                        title = "Royal Dark Appearance",
                        subtitle = "Deep royal indigo & liquid glass mode",
                        checked = settings.darkMode,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(darkMode = v) } }
                    )

                    Text("Animation Quality", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        animQualities.forEach { q ->
                            FilterChip(
                                selected = settings.animationQuality == q,
                                onClick = { onUpdateSettings { it.copy(animationQuality = q) } },
                                label = { Text(q) }
                            )
                        }
                    }

                    Text("Graphics Quality", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        gfxQualities.forEach { q ->
                            FilterChip(
                                selected = settings.graphicsQuality == q,
                                onClick = { onUpdateSettings { it.copy(graphicsQuality = q) } },
                                label = { Text(q) }
                            )
                        }
                    }
                }
            }
        }

        // Gameplay Rules & Language
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, accentGlow = EmeraldSuccess) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gameplay & Regional Architecture", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    SettingsToggleRow(
                        title = "Remember Last Played Game",
                        subtitle = "Show Continue Playing shortcut on Home Screen",
                        checked = settings.saveLastPlayedGame,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(saveLastPlayedGame = v) } }
                    )
                    SettingsToggleRow(
                        title = "Exact Roll for Tile 100 (Snakes & Ladders)",
                        subtitle = "Require exact dice number to land on square 100",
                        checked = settings.snakesExactRoll,
                        onCheckedChange = { v -> onUpdateSettings { it.copy(snakesExactRoll = v) } }
                    )

                    Text("Dice Animation Speed", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        diceSpeeds.forEach { spd ->
                            FilterChip(
                                selected = settings.diceSpeed == spd,
                                onClick = { onUpdateSettings { it.copy(diceSpeed = spd) } },
                                label = { Text(spd) }
                            )
                        }
                    }

                    Text("Default AI Difficulty", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiDifficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = settings.defaultAiDifficulty == diff,
                                onClick = { onUpdateSettings { it.copy(defaultAiDifficulty = diff) } },
                                label = { Text(diff.label) }
                            )
                        }
                    }

                    Text("Language Selection", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { lang ->
                            FilterChip(
                                selected = settings.language == lang,
                                onClick = { onUpdateSettings { it.copy(language = lang) } },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            }
        }

        // Data Management
        item {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp, accentGlow = RubyDanger) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Data & Storage Management", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetStats,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reset_stats_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldLight)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Stats", color = GoldLight)
                        }
                        Button(
                            onClick = onClearAllData,
                            colors = ButtonDefaults.buttonColors(containerColor = RubyDanger),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_data_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Data")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = Color(0xFF94A3B8))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldSuccess
            )
        )
    }
}
