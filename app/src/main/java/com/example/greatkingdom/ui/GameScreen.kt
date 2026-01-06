package com.example.greatkingdom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greatkingdom.game.Stone

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val board = viewModel.boardState.value
    val turn = viewModel.currentTurn.value
    val scores = viewModel.scores.value
    val isGameOver = viewModel.isGameOver.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEDCAA)), // Wood-ish color
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Score Board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Black", fontSize = 20.sp, color = Color.Black)
                Text(text = "${scores.first}", fontSize = 24.sp, color = Color.Black)
            }

            Text(text = if (isGameOver) "GAME OVER" else "Turn: $turn", fontSize = 24.sp, color = Color.Red)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "White", fontSize = 20.sp, color = Color.White) // White text might be invisible on light bg
                // Let's use Black text for label
                Text(text = "White", fontSize = 20.sp, color = Color.Black)
                Text(text = "${scores.second}", fontSize = 24.sp, color = Color.Black)
            }
        }

        BoardView(
            board = board,
            onPointClicked = { x, y ->
                viewModel.onPointClicked(x, y)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.onPass() },
            enabled = !isGameOver
        ) {
            Text("Pass")
        }
    }
}
