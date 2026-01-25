package com.example.greatkingdom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GreatKingdomViewModel : ViewModel(), GreatKingdomGame.GameListener {

    private val game = GreatKingdomGame(this)

    // UI States
    private val _boardState = MutableStateFlow(game.getBoardState())
    val boardState: StateFlow<Array<IntArray>> = _boardState.asStateFlow()

    private val _currentPlayer = MutableStateFlow(GreatKingdomGame.P1)
    val currentPlayer: StateFlow<Int> = _currentPlayer.asStateFlow()

    private val _gameOverMessage = MutableStateFlow<String?>(null)
    val gameOverMessage: StateFlow<String?> = _gameOverMessage.asStateFlow()

    private val _lastMove = MutableStateFlow<Pair<Int, Int>?>(null)
    val lastMove: StateFlow<Pair<Int, Int>?> = _lastMove.asStateFlow()

    private val _territoriesP1 = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val territoriesP1: StateFlow<List<Pair<Int, Int>>> = _territoriesP1.asStateFlow()

    private val _territoriesP2 = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val territoriesP2: StateFlow<List<Pair<Int, Int>>> = _territoriesP2.asStateFlow()
    
    // Side effects (Sound, Vibration) - Simple event trigger
    private val _playEffectTrigger = MutableStateFlow(0L)
    val playEffectTrigger: StateFlow<Long> = _playEffectTrigger.asStateFlow()

    init {
        // Initial sync
        _currentPlayer.value = game.currentPlayer
        updateTerritories()
    }

    // User Actions
    fun onCellClicked(row: Int, col: Int) {
        if (_gameOverMessage.value != null) return
        game.playTurn(row, col)
    }

    fun onPassClicked() {
        if (_gameOverMessage.value != null) return
        game.passTurn()
    }

    fun onUndoClicked() {
        if (_gameOverMessage.value != null) return
        game.undo()
    }

    fun onRestartClicked() {
        game.resetGame()
        _gameOverMessage.value = null
        updateTerritories()
    }

    private fun updateTerritories() {
        _territoriesP1.value = game.getTerritoryList(GreatKingdomGame.P1)
        _territoriesP2.value = game.getTerritoryList(GreatKingdomGame.P2)
    }

    // GameListener Implementation
    override fun onBoardUpdated() {
        // Force update by creating a shallow copy or notify
        // Since Array<IntArray> is mutable, StateFlow might not emit if ref is same.
        // But for this simple app, we can just emit the reference or a copy.
        // To ensure Compose recomposes, we might need a copy or a version ticker.
        // For now, let's emit the array. If Compose doesn't update, we'll fix it.
        _boardState.value = game.getBoardState().map { it.clone() }.toTypedArray()
        _lastMove.value = game.getLastMove()
        updateTerritories()
    }

    override fun onTurnChanged(currentPlayer: Int) {
        _currentPlayer.value = currentPlayer
    }

    override fun onGameOver(message: String) {
        _gameOverMessage.value = message
    }

    override fun onPiecePlaced() {
        _playEffectTrigger.value = System.currentTimeMillis()
    }
}
