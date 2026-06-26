package edu.metrostate.ics342.mediatracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.SearchPage
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _selectedType = MutableStateFlow("all")
    val selectedType = _selectedType.asStateFlow()

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // loading the next page (vs the first page / a fresh search)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    // whether the server has more pages past what we've loaded
    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var nextCursor: String? = null
    private var searchJob: Job? = null

    init {
        // browse the catalog on first load
        search()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        // debounce so we dont fire a request on every keystroke
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            runSearch()
        }
    }

    fun onTypeChange(type: String) {
        if (_selectedType.value == type) return
        _selectedType.value = type
        search()
    }

    // grab the next page and append it, called when you scroll near the bottom
    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value || _isLoading.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val typeParam = _selectedType.value.takeIf { it != "all" }
            when (val page = mediaRepository.searchMedia(_query.value, typeParam, after = nextCursor)) {
                is SearchPage.Success -> {
                    _results.value = _results.value + page.items
                    nextCursor = page.nextCursor
                    _hasMore.value = page.hasMore
                }
                // a paging error just stops loading, keep what we already have
                SearchPage.NetworkError, SearchPage.UnknownError -> _hasMore.value = false
            }
            _isLoadingMore.value = false
        }
    }

    private fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch() }
    }

    private suspend fun runSearch() {
        _isLoading.value = true
        _errorMessage.value = null
        nextCursor = null
        _hasMore.value = false
        // chips use "all" as a sentinel, the api wants the type left off
        val typeParam = _selectedType.value.takeIf { it != "all" }
        when (val page = mediaRepository.searchMedia(_query.value, typeParam, after = null)) {
            is SearchPage.Success -> {
                _results.value = page.items
                nextCursor = page.nextCursor
                _hasMore.value = page.hasMore
            }
            SearchPage.NetworkError -> { _results.value = emptyList(); _errorMessage.value = R.string.error_network }
            SearchPage.UnknownError -> { _results.value = emptyList(); _errorMessage.value = R.string.search_failed }
        }
        _isLoading.value = false
    }
}
