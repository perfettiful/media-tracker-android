package edu.metrostate.ics342.mediatracker.ui.priorities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Priority
import edu.metrostate.ics342.mediatracker.data.model.PriorityLevel
import edu.metrostate.ics342.mediatracker.theme.Finished
import edu.metrostate.ics342.mediatracker.theme.FinishedContainer
import edu.metrostate.ics342.mediatracker.theme.InProgress
import edu.metrostate.ics342.mediatracker.theme.InProgressContainer
import edu.metrostate.ics342.mediatracker.theme.PriorityHighDot
import edu.metrostate.ics342.mediatracker.theme.PriorityLowDot
import edu.metrostate.ics342.mediatracker.theme.PriorityMediumDot
import edu.metrostate.ics342.mediatracker.theme.WantTo
import edu.metrostate.ics342.mediatracker.theme.WantToContainer
import edu.metrostate.ics342.mediatracker.ui.search.MediaTypeTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritiesScreen(
    onNavigateBack: () -> Unit,
    onMediaClick: (Int) -> Unit,
    viewModel: PrioritiesViewModel = viewModel()
) {
    val priorities   by viewModel.priorities.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val levelFilter  by viewModel.levelFilter.collectAsState()

    // someone may have set a priority from the library while we were away
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.priorities_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.action_back))
                }
            }
        )

        PriorityFilterChips(
            selected   = levelFilter,
            onSelect   = viewModel::updateFilter,
            modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(errorMessage!!),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.loadPriorities() }, shape = RoundedCornerShape(20.dp)) {
                    Text(stringResource(R.string.action_retry))
                }
            }
            return@Column
        }

        val shown = priorities.filter { levelFilter == null || PriorityLevel.from(it.priority) == levelFilter }

        if (shown.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.priorities_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shown, key = { it.mediaId }) { priority ->
                PriorityCard(priority = priority, onClick = { onMediaClick(priority.mediaId) })
            }
        }
    }
}

@Composable
private fun PriorityFilterChips(
    selected: PriorityLevel?,
    onSelect: (PriorityLevel?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null,
            onClick  = { onSelect(null) },
            shape    = RoundedCornerShape(8.dp),
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            label    = { Text(stringResource(R.string.filter_all)) }
        )
        PriorityLevel.entries.forEach { level ->
            FilterChip(
                selected = selected == level,
                onClick  = { onSelect(level) },
                shape    = RoundedCornerShape(8.dp),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                leadingIcon = {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(level.dotColor()))
                },
                label = { Text(stringResource(level.shortLabelRes())) }
            )
        }
    }
}

@Composable
private fun PriorityCard(priority: Priority, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(64.dp, 90.dp).clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                val cover = priority.media?.coverUrl
                if (!cover.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model              = cover,
                        contentDescription = priority.media?.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                        loading            = { MediaTypeTile(priority.media?.mediaType ?: "book") },
                        error              = { MediaTypeTile(priority.media?.mediaType ?: "book") },
                    )
                } else {
                    MediaTypeTile(priority.media?.mediaType ?: "book")
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    priority.media?.title ?: stringResource(R.string.priorities_unknown_title),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                PriorityBadge(PriorityLevel.from(priority.priority))

                val meta = priorityMeta(priority)
                if (meta != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(level: PriorityLevel) {
    val (container, content) = level.badgeColors()
    Text(
        text  = stringResource(level.badgeLabelRes()),
        style = MaterialTheme.typography.labelSmall,
        color = content,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// "Est. 6 hours . "Friend recommended"" per the wireframe, either half can be missing
@Composable
private fun priorityMeta(priority: Priority): String? {
    val hours = priority.estimatedTimeHours?.let { stringResource(R.string.priorities_est_hours, it) }
    val note  = priority.notes?.takeIf { it.isNotBlank() }?.let { "\"$it\"" }
    return listOfNotNull(hours, note).takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun PriorityLevel.dotColor(): Color = when (this) {
    PriorityLevel.HIGH   -> PriorityHighDot
    PriorityLevel.MEDIUM -> PriorityMediumDot
    PriorityLevel.LOW    -> PriorityLowDot
}

// badges reuse the library status palette, thats what the wireframe draws
private fun PriorityLevel.badgeColors(): Pair<Color, Color> = when (this) {
    PriorityLevel.HIGH   -> WantToContainer to WantTo
    PriorityLevel.MEDIUM -> InProgressContainer to InProgress
    PriorityLevel.LOW    -> FinishedContainer to Finished
}

private fun PriorityLevel.shortLabelRes(): Int = when (this) {
    PriorityLevel.HIGH   -> R.string.priority_high
    PriorityLevel.MEDIUM -> R.string.priority_medium
    PriorityLevel.LOW    -> R.string.priority_low
}

private fun PriorityLevel.badgeLabelRes(): Int = when (this) {
    PriorityLevel.HIGH   -> R.string.priority_badge_high
    PriorityLevel.MEDIUM -> R.string.priority_badge_medium
    PriorityLevel.LOW    -> R.string.priority_badge_low
}
