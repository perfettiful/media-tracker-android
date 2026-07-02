package edu.metrostate.ics342.mediatracker.ui.search

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit

@Composable
fun MediaTypeFilterChips(
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "all"   to R.string.filter_all,
            "book"  to R.string.filter_books,
            "movie" to R.string.filter_movies,
            "show"  to R.string.filter_shows
        ).forEach { (key, labelRes) ->
            FilterChip(
                selected = selectedType == key,
                onClick  = { onTypeSelect(key) },
                label    = { Text(stringResource(labelRes)) },
                shape    = RoundedCornerShape(8.dp),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    }
}

@Composable
fun SearchResultCard(media: Media, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (media.coverUrl != null) {
                    // shimmer sits behind so the image crossfades over it instead of
                    // flashing the empty box for a frame between loading and loaded
                    ShimmerTile()
                    SubcomposeAsyncImage(
                        model              = ImageRequest.Builder(LocalContext.current)
                            .data(media.coverUrl)
                            .crossfade(300)
                            .build(),
                        contentDescription = media.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                        error              = { MediaTypeTile(media.mediaType) }
                    )
                } else {
                    MediaTypeTile(media.mediaType)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(media.title, style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(media.creatorCredit(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // only show a star once the item actually has ratings, otherwise
                    // everything reads "0.0" which looks broken
                    if (media.ratingCount > 0) {
                        Text("★ ${"%.1f".format(media.averageRating)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary)
                        Text("  ·  ${media.mediaType.replaceFirstChar { it.uppercase() }}" +
                            (media.publishedYear?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(media.mediaType.replaceFirstChar { it.uppercase() } +
                            (media.publishedYear?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// generic cover stand-in, colored tile + icon per media type. shows when
// theres no cover url or the image fails to load
@Composable
fun MediaTypeTile(mediaType: String) {
    val (tileColor, iconColor, icon) = when (mediaType) {
        "book"  -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Outlined.MenuBook)
        "movie" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Icons.Outlined.Movie)
        "show"  -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Icons.Outlined.Tv)
        else    -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Outlined.HelpOutline)
    }
    Box(
        modifier = Modifier.fillMaxSize().background(tileColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
    }
}

// skeleton shimmer while a real cover loads
@Composable
private fun ShimmerTile() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -200f,
        targetValue  = 200f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-offset"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val brush = Brush.linearGradient(
        colors = listOf(base.copy(alpha = 0.4f), base.copy(alpha = 0.9f), base.copy(alpha = 0.4f)),
        start  = Offset(offset, 0f),
        end    = Offset(offset + 150f, 150f)
    )
    Box(Modifier.fillMaxSize().background(brush))
}
