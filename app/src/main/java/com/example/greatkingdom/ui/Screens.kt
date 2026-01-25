package com.example.greatkingdom.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greatkingdom.GreatKingdomGame
import com.example.greatkingdom.GreatKingdomViewModel
import com.example.greatkingdom.R
import com.example.greatkingdom.ui.theme.AppBackground
import com.example.greatkingdom.ui.theme.Player1Color
import com.example.greatkingdom.ui.theme.Player2Color
import com.example.greatkingdom.ui.theme.Player1Territory
import com.example.greatkingdom.ui.theme.Player2Territory

@Composable
fun MainMenuScreen(onStartGame: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground), // Dark background
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.title_main),
            color = Color(0xFFFFD700), // Gold
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 60.dp)
        )

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .width(220.dp)
                .height(64.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text(text = stringResource(R.string.btn_game_start), fontSize = 18.sp)
        }

        Button(
            onClick = onExit,
            modifier = Modifier
                .width(220.dp)
                .height(64.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text(text = stringResource(R.string.btn_exit), fontSize = 18.sp)
        }
    }
}

@Composable
fun GameScreen(
    viewModel: GreatKingdomViewModel,
    onBackToMenu: () -> Unit
) {
    val boardState by viewModel.boardState.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val lastMove by viewModel.lastMove.collectAsState()
    val gameOverMessage by viewModel.gameOverMessage.collectAsState()
    val territoriesP1 by viewModel.territoriesP1.collectAsState()
    val territoriesP2 by viewModel.territoriesP2.collectAsState()

    var showPauseDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Text
            val pName = if (currentPlayer == GreatKingdomGame.P1) stringResource(R.string.player_p1) else stringResource(R.string.player_p2)
            val pColor = if (currentPlayer == GreatKingdomGame.P1) Player1Color else Player2Color
            
            Text(
                text = stringResource(R.string.status_turn_format, pName),
                color = pColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Board Canvas
            BoardCanvas(
                boardState = boardState,
                lastMove = lastMove,
                territoriesP1 = territoriesP1,
                territoriesP2 = territoriesP2,
                onCellClick = viewModel::onCellClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFEEEEEE))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = viewModel::onUndoClicked,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444))
                ) {
                    Text(stringResource(R.string.btn_undo))
                }
                Button(
                    onClick = viewModel::onPassClicked,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444))
                ) {
                    Text(stringResource(R.string.btn_pass))
                }
            }
        }

        // Pause/Back handling logic needed?
        // Dialog Overlays
        if (gameOverMessage != null) {
            ResultOverlay(
                message = gameOverMessage!!,
                onRestart = viewModel::onRestartClicked,
                onGoMain = onBackToMenu
            )
        }
    }
}

@Composable
fun ResultOverlay(
    message: String,
    onRestart: () -> Unit,
    onGoMain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = false) {}, // Block clicks
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Banner color logic
            val bannerColor = if (message.contains("파랑")) Player1Territory
            else if (message.contains("주황")) Player2Territory
            else Color.Gray

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerColor)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$message 🏆",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.width(220.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("다시 시작", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGoMain,
                modifier = Modifier.width(220.dp).height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
            ) {
                Text("메인 화면으로")
            }
        }
    }
}
