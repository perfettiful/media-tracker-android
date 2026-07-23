package edu.metrostate.ics342.mediatracker.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.LibraryResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    private val _libraryItems = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraryItems: StateFlow<List<LibraryItem>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    private val _filterState = MutableStateFlow(LibraryStatus.WANT_TO)
    val filterState: StateFlow<LibraryStatus> = _filterState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // the segmented control maps straight to the status query param
            when (val result = mediaRepository.getLibrary(_filterState.value)) {
                is LibraryResult.Success -> _libraryItems.value = result.items
                LibraryResult.NetworkError -> {
                    _libraryItems.value = emptyList()
                    _errorMessage.value = R.string.error_network
                }
                LibraryResult.UnknownError -> {
                    _libraryItems.value = emptyList()
                    _errorMessage.value = R.string.library_failed
                }
            }
            _isLoading.value = false
        }
    }

    // silent refetch for coming back to the tab after adding something on detail
    fun refresh() {
        if (_isLoading.value) return
        viewModelScope.launch {
            val result = mediaRepository.getLibrary(_filterState.value)
            if (result is LibraryResult.Success) _libraryItems.value = result.items
        }
    }

    fun updateFilter(status: LibraryStatus) {
        if (_filterState.value == status) return
        _filterState.value = status
        loadLibrary()
    }

    // next part of tonight wires PUT /library/{mediaId} and DELETE, inert until then
    fun removeItem(mediaId: Int) { }
    fun updateStatus(mediaId: Int, newStatus: LibraryStatus) { }
}
