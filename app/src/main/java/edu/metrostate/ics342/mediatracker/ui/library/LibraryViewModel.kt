package edu.metrostate.ics342.mediatracker.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.LibraryResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.PriorityLevel
import edu.metrostate.ics342.mediatracker.data.model.UpdatePriorityRequest
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

    // rollback complaints, shown as a snackbar not the full error screen
    private val _actionError = MutableStateFlow<Int?>(null)
    val actionError: StateFlow<Int?> = _actionError.asStateFlow()

    // the last thing removed, so the snackbar can offer an undo
    private val _undoCandidate = MutableStateFlow<LibraryItem?>(null)
    val undoCandidate: StateFlow<LibraryItem?> = _undoCandidate.asStateFlow()

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

    // optimistic: the item leaves the list before the server answers,
    // and comes back (at the end, good enough) if the call fails
    fun removeItem(mediaId: Int) {
        val backup = _libraryItems.value
        val removed = backup.find { it.mediaId == mediaId } ?: return
        _libraryItems.value = backup.filter { it.mediaId != mediaId }
        _undoCandidate.value = removed
        viewModelScope.launch {
            if (!mediaRepository.removeFromLibrary(mediaId)) {
                _libraryItems.value = backup
                _undoCandidate.value = null
                _actionError.value = R.string.library_remove_failed
            }
        }
    }

    // undo is just an optimistic add in the other direction
    fun undoRemove() {
        val removed = _undoCandidate.value ?: return
        _undoCandidate.value = null
        _libraryItems.value = _libraryItems.value + removed
        viewModelScope.launch {
            if (mediaRepository.addToLibrary(removed.mediaId, removed.status) == null) {
                _libraryItems.value = _libraryItems.value.filter { it.mediaId != removed.mediaId }
                _actionError.value = R.string.library_undo_failed
            }
        }
    }

    fun clearUndo() {
        _undoCandidate.value = null
    }

    // same trick. the list is server filtered by status, so a status change
    // means the item doesnt belong on this tab anymore
    fun updateStatus(mediaId: Int, newStatus: LibraryStatus) {
        val backup = _libraryItems.value
        val target = backup.find { it.mediaId == mediaId } ?: return
        if (target.status == newStatus) return
        _libraryItems.value = backup.filter { it.mediaId != mediaId }
        viewModelScope.launch {
            if (!mediaRepository.updateLibraryStatus(mediaId, newStatus)) {
                _libraryItems.value = backup
                _actionError.value = R.string.library_status_failed
            }
        }
    }

    // new entries go on the end of the list, an existing one keeps the spot it had.
    // PUT overwrites the whole row so hours and notes ride along every time
    fun setPriority(mediaId: Int, level: PriorityLevel, estimatedHours: Int?, notes: String?) {
        viewModelScope.launch {
            val existing = mediaRepository.getPriorities()
            val alreadyThere = existing?.find { it.mediaId == mediaId }
            val saved = mediaRepository.setPriority(
                UpdatePriorityRequest(
                    mediaId            = mediaId,
                    priority           = level.apiValue,
                    orderIndex         = alreadyThere?.orderIndex ?: (existing?.size ?: 0),
                    estimatedTimeHours = estimatedHours,
                    notes              = notes?.takeIf { it.isNotBlank() },
                )
            )
            _actionError.value = if (saved) R.string.priorities_saved else R.string.priorities_save_failed
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}
