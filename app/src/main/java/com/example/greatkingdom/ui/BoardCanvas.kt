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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.example.greatkingdom.GreatKingdomGame
import com.example.greatkingdom.R
import com.example.greatkingdom.ui.theme.LastMoveHighlight
import com.example.greatkingdom.ui.theme.Player1Territory
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
    
    val boardTexture = ImageBitmap.imageResource(id = R.drawable.board_texture_white)
    val pieceP1 = ImageBitmap.imageResource(id = R.drawable.piece_blue_top)
    val pieceP2 = ImageBitmap.imageResource(id = R.drawable.piece_red_top)
    val pieceNeutral = ImageBitmap.imageResource(id = R.drawable.piece_center_top)

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

        // 1. Draw Board Texture
        drawImage(
            image = boardTexture,
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )

        // 2. Draw Stones (Castles)
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cellValue = boardState[r][c]
                if (cellValue != GreatKingdomGame.EMPTY) {
                   val pieceImage = when (cellValue) {
                        GreatKingdomGame.P1 -> pieceP1
                        GreatKingdomGame.P2 -> pieceP2
                        GreatKingdomGame.NEUTRAL -> pieceNeutral
                        else -> pieceP1 // Fallback
                    }
                    drawCastleImage(r, c, cellSize, pieceImage)
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

private fun DrawScope.drawCastleImage(row: Int, col: Int, cellSize: Float, image: ImageBitmap) {
    val padding = cellSize * 0.1f
    val imageSize = cellSize - padding * 2
    
    drawImage(
        image = image,
        dstOffset = IntOffset(
            (col * cellSize + padding).toInt(),
            (row * cellSize + padding).toInt()
        ),
        dstSize = IntSize(imageSize.toInt(), imageSize.toInt())
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
