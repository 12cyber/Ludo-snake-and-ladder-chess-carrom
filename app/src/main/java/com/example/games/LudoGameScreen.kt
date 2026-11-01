package com.example.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.ALL_LUDO_PAWN_SKINS
import com.example.data.AiDifficulty
import com.example.data.GameLaunchConfig
import com.example.data.PlayModeType
import com.example.ui.components.Animated3DDice
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.drawPawnSilhouetteOnScope
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LudoBlue
import com.example.ui.theme.LudoGreen
import com.example.ui.theme.LudoRed
import com.example.ui.theme.LudoYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class LudoPawn(
    val playerIndex: Int,
    val pawnIndex: Int,
    val step: Int = -1
)

data class LudoPlayerConfig(
    val index: Int,
    val name: String,
    val color: Color,
    val isAi: Boolean,
    val startTrackOffset: Int
)

private val OUTER_TRACK_COORDS: List<Pair<Int, Int>> = listOf(
    1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6,
    6 to 5, 6 to 4, 6 to 3, 6 to 2, 6 to 1, 6 to 0,
    7 to 0, 8 to 0,
    8 to 1, 8 to 2, 8 to 3, 8 to 4, 8 to 5,
    9 to 6, 10 to 6, 11 to 6, 12 to 6, 13 to 6, 14 to 6,
    14 to 7, 14 to 8,
    13 to 8, 12 to 8, 11 to 8, 10 to 8, 9 to 8,
    8 to 9, 8 to 10, 8 to 11, 8 to 12, 8 to 13, 8 to 14,
    7 to 14, 6 to 14,
    6 to 13, 6 to 12, 6 to 11, 6 to 10, 6 to 9,
    5 to 8, 4 to 8, 3 to 8, 2 to 8, 1 to 8, 0 to 8,
    0 to 7, 0 to 6
)

private val SAFE_GLOBAL_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LudoGameScreen(
    playerUsername: String,
    launchConfig: GameLaunchConfig,
    selectedPawnSkinId: String,
    diceSpeed: String,
    soundManager: SoundManager,
    onOpenPawnVault: () -> Unit,
    onSaveSession: (String, String) -> Unit,
    onMatchFinished: (Boolean, String, String, String, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    var vsComputer by remember(launchConfig) { mutableStateOf(launchConfig.playMode == PlayModeType.VS_COMPUTER) }
    var playerCount by remember(launchConfig) { mutableIntStateOf(launchConfig.playerCount.coerceIn(2, 4)) }
    var difficulty by remember(launchConfig) { mutableStateOf(launchConfig.aiDifficulty) }
    var showModeSheet by remember { mutableStateOf(false) }

    val activeSkin = ALL_LUDO_PAWN_SKINS.find { it.id == selectedPawnSkinId } ?: ALL_LUDO_PAWN_SKINS.first()

    val playerConfigs = remember(vsComputer, playerCount, playerUsername, difficulty) {
        if (!vsComputer) {
            when (playerCount) {
                2 -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(2, "Player 2", LudoYellow, isAi = false, startTrackOffset = 26)
                )
                3 -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(1, "Player 2", LudoGreen, isAi = false, startTrackOffset = 13),
                    LudoPlayerConfig(2, "Player 3", LudoYellow, isAi = false, startTrackOffset = 26)
                )
                else -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(1, "Player 2", LudoGreen, isAi = false, startTrackOffset = 13),
                    LudoPlayerConfig(2, "Player 3", LudoYellow, isAi = false, startTrackOffset = 26),
                    LudoPlayerConfig(3, "Player 4", LudoBlue, isAi = false, startTrackOffset = 39)
                )
            }
        } else {
            when (playerCount) {
                2 -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(2, "Bot Amber (${difficulty.label})", LudoYellow, isAi = true, startTrackOffset = 26)
                )
                3 -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(1, "Bot Emerald", LudoGreen, isAi = true, startTrackOffset = 13),
                    LudoPlayerConfig(2, "Bot Amber", LudoYellow, isAi = true, startTrackOffset = 26)
                )
                else -> listOf(
                    LudoPlayerConfig(0, playerUsername, LudoRed, isAi = false, startTrackOffset = 0),
                    LudoPlayerConfig(1, "Bot Emerald", LudoGreen, isAi = true, startTrackOffset = 13),
                    LudoPlayerConfig(2, "Bot Amber", LudoYellow, isAi = true, startTrackOffset = 26),
                    LudoPlayerConfig(3, "Bot Sapphire", LudoBlue, isAi = true, startTrackOffset = 39)
                )
            }
        }
    }

    val pawns = remember {
        mutableStateListOf<LudoPawn>().apply {
            for (pIdx in 0..3) {
                for (k in 0..3) {
                    add(LudoPawn(playerIndex = pIdx, pawnIndex = k, step = -1))
                }
            }
        }
    }

    var activeTurnListIndex by remember { mutableIntStateOf(0) }
    var diceValue by remember { mutableIntStateOf(6) }
    var isRolling by remember { mutableStateOf(false) }
    var awaitingPawnSelection by remember { mutableStateOf(false) }
    var isMovingPawn by remember { mutableStateOf(false) }
    var capturesByUser by remember { mutableIntStateOf(0) }
    var winnerConfig by remember { mutableStateOf<LudoPlayerConfig?>(null) }
    var statusMessage by remember { mutableStateOf("$playerUsername's Turn • Roll the 3D Dice!") }

    // 60fps Continuous Pawn Glide Interpolation State
    var movingPawnKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var interpolatedGridPos by remember { mutableStateOf<Offset?>(null) }
    var movingPawnHopScale by remember { mutableFloatStateOf(1f) }

    fun resetGame() {
        pawns.clear()
        for (pIdx in 0..3) {
            for (k in 0..3) {
                pawns.add(LudoPawn(playerIndex = pIdx, pawnIndex = k, step = -1))
            }
        }
        activeTurnListIndex = 0
        awaitingPawnSelection = false
        isMovingPawn = false
        movingPawnKey = null
        interpolatedGridPos = null
        winnerConfig = null
        capturesByUser = 0
        statusMessage = "${playerConfigs[0].name}'s Turn • Roll the 3D Dice!"
    }

    fun globalTrackIndex(playerIndex: Int, step: Int): Int {
        val offset = playerIndex * 13
        return (offset + step) % 52
    }

    fun getGridCellForStep(pIdx: Int, k: Int, step: Int): Offset {
        if (step == -1) {
            val baseOrigin = when (pIdx) {
                0 -> Offset(1.8f, 1.8f)
                1 -> Offset(10.8f, 1.8f)
                2 -> Offset(10.8f, 10.8f)
                else -> Offset(1.8f, 10.8f)
            }
            val dx = if (k % 2 == 0) 0f else 2.2f
            val dy = if (k < 2) 0f else 2.2f
            return Offset(baseOrigin.x + dx + 0.5f, baseOrigin.y + dy + 0.5f)
        }
        if (step in 0..50) {
            val gIdx = globalTrackIndex(pIdx, step)
            val (c, r) = OUTER_TRACK_COORDS[gIdx]
            return Offset(c + 0.5f, r + 0.5f)
        }
        if (step in 51..55) {
            val homeStep = step - 50
            return when (pIdx) {
                0 -> Offset(homeStep + 0.5f, 7.5f)
                1 -> Offset(7.5f, homeStep + 0.5f)
                2 -> Offset(14 - homeStep + 0.5f, 7.5f)
                else -> Offset(7.5f, 14 - homeStep + 0.5f)
            }
        }
        return when (pIdx) {
            0 -> Offset(6.6f, 7.5f)
            1 -> Offset(7.5f, 6.6f)
            2 -> Offset(8.4f, 7.5f)
            else -> Offset(7.5f, 8.4f)
        }
    }

    fun canPawnMove(pawn: LudoPawn, roll: Int): Boolean {
        if (pawn.step == 56) return false
        if (pawn.step == -1) return roll == 6
        return (pawn.step + roll) <= 56
    }

    fun getLegalPawns(playerIndex: Int, roll: Int): List<LudoPawn> {
        return pawns.filter { it.playerIndex == playerIndex && canPawnMove(it, roll) }
    }

    fun advanceTurn(grantBonusTurn: Boolean) {
        awaitingPawnSelection = false
        if (!grantBonusTurn) {
            activeTurnListIndex = (activeTurnListIndex + 1) % playerConfigs.size
        }
        val nextPlayer = playerConfigs[activeTurnListIndex]
        statusMessage = if (grantBonusTurn) {
            "🎲 Bonus Turn for ${nextPlayer.name}! Roll again!"
        } else {
            "${nextPlayer.name}'s Turn • Roll the Dice!"
        }
    }

    suspend fun animateSmoothPawnGlide(pIdx: Int, k: Int, fromStep: Int, toStep: Int, durationMs: Int) {
        val fromPos = getGridCellForStep(pIdx, k, fromStep)
        val toPos = getGridCellForStep(pIdx, k, toStep)
        movingPawnKey = pIdx to k
        val startMs = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val t = ((now - startMs).toFloat() / durationMs.coerceAtLeast(1)).coerceIn(0f, 1f)
            interpolatedGridPos = Offset(
                fromPos.x + (toPos.x - fromPos.x) * t,
                fromPos.y + (toPos.y - fromPos.y) * t
            )
            movingPawnHopScale = 1f + sin(t * PI).toFloat() * 0.34f
            if (t >= 1f) break
        }
        movingPawnHopScale = 1f
    }

    fun executePawnMove(selectedPawn: LudoPawn, roll: Int) {
        if (isMovingPawn || winnerConfig != null) return
        val activePlayer = playerConfigs[activeTurnListIndex]
        scope.launch {
            isMovingPawn = true
            awaitingPawnSelection = false

            val listIdx = pawns.indexOfFirst {
                it.playerIndex == selectedPawn.playerIndex && it.pawnIndex == selectedPawn.pawnIndex
            }
            if (listIdx == -1) {
                isMovingPawn = false
                return@launch
            }

            if (selectedPawn.step == -1) {
                soundManager.playTokenMove()
                animateSmoothPawnGlide(selectedPawn.playerIndex, selectedPawn.pawnIndex, -1, 0, 210)
                pawns[listIdx] = selectedPawn.copy(step = 0)
            } else {
                val targetStep = selectedPawn.step + roll
                var prevStep = selectedPawn.step
                for (s in (selectedPawn.step + 1)..targetStep) {
                    soundManager.playTokenMove()
                    animateSmoothPawnGlide(selectedPawn.playerIndex, selectedPawn.pawnIndex, prevStep, s, 115)
                    pawns[listIdx] = selectedPawn.copy(step = s)
                    prevStep = s
                }
            }
            movingPawnKey = null
            interpolatedGridPos = null

            val updatedPawn = pawns[listIdx]
            var capturedOpponent = false
            val reachedHome = updatedPawn.step == 56

            if (updatedPawn.step in 0..50) {
                val myGlobal = globalTrackIndex(updatedPawn.playerIndex, updatedPawn.step)
                if (myGlobal !in SAFE_GLOBAL_INDICES) {
                    val opponentsOnSquare = pawns.mapIndexedNotNull { idx, other ->
                        if (other.playerIndex != updatedPawn.playerIndex &&
                            other.step in 0..50 &&
                            globalTrackIndex(other.playerIndex, other.step) == myGlobal
                        ) idx else null
                    }
                    if (opponentsOnSquare.size == 1) {
                        val victimIdx = opponentsOnSquare.first()
                        val victim = pawns[victimIdx]
                        soundManager.playCapture()
                        animateSmoothPawnGlide(victim.playerIndex, victim.pawnIndex, victim.step, -1, 260)
                        movingPawnKey = null
                        interpolatedGridPos = null
                        pawns[victimIdx] = victim.copy(step = -1)
                        capturedOpponent = true
                        if (activePlayer.index == 0) capturesByUser++
                        statusMessage = "⚔️ ${activePlayer.name} captured an opponent pawn!"
                        delay(280)
                    }
                }
            }

            val finishedCount = pawns.count { it.playerIndex == activePlayer.index && it.step == 56 }
            if (finishedCount == 4) {
                winnerConfig = activePlayer
                isMovingPawn = false
                onMatchFinished(
                    activePlayer.index == 0,
                    if (vsComputer) "Vs Computer (${difficulty.label})" else "Local ${playerCount}P Room",
                    if (vsComputer) "AI Bots" else "Local Opponents",
                    "All 4 ${activeSkin.name} pawns reached home",
                    capturesByUser
                )
                return@launch
            }

            val bonusTurn = (roll == 6) || capturedOpponent || reachedHome
            onSaveSession(
                if (vsComputer) "Vs AI • ${activeSkin.name}" else "Local ${playerCount}P Room",
                "Home: ${pawns.count { it.playerIndex == 0 && it.step == 56 }}/4 pawns"
            )
            isMovingPawn = false
            advanceTurn(bonusTurn)
        }
    }

    fun chooseBestAiPawn(aiPlayer: LudoPlayerConfig, legalPawns: List<LudoPawn>, roll: Int): LudoPawn {
        if (difficulty == AiDifficulty.BEGINNER) return legalPawns.random()

        return legalPawns.maxByOrNull { candidate ->
            var score = 0
            val nextStep = if (candidate.step == -1) 0 else candidate.step + roll
            if (nextStep == 56) score += 500
            if (nextStep in 0..50) {
                val targetGlobal = globalTrackIndex(aiPlayer.index, nextStep)
                if (targetGlobal !in SAFE_GLOBAL_INDICES) {
                    val opponentCount = pawns.count {
                        it.playerIndex != aiPlayer.index &&
                            it.step in 0..50 &&
                            globalTrackIndex(it.playerIndex, it.step) == targetGlobal
                    }
                    if (opponentCount == 1) score += 420
                } else {
                    score += 140
                }
            }
            if (candidate.step == -1 && roll == 6) score += 260
            if (candidate.step <= 50 && nextStep in 51..55) score += 220
            score += nextStep * 2
            score
        } ?: legalPawns.first()
    }

    fun rollDiceForCurrentPlayer() {
        if (isRolling || awaitingPawnSelection || isMovingPawn || winnerConfig != null) return
        val currentPlayer = playerConfigs[activeTurnListIndex]

        scope.launch {
            isRolling = true
            soundManager.playDiceRoll()
            val rollWait = when (diceSpeed) {
                "Instant" -> 50L
                "Fast" -> 140L
                else -> 280L
            }
            repeat(5) {
                diceValue = Random.nextInt(1, 7)
                delay(rollWait / 5)
            }
            val allInBase = pawns.filter { it.playerIndex == currentPlayer.index }.all { it.step == -1 }
            val rolled = if (allInBase && Random.nextFloat() < 0.42f) 6 else Random.nextInt(1, 7)
            diceValue = rolled
            isRolling = false

            val legal = getLegalPawns(currentPlayer.index, rolled)
            if (legal.isEmpty()) {
                statusMessage = "${currentPlayer.name} rolled $rolled • No legal pawn moves"
                delay(650)
                advanceTurn(grantBonusTurn = false)
            } else if (currentPlayer.isAi) {
                statusMessage = "${currentPlayer.name} rolled $rolled • Selecting tactical move..."
                delay(420)
                val chosen = chooseBestAiPawn(currentPlayer, legal, rolled)
                executePawnMove(chosen, rolled)
            } else if (legal.size == 1) {
                statusMessage = "${currentPlayer.name} rolled $rolled • Gliding pawn..."
                delay(180)
                executePawnMove(legal.first(), rolled)
            } else {
                awaitingPawnSelection = true
                statusMessage = "${currentPlayer.name} rolled $rolled • Tap a glowing pawn!"
            }
        }
    }

    LaunchedEffect(activeTurnListIndex, isRolling, awaitingPawnSelection, isMovingPawn, winnerConfig) {
        val activeP = playerConfigs.getOrNull(activeTurnListIndex)
        if (activeP != null && activeP.isAi && !isRolling && !awaitingPawnSelection && !isMovingPawn && winnerConfig == null) {
            delay(550)
            rollDiceForCurrentPlayer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("ludo_game_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
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
                        .testTag("ludo_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${activeSkin.name.uppercase()} • ${if (vsComputer) "VS AI" else "LOCAL ${playerCount}P"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = "Ludo Royal 3D",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onOpenPawnVault,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("ludo_pawn_vault_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Pawn Collection", tint = GoldLight)
                }
                IconButton(
                    onClick = { showModeSheet = !showModeSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("ludo_mode_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Mode & AI", tint = CyanGlow)
                }
                IconButton(
                    onClick = { resetGame() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("ludo_restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                }
            }
        }

        if (showModeSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = CyanGlow) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Switch Ludo Mode", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !vsComputer,
                            onClick = {
                                vsComputer = false
                                resetGame()
                            },
                            label = { Text("Mode 1: Local Room") }
                        )
                        FilterChip(
                            selected = vsComputer,
                            onClick = {
                                vsComputer = true
                                resetGame()
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
                                    resetGame()
                                },
                                label = { Text("${cnt}P") }
                            )
                        }
                    }
                }
            }
        }

        // Player Quadrant Status Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            playerConfigs.forEachIndexed { idx, cfg ->
                val isTurn = idx == activeTurnListIndex
                val homeCount = pawns.count { it.playerIndex == cfg.index && it.step == 56 }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTurn) cfg.color.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.06f))
                        .border(
                            width = if (isTurn) 2.dp else 1.dp,
                            color = if (isTurn) cfg.color else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cfg.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "Home: $homeCount/4",
                        fontSize = 11.sp,
                        color = GoldLight
                    )
                }
            }
        }

        // THE 15x15 LUDO 3D BOARD WITH RECESSED YARDS, 5-POINTED GOLDEN STARS & 60FPS PAWN GLIDE
        val currentTurnPlayer = playerConfigs[activeTurnListIndex]
        val legalPawnsForTap = if (awaitingPawnSelection) {
            getLegalPawns(currentTurnPlayer.index, diceValue)
        } else emptyList()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E293B))
                .border(3.5.dp, GoldPrimary, RoundedCornerShape(24.dp))
                .padding(6.dp)
                .testTag("ludo_board_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(awaitingPawnSelection, diceValue, activeTurnListIndex) {
                        detectTapGestures { tapOffset ->
                            if (!awaitingPawnSelection) return@detectTapGestures
                            val cellW = size.width / 15f
                            val cellH = size.height / 15f
                            val tappedPawn = legalPawnsForTap.minByOrNull { p ->
                                val gridPos = getGridCellForStep(p.playerIndex, p.pawnIndex, p.step)
                                val px = gridPos.x * cellW
                                val py = gridPos.y * cellH
                                hypot(tapOffset.x - px, tapOffset.y - py)
                            }
                            if (tappedPawn != null) {
                                val gridPos = getGridCellForStep(tappedPawn.playerIndex, tappedPawn.pawnIndex, tappedPawn.step)
                                val dist = hypot(
                                    tapOffset.x - gridPos.x * cellW,
                                    tapOffset.y - gridPos.y * cellH
                                )
                                if (dist <= cellW * 1.9f) {
                                    executePawnMove(tappedPawn, diceValue)
                                }
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val cellW = w / 15f
                val cellH = h / 15f

                // 1. Draw 15x15 Bevelled Grid Cells
                for (r in 0..14) {
                    for (c in 0..14) {
                        val cellColor = when {
                            r == 7 && c in 1..5 -> LudoRed.copy(alpha = 0.88f)
                            c == 7 && r in 1..5 -> LudoGreen.copy(alpha = 0.88f)
                            r == 7 && c in 9..13 -> LudoYellow.copy(alpha = 0.88f)
                            c == 7 && r in 9..13 -> LudoBlue.copy(alpha = 0.88f)
                            r == 6 && c == 1 -> LudoRed
                            r == 1 && c == 8 -> LudoGreen
                            r == 8 && c == 13 -> LudoYellow
                            r == 13 && c == 6 -> LudoBlue
                            else -> Color(0xFFF8FAFC)
                        }
                        drawRect(
                            color = cellColor,
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH)
                        )
                        drawRect(
                            color = Color(0xFF94A3B8),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH),
                            style = Stroke(width = 1.2f)
                        )
                    }
                }

                // 2. Draw 4 Recessed 3D Corner 6x6 Base Yards
                val yards = listOf(
                    Triple(0, 0, LudoRed),
                    Triple(9, 0, LudoGreen),
                    Triple(9, 9, LudoYellow),
                    Triple(0, 9, LudoBlue)
                )
                yards.forEach { (startC, startR, col) ->
                    val topLeft = Offset(startC * cellW, startR * cellH)
                    val yardSize = Size(cellW * 6f, cellH * 6f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(col, col.copy(alpha = 0.78f)),
                            start = topLeft,
                            end = topLeft + Offset(yardSize.width, yardSize.height)
                        ),
                        topLeft = topLeft,
                        size = yardSize
                    )
                    // Recessed shadow inside yard
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = topLeft + Offset(cellW * 0.78f, cellH * 0.78f),
                        size = Size(cellW * 4.44f, cellH * 4.44f),
                        cornerRadius = CornerRadius(14.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFF8FAFC),
                        topLeft = topLeft + Offset(cellW * 0.85f, cellH * 0.85f),
                        size = Size(cellW * 4.3f, cellH * 4.3f),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                    // 4 Glass-Domed Base Sockets
                    for (k in 0..3) {
                        val dx = if (k % 2 == 0) 1.8f else 4.0f
                        val dy = if (k < 2) 1.8f else 4.0f
                        val socketCenter = topLeft + Offset((dx + 0.1f) * cellW, (dy + 0.1f) * cellH)
                        drawCircle(
                            color = col.copy(alpha = 0.28f),
                            radius = cellW * 0.65f,
                            center = socketCenter
                        )
                        drawCircle(
                            color = col,
                            radius = cellW * 0.65f,
                            center = socketCenter,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // 3. Draw 8 Five-Pointed Golden Star Safe Squares
                SAFE_GLOBAL_INDICES.forEach { gIdx ->
                    val (c, r) = OUTER_TRACK_COORDS[gIdx]
                    val center = Offset((c + 0.5f) * cellW, (r + 0.5f) * cellH)
                    val outerR = cellW * 0.38f
                    val innerR = cellW * 0.17f
                    val starPath = Path().apply {
                        for (pt in 0 until 10) {
                            val rad = if (pt % 2 == 0) outerR else innerR
                            val ang = (pt * 36.0 - 90.0) * PI / 180.0
                            val px = center.x + cos(ang).toFloat() * rad
                            val py = center.y + sin(ang).toFloat() * rad
                            if (pt == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(starPath, color = GoldPrimary.copy(alpha = 0.78f))
                }

                // 4. Draw Center 3x3 Home Victory Triangles with Specular Border
                val cLeft = 6f * cellW
                val cTop = 6f * cellH
                val cRight = 9f * cellW
                val cBottom = 9f * cellH
                val cMid = Offset(7.5f * cellW, 7.5f * cellH)

                val triRed = Path().apply { moveTo(cLeft, cTop); lineTo(cMid.x, cMid.y); lineTo(cLeft, cBottom); close() }
                val triGreen = Path().apply { moveTo(cLeft, cTop); lineTo(cRight, cTop); lineTo(cMid.x, cMid.y); close() }
                val triYellow = Path().apply { moveTo(cRight, cTop); lineTo(cRight, cBottom); lineTo(cMid.x, cMid.y); close() }
                val triBlue = Path().apply { moveTo(cLeft, cBottom); lineTo(cRight, cBottom); lineTo(cMid.x, cMid.y); close() }

                drawPath(triRed, LudoRed)
                drawPath(triGreen, LudoGreen)
                drawPath(triYellow, LudoYellow)
                drawPath(triBlue, LudoBlue)
                drawCircle(Color(0xFFFEF08A), radius = cellW * 0.26f, center = cMid)

                // 5. Draw All Active Player Pawns with 60fps Glide Interpolation!
                val activePlayerIndices = playerConfigs.map { it.index }.toSet()
                pawns.filter { it.playerIndex in activePlayerIndices }.forEach { pawn ->
                    val isCurrentlyGliding = movingPawnKey == (pawn.playerIndex to pawn.pawnIndex) && interpolatedGridPos != null
                    val baseGrid = if (isCurrentlyGliding) {
                        interpolatedGridPos!!
                    } else {
                        getGridCellForStep(pawn.playerIndex, pawn.pawnIndex, pawn.step)
                    }
                    val hopScale = if (isCurrentlyGliding) movingPawnHopScale else 1f
                    val microShift = Offset(
                        ((pawn.pawnIndex % 2) - 0.5f) * cellW * 0.14f,
                        ((pawn.pawnIndex / 2) - 0.5f) * cellH * 0.14f
                    )
                    val center = Offset(
                        baseGrid.x * cellW + (if (pawn.step == -1 || isCurrentlyGliding) 0f else microShift.x),
                        baseGrid.y * cellH + (if (pawn.step == -1 || isCurrentlyGliding) 0f else microShift.y)
                    )
                    val pColor = when (pawn.playerIndex) {
                        0 -> LudoRed
                        1 -> LudoGreen
                        2 -> LudoYellow
                        else -> LudoBlue
                    }
                    val isSelectable = legalPawnsForTap.any {
                        it.playerIndex == pawn.playerIndex && it.pawnIndex == pawn.pawnIndex
                    }

                    drawPawnSilhouetteOnScope(
                        skinId = selectedPawnSkinId,
                        center = center,
                        radius = cellW * (if (isSelectable) 0.48f else 0.41f) * hopScale,
                        baseColor = pColor,
                        isHighlighted = isSelectable
                    )
                }
            }
        }

        // Bottom Control Bar with 3D Dice & Quick Move Selector
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            accentGlow = currentTurnPlayer.color
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TURN: ${currentTurnPlayer.name.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentTurnPlayer.color
                        )
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val canRoll = !currentTurnPlayer.isAi && !isRolling && !awaitingPawnSelection && !isMovingPawn && winnerConfig == null
                        Button(
                            onClick = { rollDiceForCurrentPlayer() },
                            enabled = canRoll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTurnPlayer.color,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("ludo_roll_dice_button")
                        ) {
                            Text(
                                text = when {
                                    isRolling -> "Rolling..."
                                    awaitingPawnSelection -> "Tap a Glowing Pawn"
                                    currentTurnPlayer.isAi -> "AI Thinking..."
                                    else -> "Roll 3D Dice"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Animated3DDice(
                        value = diceValue,
                        isRolling = isRolling,
                        accentColor = currentTurnPlayer.color,
                        enabled = !currentTurnPlayer.isAi && !awaitingPawnSelection && !isMovingPawn,
                        size = 66.dp,
                        onRollClick = { rollDiceForCurrentPlayer() }
                    )
                }

                if (awaitingPawnSelection && legalPawnsForTap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        legalPawnsForTap.forEach { legalPawn ->
                            Button(
                                onClick = { executePawnMove(legalPawn, diceValue) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                val label = if (legalPawn.step == -1) {
                                    "Pawn #${legalPawn.pawnIndex + 1} (Exit)"
                                } else {
                                    "Pawn #${legalPawn.pawnIndex + 1} (+$diceValue)"
                                }
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    winnerConfig?.let { champ ->
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF111C44),
            title = {
                Text("👑 ${champ.name} Wins Ludo Royal!", color = GoldLight, style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                Text(
                    "All 4 ${activeSkin.name} pawns reached the central victory triangle! Opponents captured by you: $capturesByUser.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { resetGame() },
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
