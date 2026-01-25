package com.example.greatkingdom.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.greatkingdom.GreatKingdomGame
import com.example.greatkingdom.ui.theme.LastMoveHighlight
import com.example.greatkingdom.ui.theme.Player1Color
import com.example.greatkingdom.ui.theme.Player1Territory
import com.example.greatkingdom.ui.theme.Player2Color
import com.example.greatkingdom.ui.theme.Player2Territory

@Composable
fun BoardCanvas(
    boardState: Array<IntArray>,
    lastMove: Pair<Int, Int>?,
    territoriesP1: List<Pair<Int, Int>>,
    territoriesP2: List<Pair<Int, Int>>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val boardSize = 9

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / boardSize
                    val col = (offset.x / cellSize).toInt()
                    val row = (offset.y / cellSize).toInt()
                    if (row in 0 until boardSize && col in 0 until boardSize) {
                        onCellClick(row, col)
                    }
                }
            }
    ) {
        val cellSize = size.width / boardSize

        // 1. Draw Grid
        drawGrid(boardSize, cellSize)

        // 2. Draw Stones (Castles)
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cellValue = boardState[r][c]
                if (cellValue != GreatKingdomGame.EMPTY) {
                    drawCastle(r, c, cellSize, cellValue)
                }
            }
        }

        // 3. Highlight Last Move
        lastMove?.let { (r, c) ->
            val padding = cellSize * 0.05f
            drawRect(
                color = LastMoveHighlight,
                topLeft = Offset(c * cellSize + padding, r * cellSize + padding),
                size = Size(cellSize - padding * 2, cellSize - padding * 2),
                style = Stroke(width = 8f)
            )
        }

        // 4. Draw Territory Numbers
        drawTerritories(territoriesP1, cellSize, Player1Territory, textMeasurer)
        drawTerritories(territoriesP2, cellSize, Player2Territory, textMeasurer)
    }
}

private fun DrawScope.drawGrid(boardSize: Int, cellSize: Float) {
    for (i in 0..boardSize) {
        val pos = i * cellSize
        drawLine(
            color = Color.Black,
            start = Offset(pos, 0f),
            end = Offset(pos, size.height),
            strokeWidth = 5f
        )
        drawLine(
            color = Color.Black,
            start = Offset(0f, pos),
            end = Offset(size.width, pos),
            strokeWidth = 5f
        )
    }
}

private fun DrawScope.drawCastle(row: Int, col: Int, cellSize: Float, type: Int) {
    val color = when (type) {
        GreatKingdomGame.P1 -> Player1Color
        GreatKingdomGame.P2 -> Player2Color
        else -> Color.Gray
    }
    val padding = cellSize * 0.15f
    drawRoundRect(
        color = color,
        topLeft = Offset(col * cellSize + padding, row * cellSize + padding),
        size = Size(cellSize - padding * 2, cellSize - padding * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
    )
}

private fun DrawScope.drawTerritories(
    territories: List<Pair<Int, Int>>,
    cellSize: Float,
    color: Color,
    textMeasurer: TextMeasurer
) {
    territories.forEachIndexed { index, (r, c) ->
        val centerX = c * cellSize + cellSize / 2
        val centerY = r * cellSize + cellSize / 2

        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = cellSize / 3,
            center = Offset(centerX, centerY)
        )

        val text = (index + 1).toString()
        val textLayoutResult = textMeasurer.measure(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                centerX - textLayoutResult.size.width / 2,
                centerY - textLayoutResult.size.height / 2
            )
        )
    }
}
