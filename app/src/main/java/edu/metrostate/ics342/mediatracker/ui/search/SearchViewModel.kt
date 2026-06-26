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

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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

    private fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch() }
    }

    private suspend fun runSearch() {
        _isLoading.value = true
        _errorMessage.value = null
        // chips use "all" as a sentinel, the api wants the type left off
        val typeParam = _selectedType.value.takeIf { it != "all" }
        when (val page = mediaRepository.searchMedia(_query.value, typeParam, after = null)) {
            is SearchPage.Success   -> _results.value = page.items
            SearchPage.NetworkError -> { _results.value = emptyList(); _errorMessage.value = R.string.error_network }
            SearchPage.UnknownError -> { _results.value = emptyList(); _errorMessage.value = R.string.search_failed }
        }
        _isLoading.value = false
    }
}
