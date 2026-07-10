package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.ui.StatusBadge
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    onWriteReview: (Int) -> Unit,
    viewModel: MediaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mediaId) {
        viewModel.load(mediaId)
    }

    // refetch when we come back into view, e.g. after posting a review.
    // refresh() no-ops until the first load is done so this doesnt double fetch
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
            title = {},
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: overflow menu */ }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options)
                    )
                }
            }
        )

        when (val state = uiState) {
            is MediaDetailViewModel.DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MediaDetailViewModel.DetailUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(state.msgResId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        shape   = RoundedCornerShape(20.dp)
                    ) { Text(stringResource(R.string.action_retry)) }
                }
            }

            is MediaDetailViewModel.DetailUiState.Loaded -> {
                DetailContent(
                    detail        = state.detail,
                    reviews       = state.reviews,
                    libraryStatus = state.libraryStatus,
                    isAdding      = state.isAddingToLibrary,
                    onAddWantTo   = viewModel::onAddWantTo,
                    onWriteReview = onWriteReview
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: MediaDetail,
    reviews: List<Review>,
    libraryStatus: LibraryStatus?,
    isAdding: Boolean,
    onAddWantTo: () -> Unit,
    onWriteReview: (Int) -> Unit
) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MediaCover(detail)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = detail.creatorCredit(LocalContext.current),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))
                RatingSummary(
                    averageRating = detail.averageRating,
                    ratingCount = detail.ratingCount
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (libraryStatus != null) {
                    // already in the library, show which shelf instead of the add button
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StatusBadge(status = libraryStatus)
                    }
                } else {
                    Button(
                        onClick = onAddWantTo,
                        enabled = !isAdding,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.detail_add_want_to))
                        }
                    }
                }
                OutlinedButton(
                    onClick = { /* TODO: save */ },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.detail_save))
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!detail.description.isNullOrBlank()) {
                SectionCaption(stringResource(R.string.detail_about))
                Spacer(Modifier.height(6.dp))
                Text(
                    text = detail.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }

            StatGrid(detail)

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionCaption(
                    text = stringResource(R.string.detail_reviews_count, detail.reviewCount),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onWriteReview(detail.id) }) {
                    Text(stringResource(R.string.detail_write_review))
                }
            }

            Spacer(Modifier.height(4.dp))

            if (reviews.isEmpty()) {
                Text(
                    text = stringResource(R.string.detail_no_reviews),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                reviews.forEach { review ->
                    ReviewCard(review)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

@Composable
private fun MediaCover(detail: MediaDetail) {
    val containerColor = when (detail.mediaType) {
        "book"  -> MaterialTheme.colorScheme.primaryContainer
        "movie" -> MaterialTheme.colorScheme.secondaryContainer
        else    -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val iconTint = when (detail.mediaType) {
        "book"  -> MaterialTheme.colorScheme.onPrimaryContainer
        "movie" -> MaterialTheme.colorScheme.onSecondaryContainer
        else    -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    val placeholder = when (detail.mediaType) {
        "book"  -> Icons.Outlined.MenuBook
        "movie" -> Icons.Outlined.Movie
        else    -> Icons.Outlined.Tv
    }

    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (detail.coverUrl != null) {
            AsyncImage(
                model = detail.coverUrl,
                contentDescription = detail.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = placeholder,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = iconTint
            )
        }
    }
}

@Composable
private fun RatingSummary(averageRating: Float, ratingCount: Int) {
    if (ratingCount <= 0) {
        Text(
            text = stringResource(R.string.detail_not_yet_rated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        StarRow(rating = averageRating)
        Spacer(Modifier.width(6.dp))
        Text(
            text = "%.1f".format(averageRating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "(${"%,d".format(ratingCount)})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 5 stars (full / half / empty) for a 0-5 rating, amber per the design spec
@Composable
private fun StarRow(rating: Float, starSize: Int = 16) {
    val rounded = (rating * 2).roundToInt()          // nearest half-star
    Row {
        for (i in 1..5) {
            val icon = when {
                rounded >= i * 2     -> Icons.Filled.Star
                rounded == i * 2 - 1 -> Icons.Outlined.StarHalf
                else                 -> Icons.Outlined.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(starSize.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatGrid(detail: MediaDetail) {
    val stats = buildList {
        detail.publishedYear?.let { add(stringResource(R.string.detail_stat_year) to it.toString()) }
        when (detail.mediaType) {
            "book" -> detail.pageCount?.let {
                add(stringResource(R.string.detail_stat_pages) to it.toString())
            }
            "movie" -> detail.runtimeMinutes?.let {
                add(stringResource(R.string.detail_stat_runtime) to stringResource(R.string.detail_runtime_minutes, it))
            }
            "show" -> detail.seasonCount?.let {
                add(stringResource(R.string.detail_stat_seasons) to it.toString())
            }
        }
        detail.genres.firstOrNull()?.let {
            add(stringResource(R.string.detail_stat_genre) to it)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { (label, value) ->
            StatBox(label = label, value = value, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val displayName = review.user?.displayName ?: "?"
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = review.user?.username?.let { "@$it" } ?: displayName,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = relativeTime(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                StarRow(rating = review.rating.toFloat(), starSize = 14)
                if (!review.reviewText.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = review.reviewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// minimal iso-8601 to date label, real relative time comes with the api wiring
private fun relativeTime(iso: String): String = iso.take(10)
