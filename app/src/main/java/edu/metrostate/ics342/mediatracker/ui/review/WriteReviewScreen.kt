package edu.metrostate.ics342.mediatracker.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.ui.search.MediaTypeTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    viewModel: WriteReviewViewModel = viewModel()
) {
    val media       by viewModel.media.collectAsState()
    val rating      by viewModel.rating.collectAsState()
    val reviewText  by viewModel.reviewText.collectAsState()
    val shareToFeed by viewModel.shareToFeed.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(mediaId) {
        viewModel.load(mediaId)
    }

    LaunchedEffect(submitState) {
        if (submitState is WriteReviewViewModel.SubmitState.Success) {
            onNavigateBack()
        }
    }

    val isSubmitting = submitState is WriteReviewViewModel.SubmitState.Submitting
    val errorMsg = (submitState as? WriteReviewViewModel.SubmitState.Error)
        ?.msgResId?.let { stringResource(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.review_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_close))
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // media summary so you know what youre reviewing
            media?.let { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp, 64.dp).clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (m.coverUrl != null) {
                            AsyncImage(
                                model = m.coverUrl,
                                contentDescription = m.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            MediaTypeTile(m.mediaType)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(m.title, style = MaterialTheme.typography.titleSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(m.creatorCredit(LocalContext.current),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            StarRatingRow(
                rating = rating,
                onRatingChange = viewModel::onRatingChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = viewModel::onReviewTextChange,
                placeholder = { Text(stringResource(R.string.review_placeholder)) },
                minLines = 5,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                supportingText = {
                    Text(
                        stringResource(R.string.review_char_count, reviewText.length, REVIEW_MAX_CHARS),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = shareToFeed,
                    onCheckedChange = viewModel::onShareToFeedChange
                )
                Text(stringResource(R.string.review_share_feed),
                    style = MaterialTheme.typography.bodyMedium)
            }

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::onSubmit,
                // no rating no review, and no double fire while ones posting
                enabled = rating >= 1 && !isSubmitting,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.review_post))
                }
            }
        }
    }
}

// 5 tappable stars, tap star n to set rating n. reusable per the week 8 spec
@Composable
fun StarRatingRow(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChange(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
