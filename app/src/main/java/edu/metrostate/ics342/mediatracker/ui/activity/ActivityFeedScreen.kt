package edu.metrostate.ics342.mediatracker.ui.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.data.model.ActivityEvent
import edu.metrostate.ics342.mediatracker.data.model.descriptionText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    onMediaClick: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: ActivityFeedViewModel = viewModel()
) {
    val feedItems by viewModel.feedItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.feed_title)) })

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            feedItems.forEach { event ->
                ActivityCard(
                    event        = event,
                    onMediaClick = { event.media?.id?.let(onMediaClick) },
                    onUserClick  = { event.user?.id?.let(onUserClick) }
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    event: ActivityEvent,
    onMediaClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onMediaClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick() },
                contentAlignment = Alignment.Center
            ) {
                if (event.user?.avatarUrl != null) {
                    AsyncImage(
                        model              = event.user.avatarUrl,
                        contentDescription = event.user.displayName,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                event.user?.displayName?.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.descriptionText(LocalContext.current),
                    style = MaterialTheme.typography.titleSmall)

                if (event.activityType == "review" && event.rating != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "★".repeat(event.rating) + "☆".repeat(5 - event.rating),
                        // ratings are amber per the spec, thats tertiary now
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    if (!event.reviewText.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(event.reviewText,
                            style   = MaterialTheme.typography.bodySmall,
                            color   = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(event.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (event.media?.coverUrl != null) {
                    AsyncImage(
                        model              = event.media.coverUrl,
                        contentDescription = event.media.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(when (event.media?.mediaType) {
                                "book"  -> "📖"
                                "movie" -> "🎬"
                                "show"  -> "📺"
                                else    -> "?"
                            }, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
