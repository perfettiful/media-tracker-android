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

    init {
        // show something on first load instead of a blank screen
        runSearch()
    }

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
        // blank query falls back to the popular list for the selected chip,
        // otherwise filter by title. sort by rating count, star rating breaks ties.
        _results.value = fakeSearchResults
            .filter { _selectedType.value == "all" || it.mediaType == _selectedType.value }
            .filter { q.isBlank() || it.title.contains(q, ignoreCase = true) }
            .sortedWith(compareByDescending<Media> { it.ratingCount }.thenByDescending { it.averageRating })
    }
}
