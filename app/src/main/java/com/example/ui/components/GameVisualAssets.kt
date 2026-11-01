package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ChessPieceSet
import com.example.data.GameType
import com.example.games.PieceColor
import com.example.games.PieceKind
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3D artwork canvas for the 4 main Playverse Game Cards on the Home, Games, and Lobby screens.
 */
@Composable
fun GameCard3DArtwork(
    gameType: GameType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_art_${gameType.id}")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "art_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val floatY = sin(animPhase) * 4.dp.toPx()

        when (gameType) {
            GameType.SNAKES -> {
                // 3D Isometric Bevelled Board Tiles
                val tileColors = listOf(Color(0xFFFEF3C7), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF38BDF8))
                for (r in 0..2) {
                    for (c in 0..2) {
                        val tx = w * 0.12f + c * (w * 0.22f)
                        val ty = h * 0.24f + r * (h * 0.21f)
                        val color = tileColors[(r + c) % tileColors.size]
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.45f),
                            topLeft = Offset(tx, ty + 4.dp.toPx()),
                            size = Size(w * 0.19f, h * 0.17f),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(color, color.copy(alpha = 0.8f)),
                                startY = ty,
                                endY = ty + h * 0.17f
                            ),
                            topLeft = Offset(tx, ty),
                            size = Size(w * 0.19f, h * 0.17f),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                }
                // 3D Cylindrical Wooden Ladder
                val lx1 = w * 0.20f
                val ly1 = h * 0.80f
                val lx2 = w * 0.52f
                val ly2 = h * 0.18f
                drawLine(Color.Black.copy(alpha = 0.4f), Offset(lx1 + 2f, ly1 + 4f), Offset(lx2 + 2f, ly2 + 4f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFFB45309), Offset(lx1, ly1), Offset(lx2, ly2), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFFFBBF24), Offset(lx1 + 14.dp.toPx(), ly1 + 4.dp.toPx()), Offset(lx2 + 14.dp.toPx(), ly2 + 4.dp.toPx()), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                for (step in 1..4) {
                    val t = step / 5f
                    val sx = lx1 + (lx2 - lx1) * t
                    val sy = ly1 + (ly2 - ly1) * t
                    drawLine(
                        Color(0xFFFEF08A),
                        Offset(sx, sy),
                        Offset(sx + 14.dp.toPx(), sy + 4.dp.toPx()),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                // Animated Slithering 3D Snake with Scales
                val snakePath = Path().apply {
                    moveTo(w * 0.78f, h * 0.26f + floatY)
                    cubicTo(
                        w * 0.96f, h * 0.45f,
                        w * 0.54f, h * 0.62f,
                        w * 0.80f, h * 0.84f
                    )
                }
                drawPath(
                    path = snakePath,
                    color = Color.Black.copy(alpha = 0.45f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = snakePath,
                    color = Color(0xFF10B981),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = snakePath,
                    color = Color(0xFFA7F3D0),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
                val headCenter = Offset(w * 0.78f, h * 0.26f + floatY)
                drawCircle(Color(0xFF059669), radius = 10.dp.toPx(), center = headCenter)
                drawCircle(Color.White, radius = 3.2.dp.toPx(), center = headCenter + Offset(-3.dp.toPx(), -2.dp.toPx()))
                drawCircle(Color.White, radius = 3.2.dp.toPx(), center = headCenter + Offset(3.dp.toPx(), -2.dp.toPx()))
                drawCircle(Color.Black, radius = 1.6.dp.toPx(), center = headCenter + Offset(-3.dp.toPx(), -2.dp.toPx()))
                drawCircle(Color.Black, radius = 1.6.dp.toPx(), center = headCenter + Offset(3.dp.toPx(), -2.dp.toPx()))
            }

            GameType.LUDO -> {
                val pad = w * 0.12f
                val boxSize = minOf(w, h) * 0.31f
                val quads = listOf(
                    Offset(pad, h * 0.16f) to Color(0xFFEF4444),
                    Offset(pad + boxSize * 1.15f, h * 0.16f) to Color(0xFF10B981),
                    Offset(pad, h * 0.16f + boxSize * 1.15f) to Color(0xFFF59E0B),
                    Offset(pad + boxSize * 1.15f, h * 0.16f + boxSize * 1.15f) to Color(0xFF3B82F6)
                )
                quads.forEach { (topLeft, col) ->
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.4f),
                        topLeft = topLeft + Offset(2.dp.toPx(), 4.dp.toPx()),
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        color = col,
                        topLeft = topLeft,
                        size = Size(boxSize, boxSize),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = topLeft + Offset(boxSize * 0.2f, boxSize * 0.2f),
                        size = Size(boxSize * 0.6f, boxSize * 0.6f),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                }
                val diceTopLeft = Offset(w * 0.65f, h * 0.22f + floatY)
                val dSize = minOf(w, h) * 0.29f
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = diceTopLeft + Offset(3.dp.toPx(), 6.dp.toPx()),
                    size = Size(dSize, dSize),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFCBD5E1)),
                        start = diceTopLeft,
                        end = diceTopLeft + Offset(dSize, dSize)
                    ),
                    topLeft = diceTopLeft,
                    size = Size(dSize, dSize),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
                val pipColor = Color(0xFFEF4444)
                listOf(
                    Offset(0.28f, 0.28f), Offset(0.5f, 0.5f), Offset(0.72f, 0.72f),
                    Offset(0.72f, 0.28f), Offset(0.28f, 0.72f)
                ).forEach { (px, py) ->
                    drawCircle(pipColor, radius = 2.8.dp.toPx(), center = diceTopLeft + Offset(dSize * px, dSize * py))
                }
                drawPawnSilhouetteOnScope(
                    skinId = "crown",
                    center = Offset(w * 0.76f, h * 0.74f - floatY * 0.6f),
                    radius = 15.dp.toPx(),
                    baseColor = Color(0xFFF59E0B)
                )
            }

            GameType.CARROM -> {
                val bSize = minOf(w, h) * 0.76f
                val topLeft = Offset((w - bSize) / 2f, (h - bSize) / 2f)
                // Rosewood 3D frame
                drawRoundRect(
                    color = Color(0xFF2B1206),
                    topLeft = topLeft,
                    size = Size(bSize, bSize),
                    cornerRadius = CornerRadius(14.dp.toPx())
                )
                val innerPad = bSize * 0.11f
                val playSize = bSize - innerPad * 2
                val playTopLeft = topLeft + Offset(innerPad, innerPad)
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF9E4B7), Color(0xFFE8C589)),
                        center = Offset(w / 2f, h / 2f),
                        radius = playSize * 0.75f
                    ),
                    topLeft = playTopLeft,
                    size = Size(playSize, playSize),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                val pRad = bSize * 0.068f
                listOf(
                    playTopLeft + Offset(pRad, pRad),
                    playTopLeft + Offset(playSize - pRad, pRad),
                    playTopLeft + Offset(pRad, playSize - pRad),
                    playTopLeft + Offset(playSize - pRad, playSize - pRad)
                ).forEach { pCenter ->
                    drawCircle(Color(0xFF140904), radius = pRad, center = pCenter)
                }
                val center = Offset(w / 2f, h / 2f)
                drawCircle(
                    color = Color(0xFF8B0000),
                    radius = playSize * 0.22f,
                    center = center,
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawCircle(Color(0xFFE11D48), radius = 6.dp.toPx(), center = center)
                for (i in 0 until 6) {
                    val ang = i * 1.047f + animPhase * 0.2f
                    val cPos = center + Offset(cos(ang) * 14.dp.toPx(), sin(ang) * 14.dp.toPx())
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFFF8E7) else Color(0xFF1E293B),
                        radius = 5.dp.toPx(),
                        center = cPos
                    )
                }
                val strikerPos = Offset(center.x + floatY * 1.5f, playTopLeft.y + playSize * 0.82f)
                drawLine(
                    color = Color(0xFF10B981).copy(alpha = 0.85f),
                    start = strikerPos,
                    end = center,
                    strokeWidth = 2.2.dp.toPx()
                )
                drawCircle(Color(0xFF38BDF8), radius = 8.5.dp.toPx(), center = strikerPos)
                drawCircle(Color.White, radius = 5.dp.toPx(), center = strikerPos)
            }

            GameType.CHESS -> {
                val bSize = minOf(w, h) * 0.76f
                val topLeft = Offset((w - bSize) / 2f, (h - bSize) / 2f)
                drawRoundRect(
                    color = Color(0xFF2B170A),
                    topLeft = topLeft,
                    size = Size(bSize, bSize),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                val pad = bSize * 0.08f
                val grid = (bSize - pad * 2) / 4f
                for (r in 0..3) {
                    for (c in 0..3) {
                        val sqColor = if ((r + c) % 2 == 0) Color(0xFFF3D9B1) else Color(0xFF7C4D2B)
                        drawRect(
                            color = sqColor,
                            topLeft = topLeft + Offset(pad + c * grid, pad + r * grid),
                            size = Size(grid, grid)
                        )
                    }
                }
                // Realistic 3D Staunton King & Knight on card
                drawRealisticChessPieceOnScope(
                    kind = PieceKind.KING,
                    color = PieceColor.WHITE,
                    pieceSet = ChessPieceSet.ROYAL_GOLD,
                    center = Offset(w * 0.38f, h * 0.48f + floatY * 0.5f),
                    cellSize = bSize * 0.42f
                )
                drawRealisticChessPieceOnScope(
                    kind = PieceKind.KNIGHT,
                    color = PieceColor.BLACK,
                    pieceSet = ChessPieceSet.STAUNTON,
                    center = Offset(w * 0.64f, h * 0.58f - floatY * 0.5f),
                    cellSize = bSize * 0.38f
                )
            }
        }
    }
}

/**
 * Draws a sculpted, 3D-shaded Staunton Chess Piece directly on a Compose Canvas DrawScope.
 * Replaces flat text symbols with realistic layered pedestals, bodies, collars, crowns, and specular highlights.
 */
fun DrawScope.drawRealisticChessPieceOnScope(
    kind: PieceKind,
    color: PieceColor,
    pieceSet: ChessPieceSet,
    center: Offset,
    cellSize: Float
) {
    val r = cellSize * 0.42f
    val primaryFill = if (color == PieceColor.WHITE) pieceSet.whiteFill else pieceSet.blackFill
    val rimStroke = if (color == PieceColor.WHITE) pieceSet.whiteStroke else pieceSet.blackStroke
    val highlightColor = if (color == PieceColor.WHITE) Color.White else Color(0xFF64748B)
    val shadowShade = if (color == PieceColor.WHITE) {
        Color(
            red = (primaryFill.red * 0.76f).coerceIn(0f, 1f),
            green = (primaryFill.green * 0.74f).coerceIn(0f, 1f),
            blue = (primaryFill.blue * 0.70f).coerceIn(0f, 1f)
        )
    } else {
        Color(0xFF090D16)
    }

    // 1. Cast Drop Shadow on Board Square
    drawOval(
        color = Color.Black.copy(alpha = 0.46f),
        topLeft = Offset(center.x - r * 0.85f, center.y + r * 0.42f),
        size = Size(r * 1.75f, r * 0.58f)
    )

    // 2. 3D Turned Base Pedestal
    val baseBrush = Brush.horizontalGradient(
        colors = listOf(shadowShade, highlightColor, primaryFill, shadowShade),
        startX = center.x - r * 0.8f,
        endX = center.x + r * 0.8f
    )
    drawRoundRect(
        brush = baseBrush,
        topLeft = Offset(center.x - r * 0.76f, center.y + r * 0.48f),
        size = Size(r * 1.52f, r * 0.28f),
        cornerRadius = CornerRadius(r * 0.14f)
    )
    drawRoundRect(
        color = rimStroke,
        topLeft = Offset(center.x - r * 0.76f, center.y + r * 0.48f),
        size = Size(r * 1.52f, r * 0.28f),
        cornerRadius = CornerRadius(r * 0.14f),
        style = Stroke(width = (r * 0.07f).coerceAtLeast(1.2f))
    )

    // Second pedestal tier
    drawRoundRect(
        brush = baseBrush,
        topLeft = Offset(center.x - r * 0.60f, center.y + r * 0.32f),
        size = Size(r * 1.20f, r * 0.20f),
        cornerRadius = CornerRadius(r * 0.10f)
    )

    val bodyBrush = Brush.linearGradient(
        colors = listOf(highlightColor, primaryFill, shadowShade),
        start = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
        end = Offset(center.x + r * 0.6f, center.y + r * 0.5f)
    )

    // 3. Piece-Specific Sculpted 3D Silhouette
    when (kind) {
        PieceKind.PAWN -> {
            // Tapered column
            val bodyPath = Path().apply {
                moveTo(center.x - r * 0.46f, center.y + r * 0.34f)
                quadraticTo(center.x - r * 0.22f, center.y - r * 0.05f, center.x - r * 0.26f, center.y - r * 0.22f)
                lineTo(center.x + r * 0.26f, center.y - r * 0.22f)
                quadraticTo(center.x + r * 0.22f, center.y - r * 0.05f, center.x + r * 0.46f, center.y + r * 0.34f)
                close()
            }
            drawPath(bodyPath, brush = bodyBrush)
            drawPath(bodyPath, color = rimStroke, style = Stroke(width = r * 0.07f))
            // Collar ring
            drawRoundRect(
                brush = baseBrush,
                topLeft = Offset(center.x - r * 0.34f, center.y - r * 0.28f),
                size = Size(r * 0.68f, r * 0.12f),
                cornerRadius = CornerRadius(r * 0.06f)
            )
            // Spherical finial head with 3D highlight
            val headCenter = Offset(center.x, center.y - r * 0.54f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(highlightColor, primaryFill, shadowShade),
                    center = headCenter - Offset(r * 0.12f, r * 0.12f),
                    radius = r * 0.42f
                ),
                radius = r * 0.32f,
                center = headCenter
            )
            drawCircle(
                color = rimStroke,
                radius = r * 0.32f,
                center = headCenter,
                style = Stroke(width = r * 0.07f)
            )
        }

        PieceKind.ROOK -> {
            // Castle tower column + crenellated battlements
            val towerPath = Path().apply {
                moveTo(center.x - r * 0.52f, center.y + r * 0.34f)
                lineTo(center.x - r * 0.40f, center.y - r * 0.35f)
                lineTo(center.x - r * 0.52f, center.y - r * 0.45f)
                lineTo(center.x - r * 0.52f, center.y - r * 0.72f)
                // 3 Battlement merlons
                lineTo(center.x - r * 0.28f, center.y - r * 0.72f)
                lineTo(center.x - r * 0.28f, center.y - r * 0.56f)
                lineTo(center.x - r * 0.12f, center.y - r * 0.56f)
                lineTo(center.x - r * 0.12f, center.y - r * 0.72f)
                lineTo(center.x + r * 0.12f, center.y - r * 0.72f)
                lineTo(center.x + r * 0.12f, center.y - r * 0.56f)
                lineTo(center.x + r * 0.28f, center.y - r * 0.56f)
                lineTo(center.x + r * 0.28f, center.y - r * 0.72f)
                lineTo(center.x + r * 0.52f, center.y - r * 0.72f)
                lineTo(center.x + r * 0.52f, center.y - r * 0.45f)
                lineTo(center.x + r * 0.40f, center.y - r * 0.35f)
                lineTo(center.x + r * 0.52f, center.y + r * 0.34f)
                close()
            }
            drawPath(towerPath, brush = bodyBrush)
            drawPath(towerPath, color = rimStroke, style = Stroke(width = r * 0.07f, join = StrokeJoin.Round))
        }

        PieceKind.KNIGHT -> {
            // Sculpted Staunton Warhorse Head & Mane
            val horsePath = Path().apply {
                moveTo(center.x - r * 0.48f, center.y + r * 0.34f)
                quadraticTo(center.x - r * 0.45f, center.y - r * 0.10f, center.x - r * 0.18f, center.y - r * 0.28f)
                // Muzzle & jaw
                lineTo(center.x - r * 0.56f, center.y - r * 0.20f)
                lineTo(center.x - r * 0.62f, center.y - r * 0.38f)
                lineTo(center.x - r * 0.22f, center.y - r * 0.65f)
                // Pointed ear
                lineTo(center.x - r * 0.12f, center.y - r * 0.88f)
                lineTo(center.x + r * 0.10f, center.y - r * 0.74f)
                // Flowing arched mane
                quadraticTo(center.x + r * 0.58f, center.y - r * 0.52f, center.x + r * 0.50f, center.y + r * 0.34f)
                close()
            }
            drawPath(horsePath, brush = bodyBrush)
            drawPath(horsePath, color = rimStroke, style = Stroke(width = r * 0.075f, join = StrokeJoin.Round))
            // Eye detail
            drawCircle(
                color = rimStroke,
                radius = r * 0.07f,
                center = Offset(center.x - r * 0.20f, center.y - r * 0.48f)
            )
        }

        PieceKind.BISHOP -> {
            // Tapered body + Mitre dome + Finial orb + Diagonal slash
            val mitrePath = Path().apply {
                moveTo(center.x - r * 0.46f, center.y + r * 0.34f)
                quadraticTo(center.x - r * 0.26f, center.y, center.x - r * 0.40f, center.y - r * 0.28f)
                quadraticTo(center.x - r * 0.42f, center.y - r * 0.68f, center.x, center.y - r * 0.80f)
                quadraticTo(center.x + r * 0.42f, center.y - r * 0.68f, center.x + r * 0.40f, center.y - r * 0.28f)
                quadraticTo(center.x + r * 0.26f, center.y, center.x + r * 0.46f, center.y + r * 0.34f)
                close()
            }
            drawPath(mitrePath, brush = bodyBrush)
            drawPath(mitrePath, color = rimStroke, style = Stroke(width = r * 0.07f))
            // Top pearl finial
            drawCircle(
                color = rimStroke,
                radius = r * 0.10f,
                center = Offset(center.x, center.y - r * 0.88f)
            )
            // Mitre cross slot
            drawLine(
                color = rimStroke,
                start = Offset(center.x - r * 0.14f, center.y - r * 0.48f),
                end = Offset(center.x + r * 0.14f, center.y - r * 0.48f),
                strokeWidth = r * 0.07f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = rimStroke,
                start = Offset(center.x, center.y - r * 0.62f),
                end = Offset(center.x, center.y - r * 0.34f),
                strokeWidth = r * 0.07f,
                cap = StrokeCap.Round
            )
        }

        PieceKind.QUEEN -> {
            // Multi-spiked Imperial Coronet with pearl tips
            val queenPath = Path().apply {
                moveTo(center.x - r * 0.50f, center.y + r * 0.34f)
                lineTo(center.x - r * 0.36f, center.y - r * 0.15f)
                lineTo(center.x - r * 0.62f, center.y - r * 0.68f)
                lineTo(center.x - r * 0.26f, center.y - r * 0.32f)
                lineTo(center.x, center.y - r * 0.82f)
                lineTo(center.x + r * 0.26f, center.y - r * 0.32f)
                lineTo(center.x + r * 0.62f, center.y - r * 0.68f)
                lineTo(center.x + r * 0.36f, center.y - r * 0.15f)
                lineTo(center.x + r * 0.50f, center.y + r * 0.34f)
                close()
            }
            drawPath(queenPath, brush = bodyBrush)
            drawPath(queenPath, color = rimStroke, style = Stroke(width = r * 0.07f, join = StrokeJoin.Round))
            // 3 Coronet Pearls
            listOf(
                Offset(center.x - r * 0.62f, center.y - r * 0.74f),
                Offset(center.x, center.y - r * 0.88f),
                Offset(center.x + r * 0.62f, center.y - r * 0.74f)
            ).forEach { pearlPos ->
                drawCircle(color = Color(0xFFFBBF24), radius = r * 0.09f, center = pearlPos)
                drawCircle(color = rimStroke, radius = r * 0.09f, center = pearlPos, style = Stroke(width = r * 0.04f))
            }
        }

        PieceKind.KING -> {
            // Fluted Monarch Crown + Imperial Cross Finial
            val kingPath = Path().apply {
                moveTo(center.x - r * 0.52f, center.y + r * 0.34f)
                lineTo(center.x - r * 0.38f, center.y - r * 0.18f)
                lineTo(center.x - r * 0.54f, center.y - r * 0.54f)
                quadraticTo(center.x, center.y - r * 0.72f, center.x + r * 0.54f, center.y - r * 0.54f)
                lineTo(center.x + r * 0.38f, center.y - r * 0.18f)
                lineTo(center.x + r * 0.52f, center.y + r * 0.34f)
                close()
            }
            drawPath(kingPath, brush = bodyBrush)
            drawPath(kingPath, color = rimStroke, style = Stroke(width = r * 0.075f, join = StrokeJoin.Round))
            // Gold band collar
            drawRoundRect(
                color = Color(0xFFF59E0B),
                topLeft = Offset(center.x - r * 0.42f, center.y - r * 0.22f),
                size = Size(r * 0.84f, r * 0.12f),
                cornerRadius = CornerRadius(r * 0.05f)
            )
            // Royal Cross on summit
            val crossColor = Color(0xFFFBBF24)
            drawLine(
                color = crossColor,
                start = Offset(center.x, center.y - r * 0.98f),
                end = Offset(center.x, center.y - r * 0.60f),
                strokeWidth = r * 0.13f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = crossColor,
                start = Offset(center.x - r * 0.18f, center.y - r * 0.82f),
                end = Offset(center.x + r * 0.18f, center.y - r * 0.82f),
                strokeWidth = r * 0.13f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws any of the 13 custom Ludo Pawn skins inside a DrawScope with 3D pedestal shading.
 */
fun DrawScope.drawPawnSilhouetteOnScope(
    skinId: String,
    center: Offset,
    radius: Float,
    baseColor: Color,
    isHighlighted: Boolean = false
) {
    // Ground shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.46f),
        topLeft = Offset(center.x - radius * 0.95f, center.y + radius * 0.35f),
        size = Size(radius * 1.9f, radius * 0.7f)
    )

    if (isHighlighted) {
        drawCircle(
            color = Color(0xFFFEF08A).copy(alpha = 0.90f),
            radius = radius * 1.36f,
            center = center,
            style = Stroke(width = radius * 0.20f)
        )
    }

    // Outer 3D glossy dome
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                baseColor,
                baseColor.copy(red = baseColor.red * 0.52f, green = baseColor.green * 0.52f, blue = baseColor.blue * 0.52f)
            ),
            center = center - Offset(radius * 0.32f, radius * 0.32f),
            radius = radius * 1.35f
        ),
        radius = radius,
        center = center
    )

    // Metallic bevel rim
    drawCircle(
        color = Color.White.copy(alpha = 0.88f),
        radius = radius,
        center = center,
        style = Stroke(width = (radius * 0.15f).coerceAtLeast(1.6f))
    )

    val r = radius * 0.58f
    when (skinId) {
        "crown" -> {
            val path = Path().apply {
                moveTo(center.x - r, center.y + r * 0.55f)
                lineTo(center.x - r * 0.85f, center.y - r * 0.45f)
                lineTo(center.x - r * 0.35f, center.y + r * 0.05f)
                lineTo(center.x, center.y - r * 0.75f)
                lineTo(center.x + r * 0.35f, center.y + r * 0.05f)
                lineTo(center.x + r * 0.85f, center.y - r * 0.45f)
                lineTo(center.x + r, center.y + r * 0.55f)
                close()
            }
            drawPath(path, Color(0xFFFEF08A))
        }
        "knight" -> {
            val path = Path().apply {
                moveTo(center.x - r * 0.6f, center.y + r * 0.7f)
                lineTo(center.x - r * 0.5f, center.y - r * 0.3f)
                lineTo(center.x - r * 0.8f, center.y - r * 0.4f)
                lineTo(center.x + r * 0.1f, center.y - r * 0.85f)
                lineTo(center.x + r * 0.7f, center.y - r * 0.2f)
                lineTo(center.x + r * 0.5f, center.y + r * 0.7f)
                close()
            }
            drawPath(path, Color.White)
        }
        "robot" -> {
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = center - Offset(r * 0.7f, r * 0.65f),
                size = Size(r * 1.4f, r * 1.3f),
                cornerRadius = CornerRadius(r * 0.3f)
            )
            drawRect(
                color = Color(0xFF00F5D4),
                topLeft = center - Offset(r * 0.5f, r * 0.25f),
                size = Size(r * 1.0f, r * 0.35f)
            )
        }
        "dragon" -> {
            val path = Path().apply {
                moveTo(center.x, center.y - r * 0.85f)
                lineTo(center.x + r * 0.85f, center.y - r * 0.2f)
                lineTo(center.x + r * 0.4f, center.y + r * 0.75f)
                lineTo(center.x - r * 0.4f, center.y + r * 0.75f)
                lineTo(center.x - r * 0.85f, center.y - r * 0.2f)
                close()
            }
            drawPath(path, Color(0xFFFDE047))
            drawCircle(Color(0xFFDC2626), radius = r * 0.25f, center = center)
        }
        "crystal" -> {
            val path = Path().apply {
                moveTo(center.x, center.y - r * 0.9f)
                lineTo(center.x + r * 0.65f, center.y)
                lineTo(center.x, center.y + r * 0.9f)
                lineTo(center.x - r * 0.65f, center.y)
                close()
            }
            drawPath(path, Color(0xFFE0F2FE))
        }
        "trophy" -> {
            drawRoundRect(
                color = Color(0xFFFEF08A),
                topLeft = center - Offset(r * 0.6f, r * 0.75f),
                size = Size(r * 1.2f, r * 0.9f),
                cornerRadius = CornerRadius(r * 0.3f)
            )
            drawLine(
                color = Color(0xFFFEF08A),
                start = center + Offset(0f, r * 0.1f),
                end = center + Offset(0f, r * 0.75f),
                strokeWidth = r * 0.35f
            )
            drawLine(
                color = Color(0xFFFEF08A),
                start = center + Offset(-r * 0.55f, r * 0.75f),
                end = center + Offset(r * 0.55f, r * 0.75f),
                strokeWidth = r * 0.3f,
                cap = StrokeCap.Round
            )
        }
        "lion", "tiger" -> {
            drawCircle(Color(0xFFFEF08A), radius = r * 0.75f, center = center)
            drawCircle(Color(0xFF78350F), radius = r * 0.2f, center = center + Offset(-r * 0.28f, -r * 0.15f))
            drawCircle(Color(0xFF78350F), radius = r * 0.2f, center = center + Offset(r * 0.28f, -r * 0.15f))
            drawCircle(Color(0xFF78350F), radius = r * 0.18f, center = center + Offset(0f, r * 0.2f))
        }
        "penguin" -> {
            drawCircle(Color(0xFF0F172A), radius = r * 0.75f, center = center)
            drawCircle(Color.White, radius = r * 0.5f, center = center + Offset(0f, r * 0.15f))
            drawCircle(Color(0xFFF59E0B), radius = r * 0.22f, center = center)
        }
        "alien" -> {
            drawOval(
                color = Color(0xFFA7F3D0),
                topLeft = center - Offset(r * 0.65f, r * 0.8f),
                size = Size(r * 1.3f, r * 1.6f)
            )
            drawCircle(Color.Black, radius = r * 0.22f, center = center + Offset(-r * 0.28f, -r * 0.1f))
            drawCircle(Color.Black, radius = r * 0.22f, center = center + Offset(r * 0.28f, -r * 0.1f))
        }
        "helmet" -> {
            drawCircle(Color.White, radius = r * 0.78f, center = center)
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = center - Offset(r * 0.55f, r * 0.35f),
                size = Size(r * 1.1f, r * 0.65f),
                cornerRadius = CornerRadius(r * 0.25f)
            )
        }
        "football" -> {
            drawCircle(Color.White, radius = r * 0.75f, center = center)
            drawCircle(Color(0xFF0F172A), radius = r * 0.32f, center = center)
        }
        else -> {
            drawCircle(Color.White.copy(alpha = 0.92f), radius = r * 0.50f, center = center - Offset(0f, r * 0.16f))
        }
    }
}

@Composable
fun CustomPawnPreviewBadge(
    skinId: String,
    baseColor: Color,
    size: Dp = 44.dp,
    isHighlighted: Boolean = false
) {
    Canvas(modifier = Modifier.size(size)) {
        drawPawnSilhouetteOnScope(
            skinId = skinId,
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            radius = this.size.minDimension * 0.42f,
            baseColor = baseColor,
            isHighlighted = isHighlighted
        )
    }
}

/**
 * Interactive 3D Animated Dice component with recessed bevel pips and specular shading.
 */
@Composable
fun Animated3DDice(
    value: Int,
    isRolling: Boolean,
    accentColor: Color,
    enabled: Boolean = true,
    size: Dp = 68.dp,
    onRollClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_roll_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_angle"
    )

    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .size(size)
            .rotate(if (isRolling) spinAngle else 0f)
            .shadow(
                elevation = if (enabled) 18.dp else 4.dp,
                shape = shape,
                ambientColor = accentColor,
                spotColor = accentColor
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = if (enabled) {
                        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFCBD5E1))
                    } else {
                        listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                    }
                )
            )
            .border(
                width = 2.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, Color.White)
                ),
                shape = shape
            )
            .clickable(enabled = enabled && !isRolling, onClick = onRollClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.74f)) {
            val w = this.size.width
            val h = this.size.height
            val pipRadius = w * 0.11f
            val pipColor = if (value == 6 || value == 1) Color(0xFFDC2626) else Color(0xFF0F172A)

            val tl = Offset(w * 0.22f, h * 0.22f)
            val tr = Offset(w * 0.78f, h * 0.22f)
            val ml = Offset(w * 0.22f, h * 0.50f)
            val mc = Offset(w * 0.50f, h * 0.50f)
            val mr = Offset(w * 0.78f, h * 0.50f)
            val bl = Offset(w * 0.22f, h * 0.78f)
            val br = Offset(w * 0.78f, h * 0.78f)

            val pips = when (value.coerceIn(1, 6)) {
                1 -> listOf(mc)
                2 -> listOf(tl, br)
                3 -> listOf(tl, mc, br)
                4 -> listOf(tl, tr, bl, br)
                5 -> listOf(tl, tr, mc, bl, br)
                else -> listOf(tl, tr, ml, mr, bl, br)
            }

            pips.forEach { pos ->
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = pipRadius * 1.12f,
                    center = pos + Offset(1f, 1.5f)
                )
                drawCircle(
                    color = pipColor,
                    radius = pipRadius,
                    center = pos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.40f),
                    radius = pipRadius * 0.35f,
                    center = pos - Offset(pipRadius * 0.25f, pipRadius * 0.25f)
                )
            }
        }
    }
}
