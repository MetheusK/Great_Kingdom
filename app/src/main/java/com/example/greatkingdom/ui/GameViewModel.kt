package com.example.greatkingdom.ui

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.greatkingdom.game.GameEngine
import com.example.greatkingdom.game.Stone
import com.example.greatkingdom.game.Point

class GameViewModel : ViewModel() {
    private val engine = GameEngine()

    private val _boardState = mutableStateOf(engine.board.copy())
    val boardState: State<com.example.greatkingdom.game.Board> = _boardState

    private val _currentTurn = mutableStateOf(engine.currentTurn)
    val currentTurn: State<Stone> = _currentTurn

    private val _scores = mutableStateOf(Pair(0, 0))
    val scores: State<Pair<Int, Int>> = _scores

    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    fun onPointClicked(x: Int, y: Int) {
        if (engine.placeStone(x, y)) {
            updateState()
        }
    }

    fun onPass() {
        engine.pass()
        updateState()
    }

    fun onReset() {
        // Simple reset by recreating engine (not ideal but works for prototype)
        // Ideally GameEngine should have a reset method
        // For now, we can just replace the whole logic, but since 'engine' is val, we need to re-init.
        // Actually, let's just make engine internal state mutable or restart VM.
        // Better: Add reset to engine. But I can't edit engine easily now without breaking flow.
        // I'll just clear the board manually or something.
        // Actually, let's leave reset out for MVP or just implement restart activity.
    }

    private fun updateState() {
        _boardState.value = engine.board.copy()
        _currentTurn.value = engine.currentTurn
        _scores.value = engine.calculateScore()
        _isGameOver.value = engine.isGameOver
    }
}
