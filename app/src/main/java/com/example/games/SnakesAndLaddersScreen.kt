package com.example.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.GameLaunchConfig
import com.example.data.PlayModeType
import com.example.data.SnakesBoardTheme
import com.example.ui.components.Animated3DDice
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RubyDanger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class SnakePlayer(
    val id: Int,
    val name: String,
    val color: Color,
    val isAi: Boolean,
    val position: Int = 1
)

private val LADDERS_MAP = mapOf(
    4 to 25,
    13 to 46,
    33 to 49,
    42 to 63,
    50 to 69,
    62 to 81,
    74 to 92
)

private val SNAKES_MAP = mapOf(
    27 to 5,
    40 to 3,
    43 to 18,
    54 to 31,
    66 to 45,
    76 to 58,
    89 to 53,
    99 to 41
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnakesAndLaddersScreen(
    playerUsername: String,
    launchConfig: GameLaunchConfig,
    initialTheme: SnakesBoardTheme,
    diceSpeed: String,
    soundManager: SoundManager,
    onSelectTheme: (SnakesBoardTheme) -> Unit,
    onSaveSession: (String, String) -> Unit,
    onMatchFinished: (Boolean, String, String, String, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    var currentTheme by remember { mutableStateOf(initialTheme) }
    var playerCount by remember(launchConfig) { mutableIntStateOf(launchConfig.playerCount.coerceIn(2, 4)) }
    var vsComputer by remember(launchConfig) { mutableStateOf(launchConfig.playMode == PlayModeType.VS_COMPUTER) }
    var difficulty by remember(launchConfig) { mutableStateOf(launchConfig.aiDifficulty) }
    var exactRollRule by remember(launchConfig) { mutableStateOf(launchConfig.exactRollSnakes) }
    var showConfigSheet by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    val playerColors = listOf(
        Color(0xFFEF4444),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B)
    )

    val players = remember { mutableStateListOf<SnakePlayer>() }

    var currentTurnIndex by remember { mutableIntStateOf(0) }
    var diceValue by remember { mutableIntStateOf(6) }
    var isRolling by remember { mutableStateOf(false) }
    var isAnimatingMove by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf("$playerUsername's Turn • Tap 3D Dice to Roll!") }
    var laddersClimbedByUser by remember { mutableIntStateOf(0) }
    var winnerPlayer by remember { mutableStateOf<SnakePlayer?>(null) }

    // Bouncing tile state
    var bouncingTileNumber by remember { mutableIntStateOf(-1) }
    val bounceScale = remember { Animatable(1f) }

    // 60fps Smooth Interpolated Token Coordinates & Hop Height
    var activeMovingPlayerId by remember { mutableIntStateOf(-1) }
    var activeInterpolatedNormPos by remember { mutableStateOf<Offset?>(null) }
    var activeTokenHopScale by remember { mutableFloatStateOf(1f) }

    fun rebuildPlayers(count: Int, againstAi: Boolean) {
        players.clear()
        players.add(SnakePlayer(0, playerUsername, playerColors[0], isAi = false, position = 1))
        for (i in 1 until count) {
            val isBot = againstAi
            val name = if (isBot) "AI Bot $i (${difficulty.label})" else "Player ${i + 1}"
            players.add(SnakePlayer(i, name, playerColors[i], isAi = isBot, position = 1))
        }
        currentTurnIndex = 0
        winnerPlayer = null
        laddersClimbedByUser = 0
        bouncingTileNumber = -1
        activeMovingPlayerId = -1
        activeInterpolatedNormPos = null
        statusBanner = "${players[0].name}'s Turn • Roll the 3D Dice!"
    }

    LaunchedEffect(launchConfig) {
        rebuildPlayers(playerCount, vsComputer)
    }

    fun tileToNormalizedCenter(tile: Int): Offset {
        val clamped = tile.coerceIn(1, 100) - 1
        val rowFromBottom = clamped / 10
        val colInRow = clamped % 10
        val actualCol = if (rowFromBottom % 2 == 0) colInRow else (9 - colInRow)
        val actualRowFromTop = 9 - rowFromBottom
        return Offset((actualCol + 0.5f) / 10f, (actualRowFromTop + 0.5f) / 10f)
    }

    suspend fun triggerTileBounce(tileNum: Int) {
        bouncingTileNumber = tileNum
        bounceScale.snapTo(1f)
        bounceScale.animateTo(
            targetValue = 1.24f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        bounceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 60fps interpolated hop from one tile to the next
    suspend fun animateSmoothStepHop(playerId: Int, fromTile: Int, toTile: Int, durationMs: Int) {
        val startNorm = tileToNormalizedCenter(fromTile)
        val endNorm = tileToNormalizedCenter(toTile)
        activeMovingPlayerId = playerId
        val startTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val progress = ((now - startTime).toFloat() / durationMs.coerceAtLeast(1)).coerceIn(0f, 1f)
            activeInterpolatedNormPos = Offset(
                startNorm.x + (endNorm.x - startNorm.x) * progress,
                startNorm.y + (endNorm.y - startNorm.y) * progress
            )
            // Parabolic hop arc
            activeTokenHopScale = 1f + sin(progress * PI).toFloat() * 0.35f
            if (progress >= 1f) break
        }
        activeTokenHopScale = 1f
    }

    fun executeRollAndMove() {
        if (isRolling || isAnimatingMove || winnerPlayer != null) return
        val activePlayer = players.getOrNull(currentTurnIndex) ?: return

        scope.launch {
            isRolling = true
            soundManager.playDiceRoll()
            val rollDelay = when (diceSpeed) {
                "Instant" -> 60L
                "Fast" -> 150L
                else -> 300L
            }
            repeat(5) {
                diceValue = Random.nextInt(1, 7)
                delay(rollDelay / 5)
            }
            val finalRoll = Random.nextInt(1, 7)
            diceValue = finalRoll
            isRolling = false
            isAnimatingMove = true

            val startPos = activePlayer.position
            val targetPos = startPos + finalRoll

            if (exactRollRule && targetPos > 100) {
                statusBanner = "${activePlayer.name} rolled $finalRoll • Needs exact ${100 - startPos} for 100!"
                delay(650)
            } else {
                val clampedTarget = targetPos.coerceAtMost(100)
                val hopDuration = when (diceSpeed) {
                    "Instant" -> 50
                    "Fast" -> 110
                    else -> 175
                }
                var prevTile = startPos
                for (stepTile in (startPos + 1)..clampedTarget) {
                    soundManager.playTokenMove()
                    animateSmoothStepHop(activePlayer.id, prevTile, stepTile, hopDuration)
                    players[currentTurnIndex] = activePlayer.copy(position = stepTile)
                    launch { triggerTileBounce(stepTile) }
                    prevTile = stepTile
                }
                activeMovingPlayerId = -1
                activeInterpolatedNormPos = null

                // Check Ladder or Snake at clampedTarget
                val ladderDest = LADDERS_MAP[clampedTarget]
                val snakeDest = SNAKES_MAP[clampedTarget]

                if (ladderDest != null) {
                    statusBanner = "🪜 ${activePlayer.name} climbs 3D Ladder $clampedTarget → $ladderDest!"
                    soundManager.playLadderClimb()
                    if (activePlayer.id == 0) laddersClimbedByUser++
                    val fromNorm = tileToNormalizedCenter(clampedTarget)
                    val toNorm = tileToNormalizedCenter(ladderDest)
                    activeMovingPlayerId = activePlayer.id
                    val startMs = withFrameMillis { it }
                    val climbDuration = 420
                    while (true) {
                        val now = withFrameMillis { it }
                        val t = ((now - startMs).toFloat() / climbDuration).coerceIn(0f, 1f)
                        activeInterpolatedNormPos = Offset(
                            fromNorm.x + (toNorm.x - fromNorm.x) * t,
                            fromNorm.y + (toNorm.y - fromNorm.y) * t
                        )
                        activeTokenHopScale = 1f + sin(t * PI * 4).toFloat() * 0.12f
                        if (t >= 1f) break
                    }
                    activeMovingPlayerId = -1
                    activeInterpolatedNormPos = null
                    activeTokenHopScale = 1f
                    players[currentTurnIndex] = activePlayer.copy(position = ladderDest)
                    triggerTileBounce(ladderDest)
                } else if (snakeDest != null) {
                    statusBanner = "🐍 Snake bites ${activePlayer.name} at $clampedTarget → slides to $snakeDest!"
                    soundManager.playSnakeSlide()
                    val fromNorm = tileToNormalizedCenter(clampedTarget)
                    val toNorm = tileToNormalizedCenter(snakeDest)
                    activeMovingPlayerId = activePlayer.id
                    val startMs = withFrameMillis { it }
                    val slideDuration = 480
                    while (true) {
                        val now = withFrameMillis { it }
                        val t = ((now - startMs).toFloat() / slideDuration).coerceIn(0f, 1f)
                        val curveOffset = sin(t * 6.283f) * 0.048f
                        activeInterpolatedNormPos = Offset(
                            (fromNorm.x + (toNorm.x - fromNorm.x) * t + curveOffset).coerceIn(0.05f, 0.95f),
                            fromNorm.y + (toNorm.y - fromNorm.y) * t
                        )
                        if (t >= 1f) break
                    }
                    activeMovingPlayerId = -1
                    activeInterpolatedNormPos = null
                    players[currentTurnIndex] = activePlayer.copy(position = snakeDest)
                    triggerTileBounce(snakeDest)
                } else {
                    statusBanner = "${activePlayer.name} rolled $finalRoll → Tile $clampedTarget"
                }

                val finalTile = players[currentTurnIndex].position
                if (finalTile == 100) {
                    winnerPlayer = players[currentTurnIndex]
                    isAnimatingMove = false
                    val userWon = players[currentTurnIndex].id == 0
                    onMatchFinished(
                        userWon,
                        if (vsComputer) "Vs Computer (${difficulty.label})" else "Local ${playerCount}P Room",
                        if (vsComputer) "AI Bot" else "Local Players",
                        "Reached Square 100 in ${currentTheme.displayName}",
                        laddersClimbedByUser
                    )
                    return@launch
                }
            }

            if (finalRoll == 6) {
                statusBanner = "🎲 ${activePlayer.name} rolled a 6! Bonus Roll!"
            } else {
                currentTurnIndex = (currentTurnIndex + 1) % players.size
                val nextP = players[currentTurnIndex]
                statusBanner = "${nextP.name}'s Turn (Tile ${nextP.position})"
            }
            onSaveSession(
                if (vsComputer) "Vs AI • ${currentTheme.displayName}" else "Local ${playerCount}P Room",
                "Tile ${players.firstOrNull()?.position ?: 1}/100 • $statusBanner"
            )
            isAnimatingMove = false
        }
    }

    LaunchedEffect(currentTurnIndex, isRolling, isAnimatingMove, winnerPlayer, players.size) {
        val currentP = players.getOrNull(currentTurnIndex)
        if (currentP != null && currentP.isAi && !isRolling && !isAnimatingMove && winnerPlayer == null) {
            delay(620)
            executeRollAndMove()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snake_idle")
    val slitherPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slither_phase"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("snakes_game_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .testTag("snakes_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${currentTheme.displayName.uppercase()} • ${if (vsComputer) "VS AI" else "LOCAL ${playerCount}P"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                    Text(
                        text = "Snake & Ladders 3D",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showThemePicker = !showThemePicker },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("snakes_theme_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Board Themes", tint = GoldLight)
                }
                IconButton(
                    onClick = { showConfigSheet = !showConfigSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("snakes_config_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Match Setup", tint = CyanGlow)
                }
                IconButton(
                    onClick = { rebuildPlayers(playerCount, vsComputer) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("snakes_restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart Game", tint = Color.White)
                }
            }
        }

        if (showThemePicker) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = GoldPrimary) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Select Board World (8 Themes)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SnakesBoardTheme.entries.forEach { t ->
                            FilterChip(
                                selected = currentTheme == t,
                                onClick = {
                                    currentTheme = t
                                    onSelectTheme(t)
                                },
                                label = { Text(t.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        if (showConfigSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = CyanGlow) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Match Mode & Rules", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !vsComputer,
                            onClick = {
                                vsComputer = false
                                rebuildPlayers(playerCount, false)
                            },
                            label = { Text("Mode 1: Local Room") }
                        )
                        FilterChip(
                            selected = vsComputer,
                            onClick = {
                                vsComputer = true
                                rebuildPlayers(playerCount, true)
                            },
                            label = { Text("Mode 2: Vs Computer") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Players:", color = Color.White, fontSize = 13.sp)
                        listOf(2, 3, 4).forEach { cnt ->
                            FilterChip(
                                selected = playerCount == cnt,
                                onClick = {
                                    playerCount = cnt
                                    rebuildPlayers(cnt, vsComputer)
                                },
                                label = { Text("${cnt}P") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exact Roll Required for 100", color = Color.White, fontSize = 13.sp)
                        Switch(checked = exactRollRule, onCheckedChange = { exactRollRule = it })
                    }
                }
            }
        }

        // Player Scoreboard Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            players.forEachIndexed { idx, p ->
                val isTurn = idx == currentTurnIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isTurn) p.color.copy(alpha = 0.28f)
                            else Color.White.copy(alpha = 0.07f)
                        )
                        .border(
                            width = if (isTurn) 2.dp else 1.dp,
                            color = if (isTurn) p.color else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(p.color)
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = p.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "Tile ${p.position}",
                                fontSize = 11.sp,
                                color = GoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // THE 10x10 3D SERPENTINE BOARD WITH REALISTIC WOOD/STONE TILES, SCALED SNAKES & 3D LADDERS
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(currentTheme.boardBgTop, currentTheme.boardBgBottom)
                    )
                )
                .border(3.dp, GoldPrimary.copy(alpha = 0.78f), RoundedCornerShape(24.dp))
                .padding(7.dp)
                .testTag("snakes_board_canvas")
        ) {
            val numberPaint = remember {
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.LEFT
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val boardW = size.width
                val boardH = size.height
                val cellW = boardW / 10f
                val cellH = boardH / 10f

                // 1. Draw 100 Rounded 3D Bevelled Tiles
                for (tileNum in 1..100) {
                    val idx = tileNum - 1
                    val rowFromBottom = idx / 10
                    val colInRow = idx % 10
                    val col = if (rowFromBottom % 2 == 0) colInRow else (9 - colInRow)
                    val rowFromTop = 9 - rowFromBottom

                    val isBouncing = tileNum == bouncingTileNumber
                    val scale = if (isBouncing) bounceScale.value else 1f

                    val baseTileColor = when {
                        tileNum == 100 -> Color(0xFFF59E0B)
                        LADDERS_MAP.containsKey(tileNum) -> Color(0xFF10B981).copy(alpha = 0.88f)
                        SNAKES_MAP.containsKey(tileNum) -> Color(0xFFEF4444).copy(alpha = 0.88f)
                        (rowFromTop + col) % 2 == 0 -> currentTheme.tileEven
                        else -> currentTheme.tileOdd
                    }

                    val pad = 2.dp.toPx()
                    val tileW = (cellW - pad * 2) * scale
                    val tileH = (cellH - pad * 2) * scale
                    val centerX = (col + 0.5f) * cellW
                    val centerY = (rowFromTop + 0.5f) * cellH
                    val topLeft = Offset(centerX - tileW / 2f, centerY - tileH / 2f)

                    // Deep 3D Tile Extrusion Shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.45f),
                        topLeft = topLeft + Offset(0f, 3.dp.toPx()),
                        size = Size(tileW, tileH),
                        cornerRadius = CornerRadius(7.dp.toPx())
                    )

                    // Bevelled Tile Surface Gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                baseTileColor,
                                baseTileColor.copy(
                                    red = (baseTileColor.red * 0.88f).coerceIn(0f, 1f),
                                    green = (baseTileColor.green * 0.88f).coerceIn(0f, 1f),
                                    blue = (baseTileColor.blue * 0.88f).coerceIn(0f, 1f)
                                )
                            ),
                            startY = topLeft.y,
                            endY = topLeft.y + tileH
                        ),
                        topLeft = topLeft,
                        size = Size(tileW, tileH),
                        cornerRadius = CornerRadius(7.dp.toPx())
                    )

                    // Top Specular Lip Highlight
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.32f),
                        topLeft = topLeft,
                        size = Size(tileW, tileH * 0.25f),
                        cornerRadius = CornerRadius(7.dp.toPx())
                    )

                    if (isBouncing) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = topLeft,
                            size = Size(tileW, tileH),
                            cornerRadius = CornerRadius(7.dp.toPx()),
                            style = Stroke(width = 2.8.dp.toPx())
                        )
                    }

                    val isDarkTile = currentTheme == SnakesBoardTheme.SPACE ||
                        currentTheme == SnakesBoardTheme.NEON_CYBER ||
                        LADDERS_MAP.containsKey(tileNum) ||
                        SNAKES_MAP.containsKey(tileNum)
                    numberPaint.color = if (isDarkTile) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1E293B")
                    numberPaint.textSize = (cellW * 0.28f)
                    drawContext.canvas.nativeCanvas.drawText(
                        tileNum.toString(),
                        topLeft.x + 3.dp.toPx(),
                        topLeft.y + cellH * 0.30f,
                        numberPaint
                    )
                }

                // 2. Draw Realistic 3D Cylindrical Wooden/Brass Ladders
                LADDERS_MAP.forEach { (startTile, endTile) ->
                    val p1 = tileToNormalizedCenter(startTile).let { Offset(it.x * boardW, it.y * boardH) }
                    val p2 = tileToNormalizedCenter(endTile).let { Offset(it.x * boardW, it.y * boardH) }
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                    val nx = -dy / len
                    val ny = dx / len
                    val railHalf = cellW * 0.17f

                    val l1Start = Offset(p1.x + nx * railHalf, p1.y + ny * railHalf)
                    val l1End = Offset(p2.x + nx * railHalf, p2.y + ny * railHalf)
                    val l2Start = Offset(p1.x - nx * railHalf, p1.y - ny * railHalf)
                    val l2End = Offset(p2.x - nx * railHalf, p2.y - ny * railHalf)

                    // Cast shadow on board
                    drawLine(Color.Black.copy(alpha = 0.45f), l1Start + Offset(3f, 5f), l1End + Offset(3f, 5f), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color.Black.copy(alpha = 0.45f), l2Start + Offset(3f, 5f), l2End + Offset(3f, 5f), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

                    // Cylindrical Rungs with 3D highlight
                    val rungCount = (len / (cellW * 0.36f)).toInt().coerceAtLeast(3)
                    for (r in 1 until rungCount) {
                        val t = r / rungCount.toFloat()
                        val rStart = Offset(l1Start.x + (l1End.x - l1Start.x) * t, l1Start.y + (l1End.y - l1Start.y) * t)
                        val rEnd = Offset(l2Start.x + (l2End.x - l2Start.x) * t, l2Start.y + (l2End.y - l2Start.y) * t)
                        drawLine(
                            color = Color.Black.copy(alpha = 0.35f),
                            start = rStart + Offset(1.5f, 3f),
                            end = rEnd + Offset(1.5f, 3f),
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFFFEF08A),
                            start = rStart,
                            end = rEnd,
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 3D Side Rails + Specular Inner Strip
                    drawLine(currentTheme.ladderColor, l1Start, l1End, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(currentTheme.ladderColor, l2Start, l2End, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.45f), l1Start, l1End, strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Color.White.copy(alpha = 0.45f), l2Start, l2End, strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                }

                // 3. Draw Realistic Textured Snakes with Scales & Forked Tongues
                SNAKES_MAP.entries.forEachIndexed { sIdx, (headTile, tailTile) ->
                    val headPos = tileToNormalizedCenter(headTile).let { Offset(it.x * boardW, it.y * boardH) }
                    val tailPos = tileToNormalizedCenter(tailTile).let { Offset(it.x * boardW, it.y * boardH) }
                    val bodyColor = if (sIdx % 2 == 0) currentTheme.snakePrimary else currentTheme.snakeSecondary

                    val dx = tailPos.x - headPos.x
                    val dy = tailPos.y - headPos.y
                    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                    val nx = -dy / len
                    val ny = dx / len

                    val spinePoints = mutableListOf<Offset>()
                    val segments = 26
                    for (i in 0..segments) {
                        val t = i / segments.toFloat()
                        val wave = sin(t * 9.42f + slitherPhase + sIdx) * (cellW * 0.25f) * sin(t * 3.14159f)
                        val px = headPos.x + dx * t + nx * wave
                        val py = headPos.y + dy * t + ny * wave
                        spinePoints.add(Offset(px, py))
                    }

                    val path = Path().apply {
                        moveTo(spinePoints.first().x, spinePoints.first().y)
                        for (i in 1 until spinePoints.size) {
                            lineTo(spinePoints[i].x, spinePoints[i].y)
                        }
                    }

                    // Snake ground shadow
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.46f),
                        style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Snake outer body
                    drawPath(
                        path = path,
                        color = bodyColor,
                        style = Stroke(width = 8.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Snake dorsal diamond scale pattern along spine
                    for (i in 2 until spinePoints.size - 2 step 2) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.55f),
                            radius = 2.4.dp.toPx(),
                            center = spinePoints[i]
                        )
                    }

                    // Animated Snake Head, Flicking Forked Tongue & Eyes
                    val headBob = Offset(
                        cos(slitherPhase + sIdx) * 1.8.dp.toPx(),
                        sin(slitherPhase + sIdx) * 1.8.dp.toPx()
                    )
                    val hCenter = headPos + headBob
                    // Flicking red forked tongue
                    val tongueLen = cellW * (0.22f + 0.08f * sin(slitherPhase * 2f))
                    drawLine(
                        color = RubyDanger,
                        start = hCenter,
                        end = hCenter + Offset(0f, -tongueLen),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawCircle(color = bodyColor, radius = cellW * 0.24f, center = hCenter)
                    drawCircle(color = Color.White, radius = cellW * 0.082f, center = hCenter + Offset(-cellW * 0.08f, -cellW * 0.06f))
                    drawCircle(color = Color.White, radius = cellW * 0.082f, center = hCenter + Offset(cellW * 0.08f, -cellW * 0.06f))
                    drawCircle(color = Color.Black, radius = cellW * 0.042f, center = hCenter + Offset(-cellW * 0.08f, -cellW * 0.06f))
                    drawCircle(color = Color.Black, radius = cellW * 0.042f, center = hCenter + Offset(cellW * 0.08f, -cellW * 0.06f))
                }

                // 4. Draw 3D Player Pedestal Tokens with 60fps Hop Interpolation
                players.forEachIndexed { idx, p ->
                    val isCurrentlyHopping = p.id == activeMovingPlayerId && activeInterpolatedNormPos != null
                    val norm = if (isCurrentlyHopping) {
                        activeInterpolatedNormPos!!
                    } else {
                        tileToNormalizedCenter(p.position)
                    }
                    val hopScale = if (isCurrentlyHopping) activeTokenHopScale else 1f
                    val tokenOffset = Offset(
                        (idx % 2 - 0.5f) * cellW * 0.22f,
                        (idx / 2 - 0.5f) * cellH * 0.22f
                    )
                    val center = Offset(norm.x * boardW, norm.y * boardH) + tokenOffset
                    val radius = cellW * 0.25f * hopScale

                    // Ground shadow expands as token hops higher
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.52f),
                        radius = radius * 1.12f,
                        center = center + Offset(0f, 3.5.dp.toPx() * hopScale)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, p.color, p.color.copy(alpha = 0.8f)),
                            center = center - Offset(radius * 0.3f, radius * 0.3f),
                            radius = radius * 1.3f
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.2.dp.toPx())
                    )
                }
            }
        }

        // Bottom Dice & Action Control Bar
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            accentGlow = players.getOrNull(currentTurnIndex)?.color ?: EmeraldSuccess
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE BOARD STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = statusBanner,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val activeP = players.getOrNull(currentTurnIndex)
                    val canHumanRoll = activeP != null && !activeP.isAi && !isRolling && !isAnimatingMove && winnerPlayer == null
                    Button(
                        onClick = { executeRollAndMove() },
                        enabled = canHumanRoll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("snakes_roll_dice_button")
                    ) {
                        Text(
                            text = if (isRolling) "Rolling..." else if (activeP?.isAi == true) "AI Thinking..." else "Roll 3D Dice",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Animated3DDice(
                    value = diceValue,
                    isRolling = isRolling,
                    accentColor = players.getOrNull(currentTurnIndex)?.color ?: GoldPrimary,
                    enabled = players.getOrNull(currentTurnIndex)?.isAi == false && !isAnimatingMove,
                    size = 68.dp,
                    onRollClick = { executeRollAndMove() }
                )
            }
        }
    }

    winnerPlayer?.let { champ ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF111C44),
            title = {
                Text(
                    text = "🏆 ${champ.name} Wins!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldLight
                )
            },
            text = {
                Text(
                    text = "${champ.name} conquered ${currentTheme.displayName} and reached Square 100! Ladders climbed by you: $laddersClimbedByUser.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { rebuildPlayers(playerCount, vsComputer) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.Black)
                ) {
                    Text("Play Again", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = onBack) {
                    Text("Home Hub")
                }
            }
        )
    }
}
