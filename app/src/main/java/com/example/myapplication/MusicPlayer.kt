@file:OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)

package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Selected
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TrackInfo(
  val artUrl: String,
  val title: String,
  val artist: String,
  val duration: Duration,
  val isFavorite: Boolean,
)

@Composable
fun MusicPlayer(
  trackInfo: TrackInfo,
  bufferedPercent: Float,
  progress: Duration,
) {
  Column(
    Modifier
      .width(480.dp)
      .height(297.dp)
      .clip(MaterialTheme.shapes.large)
      .background(
        MaterialTheme.colorScheme.surface
      )
      .padding(26.dp), verticalArrangement = Arrangement.Top
  ) {
    Box(Modifier.padding(horizontal = 6.dp)) { Track(trackInfo) }
    Spacer(Modifier.height(height = 31.dp))
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
      Progress(bufferedPercent, progress, trackInfo.duration)
    }
    Spacer(Modifier.height(height = 3.dp))
    Box(Modifier.padding(horizontal = 6.dp)) { Timers(progress, trackInfo.duration) }
    Spacer(Modifier.height(15.dp))
    Controls()
  }
}

@Composable
fun Track(trackInfo: TrackInfo) {
  Row(Modifier.fillMaxWidth()) {
    AsyncImage(
      modifier = Modifier
        .size(88.dp)
        .clip(MaterialTheme.shapes.extraSmall),
      model = trackInfo.artUrl,
      contentDescription = null,
      fallback = painterResource(R.drawable.no_art),
    )
    Spacer(Modifier.width(16.dp))
    Column(Modifier.align(Alignment.CenterVertically)) {
      Text(
        text = trackInfo.title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(Modifier.height(10.dp))
      Text(
        text = trackInfo.artist,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.outline,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun Progress(bufferedPercent: Float, progress: Duration, duration: Duration) {
  var progressFloat by remember { mutableFloatStateOf(progress.inWholeSeconds.toFloat()) }
  val durationFloat = if (duration.isPositive()) duration.inWholeSeconds.toFloat() else 0f

  Slider(
    value = progressFloat,
         onValueChange = { progressFloat = it },
         colors = SliderDefaults.colors(
           thumbColor = MaterialTheme.colorScheme.secondary,
           activeTrackColor = MaterialTheme.colorScheme.secondary,
           inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
         ),
         valueRange = 0f..durationFloat,
         modifier = Modifier.height(12.dp),
         thumb = {
           Box(
             Modifier
               .padding(top = 1.5.dp)
               .size(12.dp)
               .background(Color.White, CircleShape)
           )
         },
         track = {
           Box(
             modifier = Modifier
               .height(3.16.dp)
               .fillMaxWidth()
           ) {
             Box(
               modifier = Modifier
                 .fillMaxWidth()
                 .fillMaxHeight()
                 .background(MaterialTheme.colorScheme.outlineVariant)
             )
           }
           Box(
             modifier = Modifier
               .height(3.16.dp)
               .fillMaxWidth()
           ) {
             Box(
               modifier = Modifier
                 .fillMaxWidth(bufferedPercent)
                 .fillMaxHeight()
                 .background(MaterialTheme.colorScheme.outline)
             )
           }
           Box(
             modifier = Modifier
               .height(3.16.dp)
               .fillMaxWidth()
           ) {
             Box(
               modifier = Modifier
                 .fillMaxWidth((progress / duration).toFloat())
                 .fillMaxHeight()
                 .background(MaterialTheme.colorScheme.onPrimary)
             )
           }
         })
}

@Composable
fun Timers(progress: Duration, duration: Duration) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    TimerText(progress)
    TimerText(duration)
  }
}

@Composable
fun TimerText(duration: Duration) {
  Text(
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
    text = formatDuration(duration)
  )
}

private fun formatDuration(duration: Duration): String {
  val seconds = duration.inWholeSeconds
  val minutes = seconds / 60
  val remainingSeconds = seconds % 60
  return "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
private fun Controls() {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(36.dp, alignment = Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically
  ) {
    PlayerButton(icon = Icons.Filled.Repeat)
    PlayerButton(icon = Icons.Filled.SkipPrevious)
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(Selected),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.Pause,
        contentDescription = "Pause",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(48.dp)
      )
    }
    PlayerButton(icon = Icons.Filled.SkipNext)
    PlayerButton(icon = Icons.Filled.FavoriteBorder)
  }
}

@Composable
private fun PlayerButton(icon: ImageVector) {
  Icon(
    imageVector = icon,
    contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.size(24.dp)
  )
}

@Preview(widthDp = 600, heightDp = 400)
@Composable
fun MusicPlayerPreview() {
  val context = LocalContext.current

  val previewHandler = AsyncImagePreviewHandler {
    context.getDrawable(R.drawable.no_art)!!.asImage()
  }

  CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {

    MyApplicationTheme {
      Box {
        MusicPlayer(
          TrackInfo(
            "http://example.com",
            "Black Friday (pretty like the sun)",
            "Lost Frequencies, Tom Odell, Poppy Baskcomb",
            311.seconds,
            false
          ), 0.5f, 60.seconds
        )
      }
    }
  }
}
