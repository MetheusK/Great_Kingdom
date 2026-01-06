package com.example.greatkingdom.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.greatkingdom.game.Board
import com.example.greatkingdom.game.Stone

@Composable
fun BoardView(
    board: Board,
    onPointClicked: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val gridSize = size.width
                    val cellSize = gridSize / board.size
                    // Determine clicked intersection
                    // Logic: Round to nearest intersection
                    // x = round((offset.x - padding) / cellSize)
                    // But here we draw lines centered in cells? No, Go is played ON intersections.

                    // Let's assume the grid fills the view.
                    // Lines are at: cellSize/2, cellSize/2 + cellSize, ...
                    // No, usually grid lines are the coordinate system.
                    // Let's map 0..8 to the width.
                    // gap = width / (size - 1)

                    val gap = gridSize / (board.size - 1) // Wait, if size=9, there are 8 gaps.
                    // But we need some padding for the stones on the edge.
                    // Standard approach: Divide width by size. Each cell is width/size.
                    // Intersection is at center of cell?
                    // Or lines are at: step/2 + i*step.

                    val step = gridSize / board.size
                    val col = ((offset.x) / step).toInt()
                    val row = ((offset.y) / step).toInt()

                    if (col in 0 until board.size && row in 0 until board.size) {
                        onPointClicked(col, row)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = size.width / board.size
            val radius = step / 2.5f

            // Draw Grid
            for (i in 0 until board.size) {
                val pos = i * step + step / 2

                // Horizontal
                drawLine(
                    color = Color.Black,
                    start = Offset(step / 2, pos),
                    end = Offset(size.width - step / 2, pos),
                    strokeWidth = 2f
                )

                // Vertical
                drawLine(
                    color = Color.Black,
                    start = Offset(pos, step / 2),
                    end = Offset(pos, size.height - step / 2),
                    strokeWidth = 2f
                )
            }

            // Draw Stones
            for (i in 0 until board.size) {
                for (j in 0 until board.size) {
                    val stone = board.grid[i][j]
                    if (stone != Stone.EMPTY) {
                        val center = Offset(
                            x = i * step + step / 2,
                            y = j * step + step / 2
                        )
                        val color = when (stone) {
                            Stone.BLACK -> Color.Black
                            Stone.WHITE -> Color.White
                            Stone.NEUTRAL -> Color.Gray
                            else -> Color.Transparent
                        }

                        drawCircle(
                            color = color,
                            radius = radius,
                            center = center
                        )

                        // Draw outline for White stones to make them visible on white background?
                        // Actually board background is usually wood color. We'll handle that in GameScreen.
                        if (stone == Stone.WHITE) {
                            drawCircle(
                                color = Color.Black,
                                radius = radius,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }
                    }
                }
            }

            // Highlight star points (optional) - (2,2), (6,2), (4,4), (2,6), (6,6) for 9x9
            // But neutral is at (4,4).
        }
    }
}
