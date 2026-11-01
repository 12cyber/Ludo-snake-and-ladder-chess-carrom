package com.example.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.AiDifficulty
import com.example.data.CarromBoardTheme
import com.example.data.CarromCoinStyle
import com.example.data.GameLaunchConfig
import com.example.data.PlayModeType
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RubyDanger
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class DiscKind {
    STRIKER,
    WHITE_COIN,
    BLACK_COIN,
    QUEEN
}

data class CarromDisc(
    val id: Int,
    val kind: DiscKind,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float,
    val mass: Float,
    var pocketed: Boolean = false
)

private enum class HandTouchPhase {
    IDLE,
    SLIDING_BASELINE,
    AIMING_PULLBACK
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarromGameScreen(
    playerUsername: String,
    launchConfig: GameLaunchConfig,
    initialBoardTheme: CarromBoardTheme,
    initialCoinStyle: CarromCoinStyle,
    soundManager: SoundManager,
    onSelectThemes: (CarromBoardTheme, CarromCoinStyle) -> Unit,
    onSaveSession: (String, String) -> Unit,
    onMatchFinished: (Boolean, String, String, String, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var boardTheme by remember { mutableStateOf(initialBoardTheme) }
    var coinStyle by remember { mutableStateOf(initialCoinStyle) }
    var playMode by remember(launchConfig) { mutableStateOf(launchConfig.playMode) }
    var difficulty by remember(launchConfig) { mutableStateOf(launchConfig.aiDifficulty) }
    var showStyleSheet by remember { mutableStateOf(false) }
    var showModeSheet by remember { mutableStateOf(false) }

    val discs = remember { mutableStateListOf<CarromDisc>() }

    // Turn & Scoring State: 0 = Bottom (White Coins), 1 = Top (Black Coins)
    var currentTurn by remember { mutableIntStateOf(0) }
    var whiteScore by remember { mutableIntStateOf(0) }
    var blackScore by remember { mutableIntStateOf(0) }
    var queenPendingCoverBy by remember { mutableIntStateOf(-1) }
    var queenCoveredBy by remember { mutableIntStateOf(-1) }
    var queensPocketedByUser by remember { mutableIntStateOf(0) }

    // Direct Hand-Touch Striker Controls (Slide on baseline -> Pull back -> Release to shoot)
    var strikerBaselineX by remember { mutableFloatStateOf(0.50f) } // 0.22f..0.78f
    var aimAngleDeg by remember { mutableFloatStateOf(-90f) }
    var shotPower by remember { mutableFloatStateOf(0.0f) }
    var touchPhase by remember { mutableStateOf(HandTouchPhase.IDLE) }
    var fingerBoardNormalizedPos by remember { mutableStateOf<Offset?>(null) }
    var isStrikerOverlappingCoin by remember { mutableStateOf(false) }

    var isSimulatingShot by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Slide striker on baseline, pull back & release to shoot!") }
    var winnerLabel by remember { mutableStateOf<String?>(null) }

    val playMin = 0.075f
    val playMax = 0.925f
    val pocketRadius = 0.048f
    val pockets = remember {
        listOf(
            Offset(playMin + 0.026f, playMin + 0.026f),
            Offset(playMax - 0.026f, playMin + 0.026f),
            Offset(playMin + 0.026f, playMax - 0.026f),
            Offset(playMax - 0.026f, playMax - 0.026f)
        )
    }

    fun checkStrikerOverlap(xPos: Float, yPos: Float, strikerRadius: Float): Boolean {
        return discs.any { other ->
            other.kind != DiscKind.STRIKER &&
                !other.pocketed &&
                hypot(other.x - xPos, other.y - yPos) < (strikerRadius + other.radius) * 0.96f
        }
    }

    fun placeStrikerForTurn(turn: Int, baseX: Float) {
        val clampedX = baseX.coerceIn(0.22f, 0.78f)
        val baseY = if (turn == 0) 0.80f else 0.20f
        val idx = discs.indexOfFirst { it.kind == DiscKind.STRIKER }
        if (idx >= 0) {
            val st = discs[idx]
            isStrikerOverlappingCoin = checkStrikerOverlap(clampedX, baseY, st.radius)
            discs[idx] = st.copy(
                x = clampedX,
                y = baseY,
                vx = 0f,
                vy = 0f,
                pocketed = false
            )
        }
    }

    fun setupBoard() {
        discs.clear()
        val cx = 0.50f
        val cy = 0.50f
        val coinR = 0.026f
        val strikerR = 0.034f

        // 0: Striker
        discs.add(
            CarromDisc(
                id = 0,
                kind = DiscKind.STRIKER,
                x = 0.50f,
                y = 0.80f,
                radius = strikerR,
                mass = 2.2f
            )
        )
        // 1: Center Ruby Queen
        discs.add(
            CarromDisc(
                id = 1,
                kind = DiscKind.QUEEN,
                x = cx,
                y = cy,
                radius = coinR,
                mass = 1.0f
            )
        )
        // Inner ring of 6 coins
        val ring1Dist = coinR * 2.10f
        for (i in 0 until 6) {
            val ang = (i * 60.0) * PI / 180.0
            val kind = if (i % 2 == 0) DiscKind.WHITE_COIN else DiscKind.BLACK_COIN
            discs.add(
                CarromDisc(
                    id = 2 + i,
                    kind = kind,
                    x = (cx + cos(ang) * ring1Dist).toFloat(),
                    y = (cy + sin(ang) * ring1Dist).toFloat(),
                    radius = coinR,
                    mass = 1.0f
                )
            )
        }
        // Outer ring of 12 coins
        val ring2Dist = coinR * 4.18f
        for (i in 0 until 12) {
            val ang = (i * 30.0 + 15.0) * PI / 180.0
            val kind = if (i % 2 == 0) DiscKind.WHITE_COIN else DiscKind.BLACK_COIN
            discs.add(
                CarromDisc(
                    id = 8 + i,
                    kind = kind,
                    x = (cx + cos(ang) * ring2Dist).toFloat(),
                    y = (cy + sin(ang) * ring2Dist).toFloat(),
                    radius = coinR,
                    mass = 1.0f
                )
            )
        }

        currentTurn = 0
        whiteScore = 0
        blackScore = 0
        queenPendingCoverBy = -1
        queenCoveredBy = -1
        strikerBaselineX = 0.50f
        aimAngleDeg = -90f
        shotPower = 0.0f
        touchPhase = HandTouchPhase.IDLE
        fingerBoardNormalizedPos = null
        isSimulatingShot = false
        winnerLabel = null
        statusText = "$playerUsername: Slide striker on baseline, pull back & release!"
    }

    LaunchedEffect(launchConfig) {
        setupBoard()
    }

    val pocketedThisShot = remember { mutableStateListOf<DiscKind>() }

    fun fireShot(powerToUse: Float = shotPower, angleDegToUse: Float = aimAngleDeg) {
        if (isSimulatingShot || winnerLabel != null || isStrikerOverlappingCoin) return
        val sIdx = discs.indexOfFirst { it.kind == DiscKind.STRIKER }
        if (sIdx == -1) return

        pocketedThisShot.clear()
        val rad = (angleDegToUse * PI / 180.0).toFloat()
        val clampedPower = powerToUse.coerceIn(0.15f, 1.0f)
        val speed = 0.014f + clampedPower * 0.038f
        val st = discs[sIdx]
        discs[sIdx] = st.copy(
            vx = cos(rad) * speed,
            vy = sin(rad) * speed,
            pocketed = false
        )
        touchPhase = HandTouchPhase.IDLE
        fingerBoardNormalizedPos = null
        soundManager.playCarromStrike(clampedPower)
        isSimulatingShot = true
    }

    // 240Hz Sub-Stepped 60fps Rigid-Body 2D Physics Simulation Loop
    LaunchedEffect(isSimulatingShot) {
        if (!isSimulatingShot) return@LaunchedEffect

        var frameCount = 0
        while (isSimulatingShot) {
            withFrameMillis {
                frameCount++
                val subSteps = 4
                var maxSpeedSq = 0f
                var collisionSoundCooldown = 0

                repeat(subSteps) {
                    // 1. Integrate positions & elastic wood bumper rebounds
                    for (i in discs.indices) {
                        val d = discs[i]
                        if (d.pocketed) continue

                        var nx = d.x + d.vx / subSteps
                        var ny = d.y + d.vy / subSteps
                        var nvx = d.vx
                        var nvy = d.vy

                        val minBound = playMin + d.radius
                        val maxBound = playMax - d.radius

                        if (nx < minBound) {
                            nx = minBound
                            nvx = -nvx * 0.84f
                        } else if (nx > maxBound) {
                            nx = maxBound
                            nvx = -nvx * 0.84f
                        }

                        if (ny < minBound) {
                            ny = minBound
                            nvy = -nvy * 0.84f
                        } else if (ny > maxBound) {
                            ny = maxBound
                            nvy = -nvy * 0.84f
                        }

                        var fellInPocket = false
                        for (p in pockets) {
                            if (hypot(nx - p.x, ny - p.y) < pocketRadius) {
                                fellInPocket = true
                                break
                            }
                        }

                        if (fellInPocket) {
                            pocketedThisShot.add(d.kind)
                            soundManager.playCarromPocket()
                            discs[i] = d.copy(x = nx, y = ny, vx = 0f, vy = 0f, pocketed = true)
                        } else {
                            discs[i] = d.copy(x = nx, y = ny, vx = nvx, vy = nvy)
                        }
                    }

                    // 2. Elastic Circle-Circle Collision Resolution
                    for (i in 0 until discs.size) {
                        val a = discs[i]
                        if (a.pocketed) continue
                        for (j in (i + 1) until discs.size) {
                            val b = discs[j]
                            if (b.pocketed) continue

                            val dx = b.x - a.x
                            val dy = b.y - a.y
                            val dist = hypot(dx, dy)
                            val minDist = a.radius + b.radius

                            if (dist < minDist && dist > 0.0001f) {
                                val nx = dx / dist
                                val ny = dy / dist
                                val overlap = minDist - dist

                                val totalMass = a.mass + b.mass
                                val aShare = b.mass / totalMass
                                val bShare = a.mass / totalMass

                                val ax = a.x - nx * overlap * aShare
                                val ay = a.y - ny * overlap * aShare
                                val bx = b.x + nx * overlap * bShare
                                val by = b.y + ny * overlap * bShare

                                val rvx = b.vx - a.vx
                                val rvy = b.vy - a.vy
                                val velAlongNormal = rvx * nx + rvy * ny

                                var avx = a.vx
                                var avy = a.vy
                                var bvx = b.vx
                                var bvy = b.vy

                                if (velAlongNormal < 0f) {
                                    val restitution = 0.89f
                                    val impulse = -(1f + restitution) * velAlongNormal / (1f / a.mass + 1f / b.mass)
                                    val ix = impulse * nx
                                    val iy = impulse * ny
                                    avx -= ix / a.mass
                                    avy -= iy / a.mass
                                    bvx += ix / b.mass
                                    bvy += iy / b.mass

                                    if (collisionSoundCooldown <= 0) {
                                        soundManager.playCarromCollision((-velAlongNormal * 25f).coerceIn(0.15f, 1f))
                                        collisionSoundCooldown = 6
                                    }
                                }

                                discs[i] = a.copy(x = ax, y = ay, vx = avx, vy = avy)
                                discs[j] = b.copy(x = bx, y = by, vx = bvx, vy = bvy)
                            }
                        }
                    }
                    if (collisionSoundCooldown > 0) collisionSoundCooldown--
                }

                // 3. Smooth Powdered Wood Surface Friction
                for (i in discs.indices) {
                    val d = discs[i]
                    if (d.pocketed) continue
                    val nvx = d.vx * 0.982f
                    val nvy = d.vy * 0.982f
                    val spdSq = nvx * nvx + nvy * nvy
                    if (spdSq > maxSpeedSq) maxSpeedSq = spdSq
                    if (spdSq < 0.0000007f) {
                        discs[i] = d.copy(vx = 0f, vy = 0f)
                    } else {
                        discs[i] = d.copy(vx = nvx, vy = nvy)
                    }
                }

                if (maxSpeedSq < 0.0000009f || frameCount > 450) {
                    isSimulatingShot = false
                }
            }
        }

        // Evaluate Turn Outcome & Standard Carrom Rules
        val myTargetKind = if (currentTurn == 0) DiscKind.WHITE_COIN else DiscKind.BLACK_COIN
        val myCoinsPocketed = pocketedThisShot.count { it == myTargetKind }
        val oppCoinsPocketed = pocketedThisShot.count {
            it == (if (currentTurn == 0) DiscKind.BLACK_COIN else DiscKind.WHITE_COIN)
        }
        val queenPocketedNow = pocketedThisShot.contains(DiscKind.QUEEN)
        val strikerFoul = pocketedThisShot.contains(DiscKind.STRIKER)

        whiteScore += pocketedThisShot.count { it == DiscKind.WHITE_COIN } * 10
        blackScore += pocketedThisShot.count { it == DiscKind.BLACK_COIN } * 10

        var keepTurn = false

        if (strikerFoul) {
            statusText = "⚠️ Striker Foul! Turn passes."
            if (queenPendingCoverBy == currentTurn || queenPocketedNow) {
                val qIdx = discs.indexOfFirst { it.kind == DiscKind.QUEEN }
                if (qIdx >= 0) discs[qIdx] = discs[qIdx].copy(x = 0.50f, y = 0.50f, vx = 0f, vy = 0f, pocketed = false)
                queenPendingCoverBy = -1
            }
        } else {
            if (queenPendingCoverBy == currentTurn) {
                if (myCoinsPocketed > 0) {
                    queenCoveredBy = currentTurn
                    queenPendingCoverBy = -1
                    if (currentTurn == 0) {
                        whiteScore += 50
                        queensPocketedByUser++
                    } else {
                        blackScore += 50
                    }
                    statusText = "👑 QUEEN COVERED (+50 pts)! Bonus Shot!"
                    keepTurn = true
                } else {
                    val qIdx = discs.indexOfFirst { it.kind == DiscKind.QUEEN }
                    if (qIdx >= 0) discs[qIdx] = discs[qIdx].copy(x = 0.50f, y = 0.50f, vx = 0f, vy = 0f, pocketed = false)
                    queenPendingCoverBy = -1
                    statusText = "Queen not covered • Returned to center rosette!"
                }
            } else if (queenPocketedNow) {
                if (myCoinsPocketed > 0) {
                    queenCoveredBy = currentTurn
                    if (currentTurn == 0) {
                        whiteScore += 50
                        queensPocketedByUser++
                    } else {
                        blackScore += 50
                    }
                    statusText = "👑 Queen Pocketed & Covered (+50 pts)!"
                    keepTurn = true
                } else {
                    queenPendingCoverBy = currentTurn
                    statusText = "👑 Queen Pocketed! Pocket your coin next to cover it!"
                    keepTurn = true
                }
            } else if (myCoinsPocketed > 0) {
                statusText = "🎯 Pocketed $myCoinsPocketed coin(s)! Bonus Shot!"
                keepTurn = true
            } else if (oppCoinsPocketed > 0) {
                statusText = "Pocketed opponent's coin"
            }
        }

        val remainingWhite = discs.count { it.kind == DiscKind.WHITE_COIN && !it.pocketed }
        val remainingBlack = discs.count { it.kind == DiscKind.BLACK_COIN && !it.pocketed }
        val queenStillOut = discs.any { it.kind == DiscKind.QUEEN && !it.pocketed }

        if ((remainingWhite == 0 || remainingBlack == 0) && !queenStillOut && playMode != PlayModeType.PRACTICE) {
            val userWon = whiteScore >= blackScore
            val champ = if (userWon) playerUsername else (if (playMode == PlayModeType.VS_COMPUTER) "Carrom AI" else "Player 2")
            winnerLabel = "$champ Wins ($whiteScore - $blackScore)!"
            onMatchFinished(
                userWon,
                launchConfig.modeDisplayName,
                if (playMode == PlayModeType.VS_COMPUTER) "Carrom AI (${difficulty.label})" else "Player 2",
                "Score $whiteScore - $blackScore on ${boardTheme.displayName}",
                queensPocketedByUser
            )
            return@LaunchedEffect
        }

        if (!keepTurn && playMode != PlayModeType.PRACTICE) {
            currentTurn = if (currentTurn == 0) 1 else 0
        }

        strikerBaselineX = 0.50f
        aimAngleDeg = if (currentTurn == 0) -90f else 90f
        shotPower = 0f
        placeStrikerForTurn(currentTurn, strikerBaselineX)

        val turnOwner = if (currentTurn == 0) "$playerUsername (White)" else {
            if (playMode == PlayModeType.VS_COMPUTER) "AI Bot (Black)" else "Player 2 (Black)"
        }
        if (!keepTurn) {
            statusText = "$turnOwner: Slide on baseline, pull back & release!"
        }
        onSaveSession(
            "${launchConfig.modeDisplayName} • ${boardTheme.displayName}",
            "Score: White $whiteScore - Black $blackScore"
        )
    }

    // Computer AI Opponent Turn Logic
    LaunchedEffect(currentTurn, isSimulatingShot, playMode, winnerLabel) {
        if (playMode == PlayModeType.VS_COMPUTER && currentTurn == 1 && !isSimulatingShot && winnerLabel == null) {
            statusText = "🤖 AI Bot is lining up striker on top baseline..."
            delay(650)
            val targets = discs.filter {
                !it.pocketed && (it.kind == DiscKind.BLACK_COIN || it.kind == DiscKind.QUEEN)
            }
            val chosenTarget = targets.minByOrNull { it.y }
                ?: discs.firstOrNull { !it.pocketed && it.kind != DiscKind.STRIKER }
            if (chosenTarget != null) {
                val targetX = (chosenTarget.x + Random.nextFloat() * 0.12f - 0.06f).coerceIn(0.24f, 0.76f)
                // Smoothly slide AI striker along top baseline
                val steps = 14
                val startX = strikerBaselineX
                for (s in 1..steps) {
                    val t = s / steps.toFloat()
                    strikerBaselineX = startX + (targetX - startX) * t
                    placeStrikerForTurn(1, strikerBaselineX)
                    delay(18)
                }

                val dx = chosenTarget.x - strikerBaselineX
                val dy = chosenTarget.y - 0.20f
                val idealAngleDeg = (atan2(dy, dx) * 180.0 / PI).toFloat()
                val jitter = when (difficulty) {
                    AiDifficulty.BEGINNER -> (Random.nextFloat() - 0.5f) * 18f
                    AiDifficulty.EASY -> (Random.nextFloat() - 0.5f) * 11f
                    AiDifficulty.MEDIUM -> (Random.nextFloat() - 0.5f) * 5.5f
                    AiDifficulty.HARD -> (Random.nextFloat() - 0.5f) * 2.5f
                    AiDifficulty.EXPERT -> (Random.nextFloat() - 0.5f) * 0.9f
                }
                val aiAngle = idealAngleDeg + jitter
                val aiPower = (0.60f + Random.nextFloat() * 0.26f).coerceIn(0.48f, 0.90f)
                aimAngleDeg = aiAngle
                shotPower = aiPower
                touchPhase = HandTouchPhase.AIMING_PULLBACK
                delay(380)
                fireShot(aiPower, aiAngle)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("carrom_game_screen"),
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
                        .testTag("carrom_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${boardTheme.displayName.uppercase()} • ${coinStyle.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = "Carrom Pro 3D",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showStyleSheet = !showStyleSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("carrom_theme_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Board & Coin Themes", tint = GoldLight)
                }
                IconButton(
                    onClick = { showModeSheet = !showModeSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("carrom_mode_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Game Mode", tint = CyanGlow)
                }
                IconButton(
                    onClick = { setupBoard() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("carrom_restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Board", tint = Color.White)
                }
            }
        }

        if (showStyleSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = GoldPrimary) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Board Theme (7 Editions)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CarromBoardTheme.entries.forEach { t ->
                            FilterChip(
                                selected = boardTheme == t,
                                onClick = {
                                    boardTheme = t
                                    onSelectThemes(t, coinStyle)
                                },
                                label = { Text(t.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                    Text("Coin Finish (7 Materials)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CarromCoinStyle.entries.forEach { c ->
                            FilterChip(
                                selected = coinStyle == c,
                                onClick = {
                                    coinStyle = c
                                    onSelectThemes(boardTheme, c)
                                },
                                label = { Text(c.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        if (showModeSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = CyanGlow) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Switch Carrom Mode", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(PlayModeType.LOCAL_ROOM, PlayModeType.VS_COMPUTER, PlayModeType.PRACTICE).forEach { m ->
                            FilterChip(
                                selected = playMode == m,
                                onClick = {
                                    playMode = m
                                    setupBoard()
                                },
                                label = { Text(m.title) }
                            )
                        }
                    }
                    if (playMode == PlayModeType.VS_COMPUTER) {
                        Text("AI Opponent Precision", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AiDifficulty.entries.forEach { d ->
                                FilterChip(
                                    selected = difficulty == d,
                                    onClick = { difficulty = d },
                                    label = { Text(d.label) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Player Scoreboard Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val whiteRemaining = discs.count { it.kind == DiscKind.WHITE_COIN && !it.pocketed }
            val blackRemaining = discs.count { it.kind == DiscKind.BLACK_COIN && !it.pocketed }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (currentTurn == 0) GoldPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.07f))
                    .border(
                        width = if (currentTurn == 0) 2.dp else 1.dp,
                        color = if (currentTurn == 0) GoldLight else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Text("$playerUsername (White)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Score: $whiteScore • Coins Left: $whiteRemaining", color = GoldLight, fontSize = 12.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (currentTurn == 1) CyanGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.07f))
                    .border(
                        width = if (currentTurn == 1) 2.dp else 1.dp,
                        color = if (currentTurn == 1) CyanGlow else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    val p2Title = if (playMode == PlayModeType.VS_COMPUTER) "AI (${difficulty.label})" else "Player 2 (Black)"
                    Text(p2Title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Score: $blackScore • Coins Left: $blackRemaining", color = CyanGlow, fontSize = 12.sp)
                }
            }
        }

        // HYPER-REALISTIC 3D WOODEN CARROM BOARD WITH DIRECT HAND SLIDE & PULL-BACK SLINGSHOT
        val isHumanTurn = !(playMode == PlayModeType.VS_COMPUTER && currentTurn == 1) && !isSimulatingShot && winnerLabel == null

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            boardTheme.borderColor,
                            Color(0xFF1A0B04),
                            boardTheme.borderColor
                        )
                    )
                )
                .border(3.5.dp, GoldPrimary.copy(alpha = 0.85f), RoundedCornerShape(28.dp))
                .testTag("carrom_board_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isHumanTurn, currentTurn) {
                        if (!isHumanTurn) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val w = size.width.toFloat().coerceAtLeast(1f)
                            val h = size.height.toFloat().coerceAtLeast(1f)
                            val activeBaseY = if (currentTurn == 0) 0.80f else 0.20f

                            val downNormX = (down.position.x / w).coerceIn(0f, 1f)
                            val downNormY = (down.position.y / h).coerceIn(0f, 1f)

                            // Determine if user touched near the baseline/striker to position or pull back
                            val distToStriker = hypot(downNormX - strikerBaselineX, downNormY - activeBaseY)
                            val nearBaselineStrip = abs(downNormY - activeBaseY) < 0.14f

                            if (nearBaselineStrip && distToStriker > 0.065f) {
                                // Move striker immediately to finger X on baseline
                                strikerBaselineX = downNormX.coerceIn(0.22f, 0.78f)
                                placeStrikerForTurn(currentTurn, strikerBaselineX)
                            }

                            touchPhase = HandTouchPhase.SLIDING_BASELINE
                            statusText = "✋ Slide left/right on baseline, then pull back & release to shoot!"

                            var currentPhase = HandTouchPhase.SLIDING_BASELINE
                            var activePullPower = 0f
                            var activeAimAngle = if (currentTurn == 0) -90f else 90f

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    // Finger lifted! If in AIMING_PULLBACK with sufficient tension, shoot!
                                    if (currentPhase == HandTouchPhase.AIMING_PULLBACK && activePullPower >= 0.14f) {
                                        if (!isStrikerOverlappingCoin) {
                                            fireShot(activePullPower, activeAimAngle)
                                        } else {
                                            statusText = "⚠️ Slide striker away from overlapping coin first!"
                                            touchPhase = HandTouchPhase.IDLE
                                            fingerBoardNormalizedPos = null
                                        }
                                    } else {
                                        touchPhase = HandTouchPhase.IDLE
                                        fingerBoardNormalizedPos = null
                                        shotPower = 0f
                                        statusText = "Striker positioned • Pull back from striker & release to strike!"
                                    }
                                    break
                                }

                                change.consume()
                                val nx = (change.position.x / w).coerceIn(0.02f, 0.98f)
                                val ny = (change.position.y / h).coerceIn(0.02f, 0.98f)
                                fingerBoardNormalizedPos = Offset(nx, ny)

                                val dyFromBase = abs(ny - activeBaseY)
                                if (currentPhase == HandTouchPhase.SLIDING_BASELINE) {
                                    if (dyFromBase < 0.052f) {
                                        // Sliding horizontally along baseline
                                        strikerBaselineX = nx.coerceIn(0.22f, 0.78f)
                                        placeStrikerForTurn(currentTurn, strikerBaselineX)
                                        shotPower = 0f
                                    } else {
                                        // Transition smoothly into slingshot pull-back aiming!
                                        currentPhase = HandTouchPhase.AIMING_PULLBACK
                                        touchPhase = HandTouchPhase.AIMING_PULLBACK
                                    }
                                }

                                if (currentPhase == HandTouchPhase.AIMING_PULLBACK) {
                                    val pullDx = strikerBaselineX - nx
                                    val pullDy = activeBaseY - ny
                                    val pullDist = hypot(pullDx, pullDy)

                                    if (pullDist < 0.032f) {
                                        // Finger returned inside striker radius -> back to baseline sliding
                                        currentPhase = HandTouchPhase.SLIDING_BASELINE
                                        touchPhase = HandTouchPhase.SLIDING_BASELINE
                                        activePullPower = 0f
                                        shotPower = 0f
                                        statusText = "✋ Shot cancelled • Slide on baseline or pull back again"
                                    } else {
                                        // Slingshot: pulling away from target shoots in opposite vector (pullDx, pullDy)
                                        activeAimAngle = (atan2(pullDy, pullDx) * 180.0 / PI).toFloat()
                                        activePullPower = ((pullDist - 0.025f) / 0.24f).coerceIn(0.10f, 1.0f)
                                        aimAngleDeg = activeAimAngle
                                        shotPower = activePullPower
                                        statusText = "🎯 Power ${(activePullPower * 100).toInt()}% • Release finger to STRIKE!"
                                    }
                                }
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                // 1. 3D Rosewood Bumper Frame & Brushed Brass Corner Plates
                val pad = w * playMin
                val playW = w * (playMax - playMin)

                // Brass corner reinforcement brackets
                val bracketLen = w * 0.14f
                val bracketStroke = 6.dp.toPx()
                val brassColor = Color(0xFFD97706)
                // Inner recessed shadow of wooden frame
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.65f),
                    topLeft = Offset(pad - 4.dp.toPx(), pad - 4.dp.toPx()),
                    size = Size(playW + 8.dp.toPx(), playW + 8.dp.toPx()),
                    cornerRadius = CornerRadius(18.dp.toPx())
                )

                // Birch Playing Surface with Warm Radial Spotlight
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            boardTheme.surfaceColor,
                            boardTheme.surfaceColor.copy(
                                red = (boardTheme.surfaceColor.red * 0.91f).coerceIn(0f, 1f),
                                green = (boardTheme.surfaceColor.green * 0.89f).coerceIn(0f, 1f),
                                blue = (boardTheme.surfaceColor.blue * 0.85f).coerceIn(0f, 1f)
                            )
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = playW * 0.72f
                    ),
                    topLeft = Offset(pad, pad),
                    size = Size(playW, playW),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )

                // Realistic Fine Wood Grain Striations
                for (g in 1..18) {
                    val yLine = pad + (playW / 19f) * g
                    drawLine(
                        color = boardTheme.lineColor.copy(alpha = 0.055f),
                        start = Offset(pad, yLine),
                        end = Offset(pad + playW, yLine),
                        strokeWidth = (1.2f + (g % 3) * 0.6f).dp.toPx()
                    )
                }

                // 2. Four Recessed Corner Pockets with Woven Netting
                pockets.forEach { p ->
                    val center = Offset(p.x * w, p.y * h)
                    val pRadPx = pocketRadius * w
                    // Outer brass pocket collar
                    drawCircle(color = brassColor, radius = pRadPx * 1.18f, center = center)
                    // Deep pocket well
                    drawCircle(color = boardTheme.pocketColor, radius = pRadPx, center = center)
                    // Woven net cross-hatching inside pocket
                    val netStep = pRadPx * 0.45f
                    for (n in -1..1) {
                        val offset = n * netStep
                        drawLine(
                            color = Color.White.copy(alpha = 0.14f),
                            start = center + Offset(-pRadPx * 0.65f, offset),
                            end = center + Offset(pRadPx * 0.65f, offset),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.14f),
                            start = center + Offset(offset, -pRadPx * 0.65f),
                            end = center + Offset(offset, pRadPx * 0.65f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // 3. Traditional Tournament Baselines, Diagonal Arrows & 8-Pointed Center Rosette
                val lineColor = boardTheme.lineColor
                // Horizontal Double Baselines (Bottom & Top)
                listOf(0.80f, 0.20f).forEachIndexed { idx, by ->
                    val isCurrentTurnBase = (idx == 0 && currentTurn == 0) || (idx == 1 && currentTurn == 1)
                    if (isCurrentTurnBase && !isSimulatingShot) {
                        // Subtle glowing hand-slide track highlight on active baseline
                        drawRoundRect(
                            color = EmeraldSuccess.copy(alpha = 0.16f),
                            topLeft = Offset(0.20f * w, (by - 0.025f) * h),
                            size = Size(0.60f * w, 0.05f * h),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                    drawLine(
                        color = lineColor,
                        start = Offset(0.22f * w, (by - 0.012f) * h),
                        end = Offset(0.78f * w, (by - 0.012f) * h),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = lineColor,
                        start = Offset(0.22f * w, (by + 0.012f) * h),
                        end = Offset(0.78f * w, (by + 0.012f) * h),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    // Baseline End Circles
                    listOf(0.22f, 0.78f).forEach { bx ->
                        val cPos = Offset(bx * w, by * h)
                        drawCircle(color = RubyDanger, radius = w * 0.018f, center = cPos)
                        drawCircle(color = lineColor, radius = w * 0.022f, center = cPos, style = Stroke(width = 1.8.dp.toPx()))
                    }
                }

                // Vertical decorative side baselines
                listOf(0.20f, 0.80f).forEach { bx ->
                    drawLine(
                        color = lineColor.copy(alpha = 0.65f),
                        start = Offset(bx * w, 0.22f * h),
                        end = Offset(bx * w, 0.78f * h),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // 4 Diagonal Foul Lines pointing toward pockets
                pockets.forEach { p ->
                    val dirX = if (p.x < 0.5f) 1f else -1f
                    val dirY = if (p.y < 0.5f) 1f else -1f
                    val start = Offset((p.x + dirX * 0.06f) * w, (p.y + dirY * 0.06f) * h)
                    val end = Offset((p.x + dirX * 0.23f) * w, (p.y + dirY * 0.23f) * h)
                    drawLine(color = lineColor.copy(alpha = 0.75f), start = start, end = end, strokeWidth = 1.6.dp.toPx())
                }

                // Center Rosette Compass Star & Circles
                val boardCenter = Offset(w * 0.5f, h * 0.5f)
                drawCircle(
                    color = lineColor,
                    radius = w * 0.145f,
                    center = boardCenter,
                    style = Stroke(width = 2.2.dp.toPx())
                )
                drawCircle(
                    color = RubyDanger.copy(alpha = 0.55f),
                    radius = w * 0.128f,
                    center = boardCenter,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                for (ray in 0 until 8) {
                    val ang = (ray * 45.0) * PI / 180.0
                    val rEnd = boardCenter + Offset(cos(ang).toFloat() * w * 0.125f, sin(ang).toFloat() * h * 0.125f)
                    drawLine(
                        color = if (ray % 2 == 0) RubyDanger.copy(alpha = 0.55f) else lineColor.copy(alpha = 0.55f),
                        start = boardCenter,
                        end = rEnd,
                        strokeWidth = 1.8.dp.toPx()
                    )
                }
                drawCircle(color = RubyDanger, radius = w * 0.028f, center = boardCenter)
                drawCircle(color = GoldLight, radius = w * 0.012f, center = boardCenter)

                // 4. Direct Hand Slingshot Tension Ring, Pull Tether & Rebound Trajectory Guide
                val striker = discs.firstOrNull { it.kind == DiscKind.STRIKER }
                if (striker != null && !isSimulatingShot && !striker.pocketed) {
                    val startPx = Offset(striker.x * w, striker.y * h)
                    val sRadPx = striker.radius * w

                    // Baseline position halo (Emerald if clear, Ruby if overlapping a coin)
                    val haloColor = if (isStrikerOverlappingCoin) RubyDanger else EmeraldSuccess
                    drawCircle(
                        color = haloColor.copy(alpha = 0.55f),
                        radius = sRadPx * 1.42f,
                        center = startPx,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    if (touchPhase == HandTouchPhase.AIMING_PULLBACK && shotPower > 0.08f) {
                        val tensionColor = when {
                            shotPower > 0.78f -> RubyDanger
                            shotPower > 0.45f -> GoldLight
                            else -> EmeraldSuccess
                        }

                        // Expanding 3D Radial Power Tension Ring around striker
                        val ringRadius = sRadPx * (1.35f + shotPower * 1.45f)
                        drawCircle(
                            color = tensionColor.copy(alpha = 0.22f),
                            radius = ringRadius,
                            center = startPx
                        )
                        drawCircle(
                            color = tensionColor,
                            radius = ringRadius,
                            center = startPx,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                            )
                        )

                        // Pull-back slingshot tether to user's finger position
                        fingerBoardNormalizedPos?.let { fNorm ->
                            val fingerPx = Offset(fNorm.x * w, fNorm.y * h)
                            drawLine(
                                color = Color.White.copy(alpha = 0.75f),
                                start = startPx,
                                end = fingerPx,
                                strokeWidth = 2.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                            drawCircle(
                                color = tensionColor,
                                radius = sRadPx * 0.65f,
                                center = fingerPx
                            )
                            drawCircle(
                                color = Color.White,
                                radius = sRadPx * 0.65f,
                                center = fingerPx,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Forward Trajectory Ray + Wall Rebound Prediction
                        val rad = (aimAngleDeg * PI / 180.0).toFloat()
                        val dirX = cos(rad)
                        val dirY = sin(rad)
                        val maxGuideLen = w * (0.28f + shotPower * 0.52f)

                        var hitX = startPx.x + dirX * maxGuideLen
                        var hitY = startPx.y + dirY * maxGuideLen
                        val minPx = pad + sRadPx
                        val maxPx = pad + playW - sRadPx

                        // Compute first wall bounce if ray reaches cushion
                        var bounceDirX = dirX
                        var bounceDirY = dirY
                        var didBounce = false

                        var tMin = 1f
                        if (dirX < -0.001f) {
                            val t = (minPx - startPx.x) / (dirX * maxGuideLen)
                            if (t in 0f..tMin) { tMin = t; bounceDirX = -dirX; bounceDirY = dirY; didBounce = true }
                        } else if (dirX > 0.001f) {
                            val t = (maxPx - startPx.x) / (dirX * maxGuideLen)
                            if (t in 0f..tMin) { tMin = t; bounceDirX = -dirX; bounceDirY = dirY; didBounce = true }
                        }
                        if (dirY < -0.001f) {
                            val t = (minPx - startPx.y) / (dirY * maxGuideLen)
                            if (t in 0f..tMin) { tMin = t; bounceDirX = dirX; bounceDirY = -dirY; didBounce = true }
                        } else if (dirY > 0.001f) {
                            val t = (maxPx - startPx.y) / (dirY * maxGuideLen)
                            if (t in 0f..tMin) { tMin = t; bounceDirX = dirX; bounceDirY = -dirY; didBounce = true }
                        }

                        hitX = startPx.x + dirX * maxGuideLen * tMin
                        hitY = startPx.y + dirY * maxGuideLen * tMin
                        val impactPx = Offset(hitX, hitY)

                        drawLine(
                            color = tensionColor,
                            start = startPx,
                            end = impactPx,
                            strokeWidth = 3.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = tensionColor.copy(alpha = 0.5f),
                            radius = sRadPx * 0.65f,
                            center = impactPx
                        )

                        if (didBounce) {
                            val remLen = maxGuideLen * (1f - tMin) * 0.65f
                            val reboundEnd = Offset(
                                (impactPx.x + bounceDirX * remLen).coerceIn(minPx, maxPx),
                                (impactPx.y + bounceDirY * remLen).coerceIn(minPx, maxPx)
                            )
                            drawLine(
                                color = CyanGlow.copy(alpha = 0.75f),
                                start = impactPx,
                                end = reboundEnd,
                                strokeWidth = 2.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // 5. Draw All Lathe-Turned 3D Wooden Coins & Tournament Striker
                discs.forEach { d ->
                    if (d.pocketed) return@forEach
                    val center = Offset(d.x * w, d.y * h)
                    val rPx = d.radius * w

                    // Soft 3D Ambient + Directional Shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.48f),
                        radius = rPx * 1.06f,
                        center = center + Offset(2.2.dp.toPx(), 3.6.dp.toPx())
                    )

                    val baseFill = when (d.kind) {
                        DiscKind.WHITE_COIN -> coinStyle.whiteCoin
                        DiscKind.BLACK_COIN -> coinStyle.blackCoin
                        DiscKind.QUEEN -> coinStyle.queenCoin
                        DiscKind.STRIKER -> Color(0xFFF8FAFC)
                    }

                    // 3D Spherical Specular Shading
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.90f),
                                baseFill,
                                baseFill.copy(
                                    red = (baseFill.red * 0.68f).coerceIn(0f, 1f),
                                    green = (baseFill.green * 0.68f).coerceIn(0f, 1f),
                                    blue = (baseFill.blue * 0.68f).coerceIn(0f, 1f)
                                )
                            ),
                            center = center - Offset(rPx * 0.32f, rPx * 0.32f),
                            radius = rPx * 1.35f
                        ),
                        radius = rPx,
                        center = center
                    )

                    // Lathe-turned concentric ridges
                    drawCircle(
                        color = if (d.kind == DiscKind.STRIKER) Color(0xFF0284C7) else coinStyle.rimColor,
                        radius = rPx * 0.76f,
                        center = center,
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                    drawCircle(
                        color = if (d.kind == DiscKind.STRIKER) GoldPrimary else coinStyle.rimColor.copy(alpha = 0.65f),
                        radius = rPx * 0.42f,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = rPx,
                        center = center,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }
            }
        }

        // Clean, Realistic Hand-Control Status Banner (No sliders or Strike buttons!)
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            accentGlow = if (currentTurn == 0) GoldPrimary else CyanGlow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (touchPhase == HandTouchPhase.AIMING_PULLBACK) "🏹" else "✋",
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DIRECT HAND TOUCH CONTROLS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldLight
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (shotPower > 0.05f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldPrimary.copy(alpha = 0.25f))
                            .border(1.dp, GoldLight, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${(shotPower * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldLight
                        )
                    }
                }
            }
        }
    }

    winnerLabel?.let { winMsg ->
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF111C44),
            title = { Text("🎯 $winMsg", color = GoldLight, style = MaterialTheme.typography.headlineMedium) },
            text = {
                Text(
                    "Final Score: White $whiteScore — Black $blackScore. Queens pocketed & covered by you: $queensPocketedByUser.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { setupBoard() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.Black)
                ) {
                    Text("Play Again", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = onBack) { Text("Home Hub") }
            }
        )
    }
}
