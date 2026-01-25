package com.example.greatkingdom

class GreatKingdomGame(private val listener: GameListener) {
    companion object {
        const val EMPTY =0
        const val P1 = 1 // 선공 (파랑)
        const val P2 = 2 // 후공 (주황)
        const val NEUTRAL = 3
        const val SIZE = 9
    }

    private var board = Array(SIZE) { IntArray(SIZE) { EMPTY } }
    var currentPlayer = P1
    private var passCount = 0
    private var lastMove: Pair<Int, Int>? = null // 무르기를 위한 마지막 수 저장

    init {
        setupNeutralPiece()
    }

    fun getLastMove(): Pair<Int, Int>? {
        return lastMove
    }
    interface GameListener {
        fun onBoardUpdated()
        fun onTurnChanged(currentPlayer: Int)
        fun onGameOver(message: String)
        fun onPiecePlaced() // 돌이 놓였을 때 호출될 함수
    }

    private fun setupNeutralPiece() {
        val center = SIZE / 2
        board[center][center] = NEUTRAL
    }

    /**
     * 특정 플레이어의 영토 좌표 리스트를 반환합니다.
     */
    fun getTerritoryList(player: Int): List<Pair<Int, Int>> {
        val territories = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (board[r][c] == EMPTY && isMyTerritory(r, c, player)) {
                    territories.add(Pair(r, c))
                }
            }
        }
        return territories
    }

    /**
     * 특정 좌표가 해당 플레이어(checkPlayer)의 영토인지 판정합니다.
     */
    private fun isMyTerritory(row: Int, col: Int, checkPlayer: Int): Boolean {
        val opponent = if (checkPlayer == P1) P2 else P1
        val visited = Array(SIZE) { BooleanArray(SIZE) }
        var containsOpponentPiece = false
        var edgeTop = false; var edgeBottom = false; var edgeLeft = false; var edgeRight = false

        fun dfs(r: Int, c: Int) {
            if (r !in 0 until SIZE || c !in 0 until SIZE) return
            if (visited[r][c] || board[r][c] == checkPlayer || board[r][c] == NEUTRAL) return
            if (board[r][c] == opponent) {
                containsOpponentPiece = true
                return
            }
            visited[r][c] = true
            if (r == 0) edgeTop = true
            if (r == SIZE - 1) edgeBottom = true
            if (c == 0) edgeLeft = true
            if (c == SIZE - 1) edgeRight = true

            dfs(r + 1, c); dfs(r - 1, c); dfs(r, c + 1); dfs(r, c - 1)
        }

        dfs(row, col)
        if (edgeTop && edgeBottom && edgeLeft && edgeRight) return false
        return !containsOpponentPiece
    }

    fun playTurn(row: Int, col: Int) {
        if (row !in 0 until SIZE || col !in 0 until SIZE) return
        if (board[row][col] != EMPTY) return
        val opponent = if (currentPlayer == P1) P2 else P1
        if (isMyTerritory(row, col, opponent)) return

        board[row][col] = currentPlayer
        lastMove = Pair(row, col) // 현재 수를 마지막 수로 기록
        passCount = 0

        // 돌이 놓였다는 신호를 MainActivity로 전송
        listener.onPiecePlaced()

        if (checkCapture(row, col)) {
            val winner = if (currentPlayer == P1) "선공 (파랑)" else "후공 (주황)"
            listener.onGameOver("공성 성공! $winner 승리")
            return
        }

        listener.onBoardUpdated()
        switchTurn()
    }

    /**
     * 마지막 수를 되돌리는 '무르기' 기능
     */
    fun undo() {
        lastMove?.let {
            board[it.first][it.second] = EMPTY // 마지막에 둔 돌을 빈 칸으로 변경
            lastMove = null // 무르기 기록 초기화
            switchTurn() // 턴을 이전 플레이어에게 되돌림
            listener.onBoardUpdated()
        }
    }

    fun passTurn() {
        passCount++
        if (passCount >= 2) {
            calculateWinner()
        } else {
            lastMove = null // 패스 시 무르기 비활성화
            switchTurn()
        }
    }

    private fun calculateWinner() {
        val p1Count = getTerritoryList(P1).size
        val p2Count = getTerritoryList(P2).size
        val winnerMessage = if (p1Count >= p2Count + 3) {
            "선공 (파랑) 승리! (파랑: $p1Count, 주황: $p2Count)"
        } else {
            "후공 (주황) 승리! (파랑: $p1Count, 주황: $p2Count)"
        }
        listener.onGameOver("""연속 패스로 종료되었습니다.
$winnerMessage""")
    }

    private fun checkCapture(row: Int, col: Int): Boolean {
        val opponent = if (currentPlayer == P1) P2 else P1
        val dr = intArrayOf(1, -1, 0, 0)
        val dc = intArrayOf(0, 0, 1, -1)
        for (i in 0 until 4) {
            val nr = row + dr[i]
            val nc = col + dc[i]
            if (nr in 0 until SIZE && nc in 0 until SIZE && board[nr][nc] == opponent) {
                if (isSurrounded(nr, nc, opponent)) return true
            }
        }
        return false
    }

    private fun isSurrounded(r: Int, c: Int, target: Int): Boolean {
        val visited = Array(SIZE) { BooleanArray(SIZE) }
        var hasLiberty = false
        fun dfs(currR: Int, currC: Int) {
            if (currR !in 0 until SIZE || currC !in 0 until SIZE || visited[currR][currC] || hasLiberty) return
            visited[currR][currC] = true
            if (board[currR][currC] == EMPTY) {
                hasLiberty = true
                return
            }
            if (board[currR][currC] == target) {
                dfs(currR + 1, currC); dfs(currR - 1, currC); dfs(currR, currC + 1); dfs(currR, currC - 1)
            }
        }
        dfs(r, c)
        return !hasLiberty
    }

    private fun switchTurn() {
        currentPlayer = if (currentPlayer == P1) P2 else P1
        listener.onTurnChanged(currentPlayer)
    }

    fun resetGame() {
        board = Array(SIZE) { IntArray(SIZE) { EMPTY } }
        setupNeutralPiece()
        currentPlayer = P1
        passCount = 0
        lastMove = null // 게임 리셋 시 무르기 기록도 삭제
        listener.onBoardUpdated()
        listener.onTurnChanged(currentPlayer)
    }

    fun getBoardState() = board
}
