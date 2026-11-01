package com.example.games

import com.example.data.AiDifficulty
import kotlin.math.abs

enum class PieceColor { WHITE, BLACK }

enum class PieceKind(val value: Int, val whiteSymbol: String, val blackSymbol: String, val sanChar: String) {
    PAWN(100, "♙", "♟", ""),
    KNIGHT(320, "♘", "♞", "N"),
    BISHOP(330, "♗", "♝", "B"),
    ROOK(500, "♖", "♜", "R"),
    QUEEN(900, "♕", "♛", "Q"),
    KING(20000, "♔", "♚", "K")
}

data class ChessPiece(
    val kind: PieceKind,
    val color: PieceColor,
    val hasMoved: Boolean = false
)

data class ChessSquare(val row: Int, val col: Int) {
    fun isValid(): Boolean = row in 0..7 && col in 0..7
    fun toAlgebraic(): String = "${('a' + col)}${8 - row}"
}

data class ChessMove(
    val from: ChessSquare,
    val to: ChessSquare,
    val piece: ChessPiece,
    val captured: ChessPiece? = null,
    val promotion: PieceKind? = null,
    val isEnPassant: Boolean = false,
    val isKingsideCastle: Boolean = false,
    val isQueensideCastle: Boolean = false
) {
    fun toNotation(): String {
        if (isKingsideCastle) return "O-O"
        if (isQueensideCastle) return "O-O-O"
        val cap = if (captured != null || isEnPassant) "x" else "-"
        val promo = if (promotion != null) "=${promotion.sanChar}" else ""
        return "${piece.kind.sanChar}${from.toAlgebraic()}$cap${to.toAlgebraic()}$promo"
    }
}

data class ChessBoardState(
    val grid: List<List<ChessPiece?>>, // 8x8: row 0 is rank 8 (Black base), row 7 is rank 1 (White base)
    val turn: PieceColor = PieceColor.WHITE,
    val enPassantTarget: ChessSquare? = null,
    val lastMove: ChessMove? = null,
    val capturedByWhite: List<PieceKind> = emptyList(),
    val capturedByBlack: List<PieceKind> = emptyList(),
    val moveHistory: List<String> = emptyList()
)

data class ChessPuzzle(
    val id: Int,
    val title: String,
    val description: String,
    val boardState: ChessBoardState,
    val expectedFrom: ChessSquare,
    val expectedTo: ChessSquare
)

object ChessEngine {

    fun createInitialBoard(): ChessBoardState {
        val rows = MutableList(8) { MutableList<ChessPiece?>(8) { null } }
        val backRank = listOf(
            PieceKind.ROOK, PieceKind.KNIGHT, PieceKind.BISHOP, PieceKind.QUEEN,
            PieceKind.KING, PieceKind.BISHOP, PieceKind.KNIGHT, PieceKind.ROOK
        )
        for (c in 0..7) {
            rows[0][c] = ChessPiece(backRank[c], PieceColor.BLACK)
            rows[1][c] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK)
            rows[6][c] = ChessPiece(PieceKind.PAWN, PieceColor.WHITE)
            rows[7][c] = ChessPiece(backRank[c], PieceColor.WHITE)
        }
        return ChessBoardState(grid = rows)
    }

    fun getPuzzles(): List<ChessPuzzle> {
        // Puzzle 1: Scholar's Mate finish (Queen Qxf7#)
        val p1Grid = MutableList(8) { MutableList<ChessPiece?>(8) { null } }
        p1Grid[0][4] = ChessPiece(PieceKind.KING, PieceColor.BLACK)
        p1Grid[0][3] = ChessPiece(PieceKind.QUEEN, PieceColor.BLACK)
        p1Grid[1][5] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK) // f7
        p1Grid[1][6] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK)
        p1Grid[4][2] = ChessPiece(PieceKind.BISHOP, PieceColor.WHITE) // Bc4
        p1Grid[3][7] = ChessPiece(PieceKind.QUEEN, PieceColor.WHITE) // Qh5
        p1Grid[7][4] = ChessPiece(PieceKind.KING, PieceColor.WHITE)

        // Puzzle 2: Back-Rank Rook Checkmate (Re1 -> Re8#)
        val p2Grid = MutableList(8) { MutableList<ChessPiece?>(8) { null } }
        p2Grid[0][6] = ChessPiece(PieceKind.KING, PieceColor.BLACK)
        p2Grid[1][5] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK)
        p2Grid[1][6] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK)
        p2Grid[1][7] = ChessPiece(PieceKind.PAWN, PieceColor.BLACK)
        p2Grid[7][4] = ChessPiece(PieceKind.ROOK, PieceColor.WHITE) // Re1
        p2Grid[7][6] = ChessPiece(PieceKind.KING, PieceColor.WHITE)

        // Puzzle 3: Royal Knight Fork (Nd5 -> Nc7+ winning Queen)
        val p3Grid = MutableList(8) { MutableList<ChessPiece?>(8) { null } }
        p3Grid[0][4] = ChessPiece(PieceKind.KING, PieceColor.BLACK)
        p3Grid[0][0] = ChessPiece(PieceKind.QUEEN, PieceColor.BLACK)
        p3Grid[3][3] = ChessPiece(PieceKind.KNIGHT, PieceColor.WHITE) // Nd5
        p3Grid[7][6] = ChessPiece(PieceKind.KING, PieceColor.WHITE)

        return listOf(
            ChessPuzzle(
                id = 1,
                title = "Puzzle #1: Royal Battery Mate",
                description = "White to move and deliver instant Checkmate on f7!",
                boardState = ChessBoardState(grid = p1Grid, turn = PieceColor.WHITE),
                expectedFrom = ChessSquare(3, 7),
                expectedTo = ChessSquare(1, 5)
            ),
            ChessPuzzle(
                id = 2,
                title = "Puzzle #2: Back-Rank Corridor",
                description = "White to move and deliver Back-Rank Checkmate on e8!",
                boardState = ChessBoardState(grid = p2Grid, turn = PieceColor.WHITE),
                expectedFrom = ChessSquare(7, 4),
                expectedTo = ChessSquare(0, 4)
            ),
            ChessPuzzle(
                id = 3,
                title = "Puzzle #3: Grandmaster Knight Fork",
                description = "Fork the Black King and Queen with a single Knight leap!",
                boardState = ChessBoardState(grid = p3Grid, turn = PieceColor.WHITE),
                expectedFrom = ChessSquare(3, 3),
                expectedTo = ChessSquare(1, 2)
            )
        )
    }

    fun isSquareAttacked(grid: List<List<ChessPiece?>>, square: ChessSquare, byColor: PieceColor): Boolean {
        // 1. Pawn attacks
        val pawnAttackRow = if (byColor == PieceColor.WHITE) square.row + 1 else square.row - 1
        for (dc in listOf(-1, 1)) {
            val c = square.col + dc
            if (pawnAttackRow in 0..7 && c in 0..7) {
                val p = grid[pawnAttackRow][c]
                if (p != null && p.color == byColor && p.kind == PieceKind.PAWN) return true
            }
        }

        // 2. Knight attacks
        val knightJumps = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in knightJumps) {
            val r = square.row + dr
            val c = square.col + dc
            if (r in 0..7 && c in 0..7) {
                val p = grid[r][c]
                if (p != null && p.color == byColor && p.kind == PieceKind.KNIGHT) return true
            }
        }

        // 3. King adjacent attacks
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = square.row + dr
                val c = square.col + dc
                if (r in 0..7 && c in 0..7) {
                    val p = grid[r][c]
                    if (p != null && p.color == byColor && p.kind == PieceKind.KING) return true
                }
            }
        }

        // 4. Orthogonal rays (Rook / Queen)
        val orthoDirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((dr, dc) in orthoDirs) {
            var r = square.row + dr
            var c = square.col + dc
            while (r in 0..7 && c in 0..7) {
                val p = grid[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.kind == PieceKind.ROOK || p.kind == PieceKind.QUEEN)) return true
                    break
                }
                r += dr
                c += dc
            }
        }

        // 5. Diagonal rays (Bishop / Queen)
        val diagDirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        for ((dr, dc) in diagDirs) {
            var r = square.row + dr
            var c = square.col + dc
            while (r in 0..7 && c in 0..7) {
                val p = grid[r][c]
                if (p != null) {
                    if (p.color == byColor && (p.kind == PieceKind.BISHOP || p.kind == PieceKind.QUEEN)) return true
                    break
                }
                r += dr
                c += dc
            }
        }

        return false
    }

    fun isInCheck(grid: List<List<ChessPiece?>>, color: PieceColor): Boolean {
        var kingSquare: ChessSquare? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val p = grid[r][c]
                if (p != null && p.color == color && p.kind == PieceKind.KING) {
                    kingSquare = ChessSquare(r, c)
                    break
                }
            }
        }
        val kSq = kingSquare ?: return false
        val opp = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        return isSquareAttacked(grid, kSq, opp)
    }

    fun applyMoveToGrid(grid: List<List<ChessPiece?>>, move: ChessMove): List<List<ChessPiece?>> {
        val next = grid.map { it.toMutableList() }.toMutableList()
        val movedPiece = move.piece.copy(
            kind = move.promotion ?: move.piece.kind,
            hasMoved = true
        )
        next[move.from.row][move.from.col] = null
        if (move.isEnPassant) {
            next[move.from.row][move.to.col] = null
        }
        if (move.isKingsideCastle) {
            val rook = next[move.from.row][7]
            next[move.from.row][7] = null
            next[move.from.row][5] = rook?.copy(hasMoved = true)
        } else if (move.isQueensideCastle) {
            val rook = next[move.from.row][0]
            next[move.from.row][0] = null
            next[move.from.row][3] = rook?.copy(hasMoved = true)
        }
        next[move.to.row][move.to.col] = movedPiece
        return next
    }

    fun applyMove(state: ChessBoardState, move: ChessMove): ChessBoardState {
        val nextGrid = applyMoveToGrid(state.grid, move)
        val epTarget = if (move.piece.kind == PieceKind.PAWN && abs(move.to.row - move.from.row) == 2) {
            ChessSquare((move.from.row + move.to.row) / 2, move.from.col)
        } else null

        val capturedKind = move.captured?.kind ?: if (move.isEnPassant) PieceKind.PAWN else null
        val nextCapWhite = if (capturedKind != null && state.turn == PieceColor.WHITE) {
            state.capturedByWhite + capturedKind
        } else state.capturedByWhite
        val nextCapBlack = if (capturedKind != null && state.turn == PieceColor.BLACK) {
            state.capturedByBlack + capturedKind
        } else state.capturedByBlack

        val nextTurn = if (state.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        val checkSuffix = if (isInCheck(nextGrid, nextTurn)) "+" else ""
        val notation = move.toNotation() + checkSuffix

        return state.copy(
            grid = nextGrid,
            turn = nextTurn,
            enPassantTarget = epTarget,
            lastMove = move,
            capturedByWhite = nextCapWhite,
            capturedByBlack = nextCapBlack,
            moveHistory = state.moveHistory + notation
        )
    }

    fun generateLegalMovesForSquare(state: ChessBoardState, from: ChessSquare): List<ChessMove> {
        val piece = state.grid[from.row][from.col] ?: return emptyList()
        if (piece.color != state.turn) return emptyList()
        val pseudo = mutableListOf<ChessMove>()
        val r = from.row
        val c = from.col

        when (piece.kind) {
            PieceKind.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
                val promoRow = if (piece.color == PieceColor.WHITE) 0 else 7
                val oneStep = ChessSquare(r + dir, c)
                if (oneStep.isValid() && state.grid[oneStep.row][oneStep.col] == null) {
                    if (oneStep.row == promoRow) {
                        listOf(PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT).forEach { pk ->
                            pseudo.add(ChessMove(from, oneStep, piece, promotion = pk))
                        }
                    } else {
                        pseudo.add(ChessMove(from, oneStep, piece))
                        val twoStep = ChessSquare(r + dir * 2, c)
                        if (r == startRow && state.grid[twoStep.row][twoStep.col] == null) {
                            pseudo.add(ChessMove(from, twoStep, piece))
                        }
                    }
                }
                // Captures & En Passant
                for (dc in listOf(-1, 1)) {
                    val capSq = ChessSquare(r + dir, c + dc)
                    if (!capSq.isValid()) continue
                    val target = state.grid[capSq.row][capSq.col]
                    if (target != null && target.color != piece.color) {
                        if (capSq.row == promoRow) {
                            listOf(PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT).forEach { pk ->
                                pseudo.add(ChessMove(from, capSq, piece, captured = target, promotion = pk))
                            }
                        } else {
                            pseudo.add(ChessMove(from, capSq, piece, captured = target))
                        }
                    } else if (state.enPassantTarget == capSq) {
                        pseudo.add(
                            ChessMove(
                                from = from,
                                to = capSq,
                                piece = piece,
                                captured = ChessPiece(PieceKind.PAWN, if (piece.color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE),
                                isEnPassant = true
                            )
                        )
                    }
                }
            }

            PieceKind.KNIGHT -> {
                val jumps = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
                for ((dr, dc) in jumps) {
                    val to = ChessSquare(r + dr, c + dc)
                    if (!to.isValid()) continue
                    val occ = state.grid[to.row][to.col]
                    if (occ == null || occ.color != piece.color) {
                        pseudo.add(ChessMove(from, to, piece, captured = occ))
                    }
                }
            }

            PieceKind.BISHOP, PieceKind.ROOK, PieceKind.QUEEN -> {
                val dirs = mutableListOf<Pair<Int, Int>>()
                if (piece.kind != PieceKind.BISHOP) {
                    dirs.addAll(listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
                }
                if (piece.kind != PieceKind.ROOK) {
                    dirs.addAll(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
                }
                for ((dr, dc) in dirs) {
                    var nr = r + dr
                    var nc = c + dc
                    while (nr in 0..7 && nc in 0..7) {
                        val to = ChessSquare(nr, nc)
                        val occ = state.grid[nr][nc]
                        if (occ == null) {
                            pseudo.add(ChessMove(from, to, piece))
                        } else {
                            if (occ.color != piece.color) {
                                pseudo.add(ChessMove(from, to, piece, captured = occ))
                            }
                            break
                        }
                        nr += dr
                        nc += dc
                    }
                }
            }

            PieceKind.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val to = ChessSquare(r + dr, c + dc)
                        if (!to.isValid()) continue
                        val occ = state.grid[to.row][to.col]
                        if (occ == null || occ.color != piece.color) {
                            pseudo.add(ChessMove(from, to, piece, captured = occ))
                        }
                    }
                }
                // Castling
                val opp = if (piece.color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                if (!piece.hasMoved && !isInCheck(state.grid, piece.color)) {
                    // Kingside
                    val kRook = state.grid[r][7]
                    if (kRook != null && kRook.kind == PieceKind.ROOK && !kRook.hasMoved &&
                        state.grid[r][5] == null && state.grid[r][6] == null &&
                        !isSquareAttacked(state.grid, ChessSquare(r, 5), opp) &&
                        !isSquareAttacked(state.grid, ChessSquare(r, 6), opp)
                    ) {
                        pseudo.add(ChessMove(from, ChessSquare(r, 6), piece, isKingsideCastle = true))
                    }
                    // Queenside
                    val qRook = state.grid[r][0]
                    if (qRook != null && qRook.kind == PieceKind.ROOK && !qRook.hasMoved &&
                        state.grid[r][1] == null && state.grid[r][2] == null && state.grid[r][3] == null &&
                        !isSquareAttacked(state.grid, ChessSquare(r, 3), opp) &&
                        !isSquareAttacked(state.grid, ChessSquare(r, 2), opp)
                    ) {
                        pseudo.add(ChessMove(from, ChessSquare(r, 2), piece, isQueensideCastle = true))
                    }
                }
            }
        }

        return pseudo.filter { candidate ->
            val nextGrid = applyMoveToGrid(state.grid, candidate)
            !isInCheck(nextGrid, piece.color)
        }
    }

    fun generateAllLegalMoves(state: ChessBoardState): List<ChessMove> {
        val all = mutableListOf<ChessMove>()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = state.grid[r][c]
                if (p != null && p.color == state.turn) {
                    all.addAll(generateLegalMovesForSquare(state, ChessSquare(r, c)))
                }
            }
        }
        return all
    }

    // Positional Evaluation + Alpha-Beta Minimax AI
    private fun evaluateBoard(grid: List<List<ChessPiece?>>, perspective: PieceColor): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val p = grid[r][c] ?: continue
                val centerBonus = (3 - abs(3 - r)) * 6 + (3 - abs(3 - c)) * 6
                val advancementBonus = if (p.kind == PieceKind.PAWN) {
                    if (p.color == PieceColor.WHITE) (6 - r) * 12 else (r - 1) * 12
                } else 0
                val pieceScore = p.kind.value + centerBonus + advancementBonus
                if (p.color == perspective) score += pieceScore else score -= pieceScore
            }
        }
        return score
    }

    fun findBestAiMove(state: ChessBoardState, difficulty: AiDifficulty): ChessMove? {
        val legal = generateAllLegalMoves(state)
        if (legal.isEmpty()) return null
        if (difficulty == AiDifficulty.BEGINNER) return legal.random()

        val searchDepth = when (difficulty) {
            AiDifficulty.BEGINNER -> 1
            AiDifficulty.EASY -> 1
            AiDifficulty.MEDIUM -> 2
            AiDifficulty.HARD -> 2
            AiDifficulty.EXPERT -> 3
        }

        val aiColor = state.turn
        var bestMove = legal.first()
        var bestScore = Int.MIN_VALUE

        val orderedMoves = legal.sortedByDescending { (it.captured?.kind?.value ?: 0) + (it.promotion?.value ?: 0) }

        for (move in orderedMoves) {
            val nextState = applyMove(state, move)
            val score = -negamax(nextState, searchDepth - 1, -100000, 100000, if (aiColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun negamax(state: ChessBoardState, depth: Int, alphaInit: Int, beta: Int, color: PieceColor): Int {
        if (depth <= 0) {
            return evaluateBoard(state.grid, color)
        }
        val moves = generateAllLegalMoves(state)
        if (moves.isEmpty()) {
            return if (isInCheck(state.grid, color)) -19000 - depth * 100 else 0
        }
        var alpha = alphaInit
        var maxEval = -100000
        val sorted = moves.sortedByDescending { it.captured?.kind?.value ?: 0 }.take(18)
        for (mv in sorted) {
            val child = applyMove(state, mv)
            val opp = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            val eval = -negamax(child, depth - 1, -beta, -alpha, opp)
            if (eval > maxEval) maxEval = eval
            if (eval > alpha) alpha = eval
            if (alpha >= beta) break
        }
        return maxEval
    }
}
