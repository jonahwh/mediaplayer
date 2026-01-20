@file:OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)

package com.example.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOneOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Selected
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

data class TrackInfo(
  val artUrl: String,
  val title: String,
  val artist: String,
  val duration: Duration,
  val isFavorite: Boolean,
)

enum class RepeatMode {
  None, All, One
}

private data class HeartParticle(
  val id: Long,
  val targetX: Float,
  val targetY: Float,
  val size: Float,
  val duration: Int,
)

@Composable
fun MusicPlayer(
  trackInfo: TrackInfo,
  bufferedPercent: Float,
  progress: Duration,
  repeatMode: RepeatMode = RepeatMode.None,
  onRepeatModeClick: () -> Unit = {},
  isPlaying: Boolean = false,
  onPlayPauseClick: () -> Unit = {},
  isFavorite: Boolean = false,
  onFavoriteClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onPrevClick: () -> Unit = {},
  onSeek: (Duration) -> Unit = {},
) {
  Column(
    Modifier
      .widthIn(max = 480.dp)
      .heightIn(max = 297.dp)
      .clip(MaterialTheme.shapes.large)
      .background(
        MaterialTheme.colorScheme.surface
      )
      .padding(26.dp), verticalArrangement = Arrangement.Top
  ) {
    Box(Modifier.padding(horizontal = 6.dp)) { Track(trackInfo) }
    Spacer(Modifier.height(height = 31.dp))
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
      Progress(bufferedPercent, progress, trackInfo.duration, onSeek)
    }
    Spacer(Modifier.height(height = 3.dp))
    Box(Modifier.padding(horizontal = 6.dp)) { Timers(progress, trackInfo.duration) }
    Spacer(Modifier.height(15.dp))
    Controls(
      repeatMode,
      onRepeatModeClick,
      isPlaying,
      onPlayPauseClick,
      isFavorite,
      onFavoriteClick,
      onNextClick,
      onPrevClick
    )
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
    Column(
      Modifier
        .align(Alignment.CenterVertically)
        .weight(1f)
    ) {
      val titleScrollState = rememberScrollState()
      val artistScrollState = rememberScrollState()
      val maxScroll = maxOf(titleScrollState.maxValue, artistScrollState.maxValue)

      LaunchedEffect(maxScroll, trackInfo.title, trackInfo.artist) {
        if (maxScroll > 0) {
          while (true) {
            delay(2000)
            animate(
              initialValue = 0f,
              targetValue = 1f,
              animationSpec = tween(
                durationMillis = maxScroll * 15,
                easing = FastOutSlowInEasing
              )
            ) { value, _ ->
              titleScrollState.dispatchRawDelta((value * titleScrollState.maxValue) - titleScrollState.value)
              artistScrollState.dispatchRawDelta((value * artistScrollState.maxValue) - artistScrollState.value)
            }
            delay(2000)
            animate(
              initialValue = 1f,
              targetValue = 0f,
              animationSpec = tween(
                durationMillis = maxScroll * 15,
                easing = FastOutSlowInEasing
              )
            ) { value, _ ->
              titleScrollState.dispatchRawDelta((value * titleScrollState.maxValue) - titleScrollState.value)
              artistScrollState.dispatchRawDelta((value * artistScrollState.maxValue) - artistScrollState.value)
            }
          }
        }
      }

      MarqueeText(
        text = trackInfo.title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        scrollState = titleScrollState
      )
      Spacer(Modifier.height(10.dp))
      MarqueeText(
        text = trackInfo.artist,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.outline,
        scrollState = artistScrollState
      )
    }
  }
}

@Composable
fun MarqueeText(
  text: String,
  style: TextStyle,
  color: Color,
  scrollState: ScrollState,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    style = style,
    color = color,
    maxLines = 1,
    overflow = TextOverflow.Clip,
    softWrap = false,
    modifier = modifier.horizontalScroll(scrollState, enabled = false)
  )
}

@Composable
fun Progress(
  bufferedPercent: Float,
  progress: Duration,
  duration: Duration,
  onSeek: (Duration) -> Unit,
) {
  var isDragging by remember { mutableStateOf(false) }
  var sliderValue by remember { mutableFloatStateOf(0f) }
  val durationMillis = if (duration.isPositive()) duration.inWholeMilliseconds.toFloat() else 0f

  LaunchedEffect(progress) {
    if (!isDragging) {
      sliderValue = progress.inWholeMilliseconds.toFloat()
    }
  }

  Slider(
    value = sliderValue,
    onValueChange = {
      isDragging = true
      sliderValue = it
    },
    onValueChangeFinished = {
      onSeek(sliderValue.toLong().milliseconds)
      isDragging = false
    },
    colors = SliderDefaults.colors(
      thumbColor = MaterialTheme.colorScheme.secondary,
      activeTrackColor = MaterialTheme.colorScheme.secondary,
      inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
    ),
    valueRange = 0f..durationMillis,
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
            .fillMaxWidth(if (durationMillis > 0) sliderValue / durationMillis else 0f)
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
private fun Controls(
  repeatMode: RepeatMode,
  onRepeatModeClick: () -> Unit,
  isPlaying: Boolean,
  onPlayPauseClick: () -> Unit,
  isFavorite: Boolean,
  onFavoriteClick: () -> Unit,
  onNextClick: () -> Unit,
  onPrevClick: () -> Unit,
) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val repeatIcon = when (repeatMode) {
      RepeatMode.None -> Icons.Filled.Repeat
      RepeatMode.All -> Icons.Filled.RepeatOn
      RepeatMode.One -> Icons.Filled.RepeatOneOn
    }

    PlayerButton(icon = repeatIcon, onClick = onRepeatModeClick)
    PlayerButton(icon = Icons.Filled.SkipPrevious, onClick = onPrevClick)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.2f else 1f, label = "scale")

    Button(
      onClick = onPlayPauseClick,
      modifier = Modifier
        .size(72.dp)
        .scale(scale),
      shape = CircleShape,
      colors = ButtonDefaults.buttonColors(containerColor = Selected),
      contentPadding = PaddingValues(0.dp),
      interactionSource = interactionSource
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(48.dp)
      )
    }
    PlayerButton(icon = Icons.Filled.SkipNext, onClick = onNextClick)

    val favoriteIcon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder

    Box(contentAlignment = Alignment.Center) {
      val hearts = remember { mutableStateListOf<HeartParticle>() }

      hearts.forEach { heart ->
        FloatingHeart(heart) {
          hearts.remove(heart)
        }
      }

      PlayerButton(
        icon = favoriteIcon,
        onClick = {
          if (!isFavorite) {
            repeat(5) {
              hearts.add(
                HeartParticle(
                  id = Random.nextLong(),
                  targetX = Random.nextFloat() * 100f - 50f,
                  targetY = Random.nextFloat() * -100f - 50f,
                  size = Random.nextFloat() * 10f + 20f,
                  duration = Random.nextInt(500, 1000)
                )
              )
            }
          }
          onFavoriteClick()
        }
      )
    }
  }
}

@Composable
private fun FloatingHeart(particle: HeartParticle, onComplete: () -> Unit) {
  val progress = remember { Animatable(0f) }

  LaunchedEffect(particle) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = particle.duration, easing = LinearEasing)
    )
    onComplete()
  }

  val x = particle.targetX * progress.value
  val y = particle.targetY * progress.value
  val alpha = 1f - progress.value
  val scale = 1f - (progress.value * 0.5f)

  Icon(
    imageVector = Icons.Filled.Favorite,
    contentDescription = null,
    tint = Color.Red,
    modifier = Modifier
      .size(particle.size.dp)
      .offset { IntOffset(x.dp.roundToPx(), y.dp.roundToPx()) }
      .alpha(alpha)
      .scale(scale)
  )
}

@Composable
private fun PlayerButton(
  icon: ImageVector,
  onClick: () -> Unit,
  tint: Color = MaterialTheme.colorScheme.onSurface,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 1.5f else 1f, label = "scale")

  CompositionLocalProvider(
    LocalRippleConfiguration provides RippleConfiguration(
      color = Color.White,
      rippleAlpha = RippleAlpha(
        draggedAlpha = 0.16f,
        focusedAlpha = 0.12f,
        hoveredAlpha = 0.08f,
        pressedAlpha = 0.4f
      )
    )
  ) {
    IconButton(
      onClick = onClick,
      interactionSource = interactionSource,
      modifier = Modifier.scale(scale)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
      )
    }
  }
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
        var repeatMode by remember { mutableStateOf(RepeatMode.None) }
        var isPlaying by remember { mutableStateOf(false) }
        var isFavorite by remember { mutableStateOf(false) }
        MusicPlayer(
          TrackInfo(
            "http://example.com",
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
