package edu.metrostate.ics342.mediatracker.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMediaClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val query        by viewModel.query.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val results      by viewModel.results.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.app_name)) })

        OutlinedTextField(
            value         = query,
            onValueChange = viewModel::onQueryChange,
            placeholder   = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon   = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine    = true,
            shape         = RoundedCornerShape(28.dp),
            modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        MediaTypeFilterChips(
            selectedType = selectedType,
            onTypeSelect = viewModel::onTypeChange,
            modifier     = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when {
            isLoading && results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && results.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(errorMessage!!),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            query.isNotBlank() && results.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.search_no_results, query),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Text(
                    when {
                        query.isBlank()   -> stringResource(R.string.search_browse)
                        results.size == 1 -> stringResource(R.string.search_result_count, results.size)
                        else              -> stringResource(R.string.search_results_count, results.size)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SearchResultsList(
                    results      = results,
                    listState    = listState,
                    onMediaClick = onMediaClick
                )
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<Media>,
    listState: LazyListState,
    onMediaClick: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { media ->
                SearchResultCard(media = media, onClick = { onMediaClick(media.id) })
            }
        }

        // float a down chevron while theres more below, fade it out at the bottom
        AnimatedVisibility(
            visible  = listState.canScrollForward,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            Surface(
                shape           = CircleShape,
                color           = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
                onClick = {
                    scope.launch {
                        val target = (listState.firstVisibleItemIndex + 4).coerceAtMost(results.lastIndex)
                        listState.animateScrollToItem(target)
                    }
                }
            ) {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.search_scroll_more),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(6.dp).size(24.dp)
                )
            }
        }
    }
}
