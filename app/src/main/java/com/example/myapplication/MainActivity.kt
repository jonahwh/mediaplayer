package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Column(Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            var repeatMode by remember { mutableStateOf(RepeatMode.None) }
            var isPlaying by remember { mutableStateOf(false) }
            var isFavorite by remember { mutableStateOf(false) }
            MusicPlayer(
              TrackInfo(
                "https://blocks.astratic.com/img/general-img-square.png",
                "Black Friday (pretty like the sun)",
                "Lost Frequencies, Tom Odell, Poppy Baskcomb",
                311.seconds,
                false
              ),
              0.5f,
              60.seconds,
              repeatMode = repeatMode,
              onRepeatModeClick = {
                repeatMode = when (repeatMode) {
                  RepeatMode.None -> RepeatMode.All
                  RepeatMode.All -> RepeatMode.One
                  RepeatMode.One -> RepeatMode.None
                }
              },
              isPlaying = isPlaying,
              onPlayPauseClick = { isPlaying = !isPlaying },
              isFavorite = isFavorite,
              onFavoriteClick = { isFavorite = !isFavorite }
            )
          }
        }
      }
    }
  }
}