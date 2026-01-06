package com.example.greatkingdom.game

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun testInitialSetup() {
        val engine = GameEngine()
        assertEquals(Stone.NEUTRAL, engine.board.getStone(Point(4, 4)))
        assertEquals(Stone.BLACK, engine.currentTurn)
    }

    @Test
    fun testPlaceStone() {
        val engine = GameEngine()
        assertTrue(engine.placeStone(0, 0))
        assertEquals(Stone.BLACK, engine.board.getStone(Point(0, 0)))
        assertEquals(Stone.WHITE, engine.currentTurn)
    }

    @Test
    fun testCapture() {
        val engine = GameEngine()
        // Black setup capture around 0,1
        // B at 0,0
        engine.placeStone(0, 0) // B
        engine.placeStone(0, 1) // W (To be captured)
        engine.placeStone(1, 1) // B
        engine.placeStone(5, 5) // W (dummy)
        engine.placeStone(0, 2) // B -> Captures 0,1? No, 0,1 has liberty at 0,2 (taken) and 1,1 (taken) and 0,0 (taken).
        // Wait:
        // 0,1 W neighbors: 0,0(B), 0,2(B), 1,1(B). No other neighbors (edge).
        // So 0,1 should be captured.

        assertEquals(Stone.EMPTY, engine.board.getStone(Point(0, 1)))
        assertEquals(1, engine.blackCaptured)
    }

    @Test
    fun testSuicideRule() {
        val engine = GameEngine()
        // Surround 0,0 with White
        // B: Pass, W: 1,0
        engine.pass() // B pass
        engine.placeStone(1, 0) // W
        engine.pass() // B pass
        engine.placeStone(0, 1) // W

        // Now 0,0 is surrounded by W. B tries to play at 0,0
        // It has 0 liberties immediately. Should be invalid (unless it captures, which it doesn't).
        assertFalse(engine.placeStone(0, 0))
    }

    @Test
    fun testNeutralStoneIsWall() {
        val engine = GameEngine()
        // Neutral is at 4,4
        // Try to place at 4,4 -> Should fail
        assertFalse(engine.placeStone(4, 4))
    }

    @Test
    fun testTerritoryScoring() {
        val engine = GameEngine()
        // Create a small black territory in corner
        engine.placeStone(1, 0) // B
        engine.pass()
        engine.placeStone(0, 1) // B

        // 0,0 is empty and surrounded by B (1,0 and 0,1).
        // BUT wait, in Go, the rest of the board is empty.
        // If B plays 1,0 and 0,1, and nothing else is on the board.
        // 0,0 is surrounded by B.
        // But what about the HUGE area outside?
        // 2,0, 3,0 ... all the way to 8,8.
        // They are adjacent to Black stones?
        // 1,0 is Black. 2,0 is empty.
        // So the huge empty region touches Black. Does it touch White? No (White passed).
        // So technically, ALL empty space touching Black is Black territory if White has no stones?
        // This is why usually you need to check if it touches BOTH.
        // If White has no stones, everything is Black territory?
        // Or does "Board Edge" counting matter?
        // In this specific test case, White PASSED. White has 0 stones.
        // So the big region touches Black (at 1,0 and 0,1) and touches NO White.
        // So the code thinks the entire board (minus 0,0 and the two stones) is Black territory.
        // Correct logic: If a region touches ONLY Black, it is Black.
        // So 78 is actually correct! (81 total - 1 neutral - 2 black - 0,0 = 77? wait.
        // 9x9 = 81. 1 Neutral. 2 Stones. 78 Empty.
        // 0,0 is one region (size 1). Touches B.
        // The rest is another region (size 77). Touches B.
        // So Total = 1 + 77 = 78.

        // To fix the test, we should place White stones to frame the board or delimit territory.

        engine.placeStone(8, 8) // White plays somewhere to claim territory or nullify
        // But simply placing one White stone makes the big region touch White too.
        // Region: (2,0...8,7 etc).
        // It touches B (at 1,0) and W (at 8,8).
        // So it becomes neutral/dame.
        // 0,0 touches ONLY B. So it should be 1.

        // Reset and try with White interference
    }

    @Test
    fun testTerritoryScoringCorrectly() {
        val engine = GameEngine()
        engine.placeStone(1, 0) // B
        engine.pass() // W
        engine.placeStone(0, 1) // B
        engine.placeStone(2, 2) // W - Just a stone out there.

        // The big region now touches B (1,0) and W (2,2). So it is neutral.
        // The small region (0,0) touches B (1,0, 0,1) and borders edge.
        // Does it touch W? No.

        val (blackScore, whiteScore) = engine.calculateScore()
        assertEquals(1, blackScore)
    }
}
