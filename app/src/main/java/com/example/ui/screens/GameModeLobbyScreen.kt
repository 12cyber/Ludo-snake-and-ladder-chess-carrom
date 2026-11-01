package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ALL_LUDO_PAWN_SKINS
import com.example.data.AiDifficulty
import com.example.data.CarromBoardTheme
import com.example.data.CarromCoinStyle
import com.example.data.ChessBoardTheme
import com.example.data.ChessPieceSet
import com.example.data.GameLaunchConfig
import com.example.data.GameType
import com.example.data.PlayModeType
import com.example.data.PlayerProfileState
import com.example.data.SnakesBoardTheme
import com.example.ui.components.CustomPawnPreviewBadge
import com.example.ui.components.GameCard3DArtwork
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

/**
 * Full-Screen Pre-Game Mode Selection Lobby.
 * Shown whenever a player taps any of the 4 games so they can clearly pick:
 * - Mode 1: Local Room (Offline Pass & Play with friends in a room)
 * - Mode 2: Play vs Computer AI (with 5 AI difficulty tiers)
 * - Plus player count (2P/3P/4P) and board/piece themes before entering the game.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameModeLobbyScreen(
    config: GameLaunchConfig,
    profile: PlayerProfileState,
    onUpdateConfig: ((GameLaunchConfig) -> GameLaunchConfig) -> Unit,
    onSelectSnakesTheme: (SnakesBoardTheme) -> Unit,
    onSelectCarromThemes: (CarromBoardTheme, CarromCoinStyle) -> Unit,
    onSelectChessThemes: (ChessBoardTheme, ChessPieceSet) -> Unit,
    onOpenPawnVault: () -> Unit,
    onStartMatch: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val game = config.gameType
    val availableModes = when (game) {
        GameType.SNAKES, GameType.LUDO -> listOf(PlayModeType.LOCAL_ROOM, PlayModeType.VS_COMPUTER)
        GameType.CARROM -> listOf(PlayModeType.LOCAL_ROOM, PlayModeType.VS_COMPUTER, PlayModeType.PRACTICE)
        GameType.CHESS -> listOf(PlayModeType.LOCAL_ROOM, PlayModeType.VS_COMPUTER, PlayModeType.PRACTICE, PlayModeType.PUZZLE)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_mode_lobby_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar & 3D Game Hero Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .testTag("lobby_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SELECT GAME MODE & TABLE SETUP",
                        style = MaterialTheme.typography.labelSmall,
                        color = game.accentColor
                    )
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
        }

        // 2. Hero Preview Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                accentGlow = game.accentColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(game.accentColor.copy(alpha = 0.35f), Color(0xFF070C1F))
                                )
                            )
                            .border(1.5.dp, game.accentColor, RoundedCornerShape(20.dp))
                    ) {
                        GameCard3DArtwork(gameType = game, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(game.accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Selected: ${config.modeDisplayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight
                            )
                        }
                    }
                }
            }
        }

        // 3. Primary Mode Cards: Mode 1 (Local Room Offline) vs Mode 2 (With Computer AI)
        item {
            Text(
                text = "CHOOSE HOW YOU WANT TO PLAY",
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                availableModes.forEach { mode ->
                    val isSelected = config.playMode == mode
                    val icon: ImageVector = when (mode) {
                        PlayModeType.LOCAL_ROOM -> Icons.Default.Groups
                        PlayModeType.VS_COMPUTER -> Icons.Default.Computer
                        PlayModeType.PRACTICE -> Icons.Default.SportsScore
                        PlayModeType.PUZZLE -> Icons.Default.Extension
                    }
                    val cardAccent = when (mode) {
                        PlayModeType.LOCAL_ROOM -> EmeraldSuccess
                        PlayModeType.VS_COMPUTER -> CyanGlow
                        PlayModeType.PRACTICE -> GoldPrimary
                        PlayModeType.PUZZLE -> Color(0xFFA855F7)
                    }

                    ModeOptionCard(
                        title = mode.title,
                        subtitle = mode.subtitle,
                        icon = icon,
                        accentColor = cardAccent,
                        isSelected = isSelected,
                        testTag = "mode_card_${mode.name.lowercase()}",
                        onClick = { onUpdateConfig { it.copy(playMode = mode) } }
                    )
                }
            }
        }

        // 4. Mode-Specific Configuration (Player Count for Local Room OR AI Difficulty for Vs Computer)
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentGlow = GoldPrimary
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (config.playMode == PlayModeType.LOCAL_ROOM) {
                        val countOptions = when (game) {
                            GameType.SNAKES, GameType.LUDO -> listOf(2, 3, 4)
                            GameType.CARROM -> listOf(2, 4)
                            GameType.CHESS -> listOf(2)
                        }
                        Text(
                            text = "Local Room Players (Offline Pass & Play)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            countOptions.forEach { count ->
                                val selected = config.playerCount == count
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (selected) EmeraldSuccess.copy(alpha = 0.28f)
                                            else Color.White.copy(alpha = 0.06f)
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) EmeraldSuccess else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onUpdateConfig { it.copy(playerCount = count) } }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (game == GameType.CARROM && count == 4) "4P Doubles" else "$count Players",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) Color.White else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    } else if (config.playMode == PlayModeType.VS_COMPUTER) {
                        Text(
                            text = "Computer AI Difficulty",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AiDifficulty.entries.forEach { diff ->
                                val selected = config.aiDifficulty == diff
                                FilterChip(
                                    selected = selected,
                                    onClick = { onUpdateConfig { it.copy(aiDifficulty = diff) } },
                                    label = { Text(diff.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanGlow.copy(alpha = 0.30f),
                                        selectedLabelColor = Color.White,
                                        labelColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                        if (game == GameType.SNAKES || game == GameType.LUDO) {
                            Text(
                                text = "Total Players on Board (You + AI Bots)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf(2, 3, 4).forEach { cnt ->
                                    FilterChip(
                                        selected = config.playerCount == cnt,
                                        onClick = { onUpdateConfig { it.copy(playerCount = cnt) } },
                                        label = { Text("$cnt Players (1 Human + ${cnt - 1} AI)") }
                                    )
                                }
                            }
                        }
                    }

                    // Game-specific visual & rule customization inside the lobby
                    when (game) {
                        GameType.SNAKES -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Require Exact Roll for Tile 100", color = Color.White, fontSize = 14.sp)
                                Switch(
                                    checked = config.exactRollSnakes,
                                    onCheckedChange = { v -> onUpdateConfig { it.copy(exactRollSnakes = v) } }
                                )
                            }
                            Text("Board World Theme", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SnakesBoardTheme.entries.forEach { t ->
                                    FilterChip(
                                        selected = profile.snakesTheme == t,
                                        onClick = { onSelectSnakesTheme(t) },
                                        label = { Text(t.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        GameType.LUDO -> {
                            val selectedPawn = ALL_LUDO_PAWN_SKINS.find { it.id == profile.selectedPawnSkinId }
                                ?: ALL_LUDO_PAWN_SKINS.first()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .clickable(onClick = onOpenPawnVault)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CustomPawnPreviewBadge(
                                        skinId = selectedPawn.id,
                                        baseColor = selectedPawn.accentColor,
                                        size = 42.dp,
                                        isHighlighted = true
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Equipped 3D Pawn", style = MaterialTheme.typography.labelSmall, color = GoldLight)
                                        Text("${selectedPawn.name} (${selectedPawn.category})", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Icon(Icons.Default.Star, contentDescription = "Change Pawn", tint = GoldLight)
                            }
                        }

                        GameType.CARROM -> {
                            Text("Wooden Board Finish", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CarromBoardTheme.entries.forEach { b ->
                                    FilterChip(
                                        selected = profile.carromBoardTheme == b,
                                        onClick = { onSelectCarromThemes(b, profile.carromCoinStyle) },
                                        label = { Text(b.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Text("Lathe-Turned Coin Material", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CarromCoinStyle.entries.forEach { c ->
                                    FilterChip(
                                        selected = profile.carromCoinStyle == c,
                                        onClick = { onSelectCarromThemes(profile.carromBoardTheme, c) },
                                        label = { Text(c.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        GameType.CHESS -> {
                            Text("Chessboard Wood & Marble", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ChessBoardTheme.entries.forEach { b ->
                                    FilterChip(
                                        selected = profile.chessBoardTheme == b,
                                        onClick = { onSelectChessThemes(b, profile.chessPieceSet) },
                                        label = { Text(b.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Text("3D Staunton Piece Set", style = MaterialTheme.typography.titleMedium, color = GoldLight)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ChessPieceSet.entries.forEach { ps ->
                                    FilterChip(
                                        selected = profile.chessPieceSet == ps,
                                        onClick = { onSelectChessThemes(profile.chessBoardTheme, ps) },
                                        label = { Text(ps.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Launch Match CTA Button
        item {
            Button(
                onClick = onStartMatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = game.accentColor,
                    contentColor = Color(0xFF050A19)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lobby_start_match_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START ${game.title.uppercase()} (${config.modeDisplayName.uppercase()})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.16f),
        label = "mode_border"
    )
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag(testTag),
        cornerRadius = 22.dp,
        accentGlow = if (isSelected) accentColor else Color(0xFF334155),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.28f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(1.5.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color.White else accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.Transparent)
                    .border(2.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070C1F))
                    )
                }
            }
        }
    }
}
