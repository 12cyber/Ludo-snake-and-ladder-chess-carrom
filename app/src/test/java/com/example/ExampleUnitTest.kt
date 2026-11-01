package com.example

import com.example.data.AiDifficulty
import com.example.games.ChessEngine
import com.example.games.ChessSquare
import com.example.games.PieceColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun chessEngine_initialBoardHas20LegalMoves() {
        val initial = ChessEngine.createInitialBoard()
        val moves = ChessEngine.generateAllLegalMoves(initial)
        // 16 pawn moves (1 or 2 squares) + 4 knight leaps = 20 legal opening moves
        assertEquals(20, moves.size)
        assertEquals(PieceColor.WHITE, initial.turn)
    }

    @Test
    fun chessEngine_aiSelectsValidMove() {
        val initial = ChessEngine.createInitialBoard()
        val aiMove = ChessEngine.findBestAiMove(initial, AiDifficulty.MEDIUM)
        assertNotNull(aiMove)
        val next = ChessEngine.applyMove(initial, aiMove!!)
        assertEquals(PieceColor.BLACK, next.turn)
        assertEquals(1, next.moveHistory.size)
    }

    @Test
    fun chessEngine_puzzlesHaveValidSolutionMoves() {
        val puzzles = ChessEngine.getPuzzles()
        assertTrue(puzzles.isNotEmpty())
        val firstPuzzle = puzzles.first()
        val legalFromExpected = ChessEngine.generateLegalMovesForSquare(
            firstPuzzle.boardState,
            ChessSquare(firstPuzzle.expectedFrom.row, firstPuzzle.expectedFrom.col)
        )
        assertTrue(legalFromExpected.any { it.to == firstPuzzle.expectedTo })
    }
}
