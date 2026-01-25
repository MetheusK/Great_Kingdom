package com.example.greatkingdom

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.greatkingdom.ui.GameScreen
import com.example.greatkingdom.ui.MainMenuScreen
import com.example.greatkingdom.ui.theme.GreatKingdomTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GreatKingdomViewModel by viewModels()
    private lateinit var vibrator: Vibrator
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Effects
        initEffects()

        setContent {
            GreatKingdomTheme {
                var currentScreen by remember { mutableStateOf(Screen.MENU) }

                when (currentScreen) {
                    Screen.MENU -> MainMenuScreen(
                        onStartGame = {
                            viewModel.onRestartClicked()
                            currentScreen = Screen.GAME
                        },
                        onExit = { finish() }
                    )
                    Screen.GAME -> GameScreen(
                        viewModel = viewModel,
                        onBackToMenu = { currentScreen = Screen.MENU }
                    )
                }
            }
        }

        // Listen for sound effects side-effect
        lifecycleScope.launch {
            viewModel.playEffectTrigger.collectLatest { timestamp ->
                if (timestamp > 0) {
                    playPlacementEffect()
                }
            }
        }
    }

    private fun initEffects() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(resources.openRawResourceFd(R.raw.tak))
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            mediaPlayer?.prepare()
            mediaPlayer?.setVolume(1.0f, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }

    private fun playPlacementEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                try { it.prepare() } catch (e: Exception) { e.printStackTrace() }
            }
            it.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    enum class Screen {
        MENU, GAME
    }
}
