package com.example.greatkingdom.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons

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
            .fillMaxSize(), // No background here to let parent Box background show through
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
                .height(64.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text(text = stringResource(R.string.btn_game_start), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

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

    // Pause on Back Button
    BackHandler(enabled = !showPauseDialog) {
        showPauseDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // No background, transparent
    ) {
        // Pause Button (Top Right)
        // Pause Button (Top Right)
        IconButton(
            onClick = { showPauseDialog = true },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            // Custom Pause Icon (||)
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.size(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                )
            }
        }

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
                    // Removed background(Color(0xFFEEEEEE)) to be clean or use BoardCanvas internal
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

        // Dialog Overlays
        if (showPauseDialog) {
            PauseOverlay(
                onResume = { showPauseDialog = false },
                onGoMain = onBackToMenu
            )
        }

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
            .background(Color.Black.copy(alpha = 0.7f)) // Darken background
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Luxury Card Design
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFFD700)), // Gold Border
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)) // Dark elegant background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .width(IntrinsicSize.Max)
            ) {
                // Trophy Icon or Decorative Header
                Text(
                    text = "🏆",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Winner / Message Text
                // Check if message mentions Red/Blue to style accordingly
                val highlightColor = if (message.contains("파랑")) Player1Color 
                                    else if (message.contains("빨강") || message.contains("주황")) Player2Color 
                                    else Color.White

                Text(
                    text = message.replace(" 🏆", ""), // Remove trophy if it was in string
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = highlightColor)
                ) {
                    Text("다시 시작", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onGoMain,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("메인 화면으로", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onGoMain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White), // Simple White border for Pause
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(32.dp)
                    .width(IntrinsicSize.Max)
            ) {
                Text(
                    text = stringResource(R.string.dialog_pause_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)) // Standard Blue for action
                ) {
                    Text(stringResource(R.string.btn_resume), color = Color.White, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onGoMain,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                     Text(stringResource(R.string.btn_go_main), fontSize = 16.sp)
                }
            }
        }
    }
}
