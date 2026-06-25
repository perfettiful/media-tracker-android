package edu.metrostate.ics342.mediatracker.ui.search

import androidx.lifecycle.ViewModel
import edu.metrostate.ics342.mediatracker.data.fakeSearchResults
import edu.metrostate.ics342.mediatracker.data.model.Media
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedType = MutableStateFlow("all")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    fun onQueryChange(value: String) {
        _query.value = value
        runSearch()
    }

    fun onTypeChange(type: String) {
        _selectedType.value = type
        runSearch()
    }

    private fun runSearch() {
        val q = _query.value.trim()
        // nothing typed yet, keep the screen empty (default state)
        if (q.isBlank()) {
            _results.value = emptyList()
            return
        }
        // mock list for now, swap for GET /media once the interceptor is in
        _results.value = fakeSearchResults.filter { media ->
            (_selectedType.value == "all" || media.mediaType == _selectedType.value) &&
                media.title.contains(q, ignoreCase = true)
        }
    }
}
