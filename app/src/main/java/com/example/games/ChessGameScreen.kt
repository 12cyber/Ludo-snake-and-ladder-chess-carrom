package com.example.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.AiDifficulty
import com.example.data.ChessBoardTheme
import com.example.data.ChessPieceSet
import com.example.data.GameLaunchConfig
import com.example.data.PlayModeType
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.drawRealisticChessPieceOnScope
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RubyDanger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChessGameScreen(
    playerUsername: String,
    launchConfig: GameLaunchConfig,
    initialBoardTheme: ChessBoardTheme,
    initialPieceSet: ChessPieceSet,
    soundManager: SoundManager,
    onSelectThemes: (ChessBoardTheme, ChessPieceSet) -> Unit,
    onSaveSession: (String, String) -> Unit,
    onMatchFinished: (Boolean, Boolean, String, String, String, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val scope = rememberCoroutineScope()
    var boardTheme by remember { mutableStateOf(initialBoardTheme) }
    var pieceSet by remember { mutableStateOf(initialPieceSet) }
    var playMode by remember(launchConfig) { mutableStateOf(launchConfig.playMode) }
    var difficulty by remember(launchConfig) { mutableStateOf(launchConfig.aiDifficulty) }
    var showStyleSheet by remember { mutableStateOf(false) }
    var showModeSheet by remember { mutableStateOf(false) }
    var flipBoard by remember { mutableStateOf(false) }

    val puzzles = remember { ChessEngine.getPuzzles() }
    var activePuzzleIndex by remember { mutableIntStateOf(0) }

    var boardState by remember { mutableStateOf(ChessEngine.createInitialBoard()) }
    val stateStack = remember { mutableStateListOf<ChessBoardState>() }

    var selectedSquare by remember { mutableStateOf<ChessSquare?>(null) }
    var legalMovesForSelected by remember { mutableStateOf<List<ChessMove>>(emptyList()) }
    var pendingPromotionMoves by remember { mutableStateOf<List<ChessMove>>(emptyList()) }
    var isAiThinking by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf("White to Move ($playerUsername)") }
    var gameOverTitle by remember { mutableStateOf<String?>(null) }
    var gameOverDetail by remember { mutableStateOf("") }
    var checkmatesByUser by remember { mutableIntStateOf(0) }

    // 60fps Piece Slide Animation State
    var animatingMove by remember { mutableStateOf<ChessMove?>(null) }
    var animatingGridOffset by remember { mutableStateOf<Offset?>(null) }

    // Chess Clocks (10:00 default in seconds)
    var whiteTimeSec by remember { mutableIntStateOf(600) }
    var blackTimeSec by remember { mutableIntStateOf(600) }

    fun resetChessGame(mode: PlayModeType = playMode, puzzleIdx: Int = activePuzzleIndex) {
        stateStack.clear()
        selectedSquare = null
        legalMovesForSelected = emptyList()
        pendingPromotionMoves = emptyList()
        animatingMove = null
        animatingGridOffset = null
        gameOverTitle = null
        isAiThinking = false
        whiteTimeSec = 600
        blackTimeSec = 600
        if (mode == PlayModeType.PUZZLE) {
            val p = puzzles[puzzleIdx % puzzles.size]
            boardState = p.boardState
            statusBanner = "${p.title} • ${p.description}"
        } else {
            boardState = ChessEngine.createInitialBoard()
            statusBanner = "White to Move ($playerUsername)"
        }
    }

    LaunchedEffect(launchConfig) {
        resetChessGame(launchConfig.playMode)
    }

    LaunchedEffect(boardState.turn, gameOverTitle, playMode) {
        if (gameOverTitle != null || playMode == PlayModeType.PUZZLE || playMode == PlayModeType.PRACTICE) return@LaunchedEffect
        while (gameOverTitle == null) {
            delay(1000)
            if (boardState.turn == PieceColor.WHITE) {
                whiteTimeSec = (whiteTimeSec - 1).coerceAtLeast(0)
                if (whiteTimeSec == 0) {
                    gameOverTitle = "⏱️ Time Out! Black Wins"
                    gameOverDetail = "White's chess clock expired."
                }
            } else {
                blackTimeSec = (blackTimeSec - 1).coerceAtLeast(0)
                if (blackTimeSec == 0) {
                    gameOverTitle = "⏱️ Time Out! White Wins"
                    gameOverDetail = "Black's chess clock expired."
                }
            }
        }
    }

    fun evaluatePostMoveStatus(nextState: ChessBoardState) {
        val allLegal = ChessEngine.generateAllLegalMoves(nextState)
        val inCheck = ChessEngine.isInCheck(nextState.grid, nextState.turn)

        if (allLegal.isEmpty()) {
            if (inCheck) {
                val winnerColor = if (nextState.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                val userWon = winnerColor == PieceColor.WHITE
                if (userWon) checkmatesByUser++
                gameOverTitle = "♔ Checkmate! ${if (userWon) playerUsername else "Opponent"} Wins!"
                gameOverDetail = "Delivered in ${nextState.moveHistory.size} half-moves."
                onMatchFinished(
                    userWon,
                    false,
                    launchConfig.modeDisplayName,
                    if (playMode == PlayModeType.VS_COMPUTER) "AI (${difficulty.label})" else "Player 2",
                    "Checkmate in ${nextState.moveHistory.size} moves",
                    checkmatesByUser
                )
            } else {
                gameOverTitle = "🤝 Draw by Stalemate!"
                gameOverDetail = "No legal moves available and King is not in check."
                onMatchFinished(
                    false,
                    true,
                    launchConfig.modeDisplayName,
                    if (playMode == PlayModeType.VS_COMPUTER) "AI (${difficulty.label})" else "Player 2",
                    "Stalemate Draw",
                    0
                )
            }
            return
        }

        val turnLabel = if (nextState.turn == PieceColor.WHITE) "White ($playerUsername)" else {
            if (playMode == PlayModeType.VS_COMPUTER) "Black (AI ${difficulty.label})" else "Black (Player 2)"
        }
        statusBanner = if (inCheck) "⚠️ CHECK! $turnLabel must defend the King!" else "$turnLabel to Move"
        onSaveSession(
            "${launchConfig.modeDisplayName} • ${boardTheme.displayName}",
            "Move ${nextState.moveHistory.size / 2 + 1} • $statusBanner"
        )
    }

    fun commitChessMove(move: ChessMove) {
        scope.launch {
            if (playMode == PlayModeType.PUZZLE) {
                val currentPuzzle = puzzles[activePuzzleIndex % puzzles.size]
                if (move.from == currentPuzzle.expectedFrom && move.to == currentPuzzle.expectedTo) {
                    val next = ChessEngine.applyMove(boardState, move)
                    boardState = next
                    selectedSquare = null
                    legalMovesForSelected = emptyList()
                    soundManager.playVictory()
                    statusBanner = "✅ Brilliant! Solved ${currentPuzzle.title}!"
                } else {
                    soundManager.playSnakeSlide()
                    statusBanner = "❌ Not the best tactical blow — try another move!"
                    selectedSquare = null
                    legalMovesForSelected = emptyList()
                }
                return@launch
            }

            selectedSquare = null
            legalMovesForSelected = emptyList()
            pendingPromotionMoves = emptyList()

            // 60fps smooth piece slide animation from source square to destination square
            animatingMove = move
            val startMs = withFrameMillis { it }
            val durationMs = 170
            while (true) {
                val now = withFrameMillis { it }
                val t = ((now - startMs).toFloat() / durationMs).coerceIn(0f, 1f)
                val curCol = move.from.col + (move.to.col - move.from.col) * t
                val curRow = move.from.row + (move.to.row - move.from.row) * t
                animatingGridOffset = Offset(curCol, curRow)
                if (t >= 1f) break
            }
            animatingMove = null
            animatingGridOffset = null

            stateStack.add(boardState)
            val next = ChessEngine.applyMove(boardState, move)
            boardState = next

            val inCheck = ChessEngine.isInCheck(next.grid, next.turn)
            soundManager.playChessMove(isCapture = move.captured != null || move.isEnPassant, isCheck = inCheck)
            evaluatePostMoveStatus(next)
        }
    }

    LaunchedEffect(boardState, playMode, difficulty, gameOverTitle) {
        if (playMode == PlayModeType.VS_COMPUTER && boardState.turn == PieceColor.BLACK && gameOverTitle == null && !isAiThinking) {
            isAiThinking = true
            delay(360)
            val bestMove = withContext(Dispatchers.Default) {
                ChessEngine.findBestAiMove(boardState, difficulty)
            }
            isAiThinking = false
            if (bestMove != null) {
                commitChessMove(bestMove)
            }
        }
    }

    fun formatClock(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("chess_game_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Navigation & Actions Bar
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
                        .testTag("chess_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${boardTheme.displayName.uppercase()} • ${pieceSet.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight
                    )
                    Text(
                        text = "Grandmaster Chess 3D",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { flipBoard = !flipBoard },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("chess_flip_button")
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Board", tint = Color.White)
                }
                IconButton(
                    onClick = { showStyleSheet = !showStyleSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("chess_theme_button")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Themes & Sets", tint = GoldLight)
                }
                IconButton(
                    onClick = { showModeSheet = !showModeSheet },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("chess_mode_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Mode & AI", tint = CyanGlow)
                }
                IconButton(
                    onClick = { resetChessGame() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("chess_restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                }
            }
        }

        if (showStyleSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = GoldPrimary) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chessboard Material (7 Themes)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChessBoardTheme.entries.forEach { t ->
                            FilterChip(
                                selected = boardTheme == t,
                                onClick = {
                                    boardTheme = t
                                    onSelectThemes(t, pieceSet)
                                },
                                label = { Text(t.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                    Text("3D Staunton Piece Set (7 Styles)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChessPieceSet.entries.forEach { ps ->
                            FilterChip(
                                selected = pieceSet == ps,
                                onClick = {
                                    pieceSet = ps
                                    onSelectThemes(boardTheme, ps)
                                },
                                label = { Text(ps.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        if (showModeSheet) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, accentGlow = CyanGlow) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Switch Chess Mode", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            PlayModeType.LOCAL_ROOM,
                            PlayModeType.VS_COMPUTER,
                            PlayModeType.PRACTICE,
                            PlayModeType.PUZZLE
                        ).forEach { m ->
                            FilterChip(
                                selected = playMode == m,
                                onClick = {
                                    playMode = m
                                    resetChessGame(mode = m)
                                },
                                label = { Text(m.title) }
                            )
                        }
                    }
                    if (playMode == PlayModeType.VS_COMPUTER) {
                        Text("Engine AI Difficulty (5 Tiers)", style = MaterialTheme.typography.titleMedium, color = Color.White)
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

        // Clocks & Captured Pieces Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val whiteTurn = boardState.turn == PieceColor.WHITE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (whiteTurn) GoldPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                    .border(
                        width = if (whiteTurn) 2.dp else 1.dp,
                        color = if (whiteTurn) GoldLight else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("♔ $playerUsername", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(formatClock(whiteTimeSec), style = MaterialTheme.typography.labelSmall, color = GoldLight)
                    }
                    val caps = boardState.capturedByWhite.joinToString("") { it.blackSymbol }
                    Text(
                        text = if (caps.isEmpty()) "Captured: —" else "Captured: $caps",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (!whiteTurn) CyanGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                    .border(
                        width = if (!whiteTurn) 2.dp else 1.dp,
                        color = if (!whiteTurn) CyanGlow else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val oppName = if (playMode == PlayModeType.VS_COMPUTER) "♚ AI (${difficulty.label})" else "♚ Player 2"
                        Text(oppName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(formatClock(blackTimeSec), style = MaterialTheme.typography.labelSmall, color = CyanGlow)
                    }
                    val caps = boardState.capturedByBlack.joinToString("") { it.whiteSymbol }
                    Text(
                        text = if (caps.isEmpty()) "Captured: —" else "Captured: $caps",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // THE 8x8 GRANDMASTER CHESSBOARD WITH SCULPTED 3D STAUNTON PIECES & SMOOTH SLIDE ANIMATION
        val canHumanTap = !(playMode == PlayModeType.VS_COMPUTER && boardState.turn == PieceColor.BLACK) &&
            !isAiThinking &&
            animatingMove == null &&
            gameOverTitle == null

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(boardTheme.borderColor, Color(0xFF140B05), boardTheme.borderColor)
                    )
                )
                .border(3.5.dp, GoldPrimary.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                .padding(10.dp)
                .testTag("chess_board_canvas")
        ) {
            val coordPaint = remember {
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(boardState, selectedSquare, legalMovesForSelected, flipBoard, canHumanTap) {
                        detectTapGestures { tap ->
                            if (!canHumanTap) return@detectTapGestures
                            val sqW = size.width / 8f
                            val sqH = size.height / 8f
                            val visualCol = (tap.x / sqW).toInt().coerceIn(0, 7)
                            val visualRow = (tap.y / sqH).toInt().coerceIn(0, 7)
                            val boardRow = if (flipBoard) 7 - visualRow else visualRow
                            val boardCol = if (flipBoard) 7 - visualCol else visualCol
                            val tappedSq = ChessSquare(boardRow, boardCol)

                            val matchingMoves = legalMovesForSelected.filter { it.to == tappedSq }
                            if (matchingMoves.isNotEmpty()) {
                                if (matchingMoves.size > 1 && matchingMoves.first().promotion != null) {
                                    pendingPromotionMoves = matchingMoves
                                } else {
                                    commitChessMove(matchingMoves.first())
                                }
                                return@detectTapGestures
                            }

                            val piece = boardState.grid[boardRow][boardCol]
                            if (piece != null && piece.color == boardState.turn) {
                                soundManager.playClick()
                                selectedSquare = tappedSq
                                legalMovesForSelected = ChessEngine.generateLegalMovesForSquare(boardState, tappedSq)
                            } else {
                                selectedSquare = null
                                legalMovesForSelected = emptyList()
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val sqW = w / 8f
                val sqH = h / 8f

                // 1. Draw 64 Inlaid Squares with Wood/Marble Shading & Highlights
                for (vr in 0..7) {
                    for (vc in 0..7) {
                        val r = if (flipBoard) 7 - vr else vr
                        val c = if (flipBoard) 7 - vc else vc
                        val sq = ChessSquare(r, c)
                        val isLight = (r + c) % 2 == 0
                        val baseColor = if (isLight) boardTheme.lightSquare else boardTheme.darkSquare

                        val topLeft = Offset(vc * sqW, vr * sqH)
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(baseColor, baseColor.copy(alpha = 0.90f)),
                                start = topLeft,
                                end = topLeft + Offset(sqW, sqH)
                            ),
                            topLeft = topLeft,
                            size = Size(sqW, sqH)
                        )

                        val last = boardState.lastMove
                        if (last != null && (last.from == sq || last.to == sq)) {
                            drawRect(
                                color = Color(0xFFFACC15).copy(alpha = 0.36f),
                                topLeft = topLeft,
                                size = Size(sqW, sqH)
                            )
                        }

                        if (selectedSquare == sq) {
                            drawRect(
                                color = Color(0xFF38BDF8).copy(alpha = 0.46f),
                                topLeft = topLeft,
                                size = Size(sqW, sqH)
                            )
                        }

                        val occupant = boardState.grid[r][c]
                        if (occupant != null && occupant.kind == PieceKind.KING &&
                            ChessEngine.isInCheck(boardState.grid, occupant.color)
                        ) {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(RubyDanger.copy(alpha = 0.85f), RubyDanger.copy(alpha = 0.25f)),
                                    center = topLeft + Offset(sqW / 2f, sqH / 2f),
                                    radius = sqW * 0.7f
                                ),
                                topLeft = topLeft,
                                size = Size(sqW, sqH)
                            )
                        }

                        if (vc == 0) {
                            coordPaint.color = if (isLight) android.graphics.Color.parseColor("#334155") else android.graphics.Color.parseColor("#F8FAFC")
                            coordPaint.textSize = sqW * 0.21f
                            drawContext.canvas.nativeCanvas.drawText(
                                "${8 - r}",
                                topLeft.x + 3.dp.toPx(),
                                topLeft.y + sqH * 0.23f,
                                coordPaint
                            )
                        }
                        if (vr == 7) {
                            coordPaint.color = if (isLight) android.graphics.Color.parseColor("#334155") else android.graphics.Color.parseColor("#F8FAFC")
                            coordPaint.textSize = sqW * 0.21f
                            drawContext.canvas.nativeCanvas.drawText(
                                "${('a' + c)}",
                                topLeft.x + sqW * 0.80f,
                                topLeft.y + sqH * 0.93f,
                                coordPaint
                            )
                        }

                        // Draw stationary pieces (skip the piece currently sliding in animation)
                        val isAnimatingSource = animatingMove?.from == sq
                        if (occupant != null && !isAnimatingSource) {
                            val center = Offset(topLeft.x + sqW / 2f, topLeft.y + sqH / 2f)
                            drawRealisticChessPieceOnScope(
                                kind = occupant.kind,
                                color = occupant.color,
                                pieceSet = pieceSet,
                                center = center,
                                cellSize = sqW
                            )
                        }

                        // Legal move indicator dots & capture rings
                        val isLegalDest = legalMovesForSelected.any { it.to == sq }
                        if (isLegalDest) {
                            val center = Offset(topLeft.x + sqW / 2f, topLeft.y + sqH / 2f)
                            if (occupant != null) {
                                drawCircle(
                                    color = EmeraldSuccess,
                                    radius = sqW * 0.44f,
                                    center = center,
                                    style = Stroke(width = 3.5.dp.toPx())
                                )
                            } else {
                                drawCircle(
                                    color = EmeraldSuccess.copy(alpha = 0.85f),
                                    radius = sqW * 0.16f,
                                    center = center
                                )
                            }
                        }
                    }
                }

                // 2. Draw Moving Piece at 60fps Interpolated Coordinates
                val activeAnim = animatingMove
                val activeOffset = animatingGridOffset
                if (activeAnim != null && activeOffset != null) {
                    val visCol = if (flipBoard) 7f - activeOffset.x else activeOffset.x
                    val visRow = if (flipBoard) 7f - activeOffset.y else activeOffset.y
                    val center = Offset((visCol + 0.5f) * sqW, (visRow + 0.5f) * sqH)
                    drawRealisticChessPieceOnScope(
                        kind = activeAnim.promotion ?: activeAnim.piece.kind,
                        color = activeAnim.piece.color,
                        pieceSet = pieceSet,
                        center = center,
                        cellSize = sqW * 1.12f
                    )
                }
            }
        }

        // Bottom Tactical Controls, Move History & Hint/Undo Bar
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            accentGlow = Color(0xFF8B5CF6)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = statusBanner,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                val recentNotation = if (boardState.moveHistory.isEmpty()) {
                    "Move History: Tap any White piece to view legal moves"
                } else {
                    "Moves: " + boardState.moveHistory.takeLast(8).joinToString("  ")
                }
                Text(
                    text = recentNotation,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (stateStack.isNotEmpty() && !isAiThinking) {
                                soundManager.playClick()
                                if (playMode == PlayModeType.VS_COMPUTER && stateStack.size >= 2) {
                                    stateStack.removeAt(stateStack.lastIndex)
                                    boardState = stateStack.removeAt(stateStack.lastIndex)
                                } else {
                                    boardState = stateStack.removeAt(stateStack.lastIndex)
                                }
                                selectedSquare = null
                                legalMovesForSelected = emptyList()
                            }
                        },
                        enabled = stateStack.isNotEmpty() && !isAiThinking,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chess_undo_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (canHumanTap) {
                                scope.launch {
                                    val hint = withContext(Dispatchers.Default) {
                                        ChessEngine.findBestAiMove(boardState, AiDifficulty.HARD)
                                    }
                                    if (hint != null) {
                                        selectedSquare = hint.from
                                        legalMovesForSelected = listOf(hint)
                                        statusBanner = "💡 Hint: Move ${hint.piece.kind.name} ${hint.from.toAlgebraic()} → ${hint.to.toAlgebraic()}"
                                    }
                                }
                            }
                        },
                        enabled = canHumanTap,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chess_hint_button")
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Best Hint", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (playMode == PlayModeType.PUZZLE) {
                        Button(
                            onClick = {
                                activePuzzleIndex = (activePuzzleIndex + 1) % puzzles.size
                                resetChessGame(mode = PlayModeType.PUZZLE, puzzleIdx = activePuzzleIndex)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next Puzzle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (pendingPromotionMoves.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { pendingPromotionMoves = emptyList() },
            containerColor = Color(0xFF111C44),
            title = { Text("Promote Pawn To:", color = GoldLight, style = MaterialTheme.typography.titleLarge) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pendingPromotionMoves.forEach { promoMove ->
                        val pk = promoMove.promotion ?: PieceKind.QUEEN
                        Button(
                            onClick = { commitChessMove(promoMove) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                        ) {
                            Text("${pk.whiteSymbol} ${pk.sanChar}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    gameOverTitle?.let { title ->
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF111C44),
            title = { Text(title, color = GoldLight, style = MaterialTheme.typography.headlineMedium) },
            text = { Text(gameOverDetail, color = Color.White) },
            confirmButton = {
                Button(
                    onClick = { resetChessGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.Black)
                ) {
                    Text("New Match", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = onBack) { Text("Home Hub") }
            }
        )
    }
}
