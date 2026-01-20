package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

data class AudioTrack(
  val resName: String,
  val title: String,
  val artist: String,
  val artUrl: String = "https://blocks.astratic.com/img/general-img-square.png",
  val isFavorite: Boolean = false,
)

val initialPlaylist = listOf(
  AudioTrack(
    "blow_the_man_down",
    "Blow The Man Down",
    "USNA Midshipmen Glee Club"
  ),
  AudioTrack(
    "dunken_sailor",
    "Dunken Sailor",
    "USNA Midshipmen Glee Club"
  ),
  AudioTrack(
    "insomnia",
    "Insomnia Pt. 2",
    "Meydän"
  ),
  AudioTrack(
    "virus",
    "Virus (Instrumental)",
    "Kate Orange"
  ),
  AudioTrack(
    "entrevista",
    "Entrevista a Gil Imaná",
    "Gustavo Cardoso"
  ),
  AudioTrack(
    "rhapsody_in_blue",
    "Rhapsody in Blue",
    "George Gershwin"
  )
)

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Column(
            Modifier
              .padding(innerPadding)
              .padding(horizontal = 16.dp)
          ) {
            PlayerScreen()
          }
        }
      }
    }
  }

  @OptIn(UnstableApi::class)
  @Composable
  fun PlayerScreen() {
    val context = LocalContext.current

    val playlist = remember { mutableStateListOf(*initialPlaylist.toTypedArray()) }
    var repeatMode by remember { mutableStateOf(RepeatMode.None) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var trackDuration by remember { mutableLongStateOf(0L) }
    var bufferedPercent by remember { mutableFloatStateOf(0f) }
    var isForwardAnimation by remember { mutableStateOf(true) }

    val currentTrack = playlist[currentTrackIndex]

    val exoPlayer = remember {
      ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
      onDispose {
        exoPlayer.release()
      }
    }

    LaunchedEffect(currentTrack) {
      val mediaItem = exoPlayer.currentMediaItem
      val resId = context.resources.getIdentifier(currentTrack.resName, "raw", context.packageName)
      val uri = "android.resource://${context.packageName}/$resId"

      if (mediaItem?.localConfiguration?.uri.toString() != uri) {
        currentPosition = 0L
        trackDuration = 0L
        bufferedPercent = 0f

        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        if (resId != 0) {
          val newMediaItem = MediaItem.fromUri(uri)
          exoPlayer.setMediaItem(newMediaItem)
          exoPlayer.prepare()

          if (isPlaying) {
            exoPlayer.play()
          }
        } else {
          Log.e("MainActivity", "Resource not found: ${currentTrack.resName}")
        }
      }
    }

    DisposableEffect(exoPlayer) {
      val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
          if (playbackState == Player.STATE_READY) {
            trackDuration = exoPlayer.duration.coerceAtLeast(0L)
          }
          if (playbackState == Player.STATE_ENDED) {
            if (repeatMode == RepeatMode.One) {
              exoPlayer.seekTo(0)
              exoPlayer.play()
            } else if (repeatMode == RepeatMode.All || currentTrackIndex < playlist.lastIndex) {
              isForwardAnimation = true
              if (currentTrackIndex < playlist.lastIndex) {
                currentTrackIndex++
              } else if (repeatMode == RepeatMode.All) {
                currentTrackIndex = 0
              }
            } else {
              isPlaying = false
              currentPosition = 0
              exoPlayer.seekTo(0)
              exoPlayer.pause()
            }
          }
        }

        override fun onIsPlayingChanged(isPlayingState: Boolean) {
          isPlaying = isPlayingState
        }
      }
      exoPlayer.addListener(listener)
      onDispose {
        exoPlayer.removeListener(listener)
      }
    }

    LaunchedEffect(isPlaying) {
      if (isPlaying) {
        if (!exoPlayer.isPlaying && exoPlayer.playbackState != Player.STATE_ENDED) {
          exoPlayer.play()
        }
      } else {
        if (exoPlayer.isPlaying) {
          exoPlayer.pause()
        }
      }
    }

    LaunchedEffect(Unit) {
      while (true) {
        if (exoPlayer.isPlaying) {
          currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
        }

        if (trackDuration > 0) {
          val currentProgressPercent =
            currentPosition.toFloat() / trackDuration

          val maxBufferAhead = 0.15f + (Random.nextFloat() * 0.1f)
          val bufferTarget = (currentProgressPercent + maxBufferAhead).coerceAtMost(1f)

          if (bufferedPercent < bufferTarget) {
            bufferedPercent += Random.nextFloat() * 0.02f
            bufferedPercent = bufferedPercent.coerceAtMost(1f)
          }

          if (bufferedPercent < currentProgressPercent) {
            bufferedPercent = currentProgressPercent
          }
        } else {
          if (bufferedPercent < 0.05f) {
            bufferedPercent += 0.01f
          }
        }

        delay(100)
      }
    }

    MusicPlayer(
      TrackInfo(
        artUrl = currentTrack.artUrl,
        title = currentTrack.title,
        artist = currentTrack.artist,
        duration = trackDuration.milliseconds,
        isFavorite = currentTrack.isFavorite
      ),
      bufferedPercent = bufferedPercent,
      progress = currentPosition.milliseconds,
      repeatMode = repeatMode,
      onRepeatModeClick = {
        repeatMode = when (repeatMode) {
          RepeatMode.None -> RepeatMode.All
          RepeatMode.All -> RepeatMode.One
          RepeatMode.One -> RepeatMode.None
        }
      },
      isPlaying = isPlaying,
      onPlayPauseClick = {
        isPlaying = !isPlaying
      },
      isFavorite = currentTrack.isFavorite,
      onFavoriteClick = {
        val updatedTrack = currentTrack.copy(isFavorite = !currentTrack.isFavorite)
        playlist[currentTrackIndex] = updatedTrack
      },
      onNextClick = {
        isForwardAnimation = true
        if (currentTrackIndex < playlist.lastIndex) {
          currentTrackIndex++
        } else if (repeatMode == RepeatMode.All) {
          currentTrackIndex = 0
        }
      },
      onPrevClick = {
        isForwardAnimation = false
        if (currentPosition > 3000) {
          exoPlayer.seekTo(0)
          currentPosition = 0
        } else {
          if (currentTrackIndex > 0) {
            currentTrackIndex--
          } else if (repeatMode == RepeatMode.All) {
            currentTrackIndex = playlist.lastIndex
          }
        }
      },
      onSeek = { seekDuration ->
        exoPlayer.seekTo(seekDuration.inWholeMilliseconds)
        currentPosition = seekDuration.inWholeMilliseconds
      },
      isForwardAnimation = isForwardAnimation
    )
  }
}
