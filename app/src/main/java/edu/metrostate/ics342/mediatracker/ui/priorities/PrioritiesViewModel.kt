package edu.metrostate.ics342.mediatracker.ui.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Priority
import edu.metrostate.ics342.mediatracker.data.model.PriorityLevel
import edu.metrostate.ics342.mediatracker.data.model.UpdatePriorityRequest
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrioritiesViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    private val _priorities = MutableStateFlow<List<Priority>>(emptyList())
    val priorities: StateFlow<List<Priority>> = _priorities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    // a failed reorder shouldnt wipe the list, snackbar instead of the error screen
    private val _actionError = MutableStateFlow<Int?>(null)
    val actionError: StateFlow<Int?> = _actionError.asStateFlow()

    fun clearActionError() {
        _actionError.value = null
    }

    // null is the All chip
    private val _levelFilter = MutableStateFlow<PriorityLevel?>(null)
    val levelFilter: StateFlow<PriorityLevel?> = _levelFilter.asStateFlow()

    init {
        loadPriorities()
    }

    fun loadPriorities() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val loaded = mediaRepository.getPriorities()
            if (loaded != null) {
                _priorities.value = loaded
            } else {
                _priorities.value = emptyList()
                _errorMessage.value = R.string.priorities_failed
            }
            _isLoading.value = false
        }
    }

    fun refresh() {
        if (_isLoading.value) return
        viewModelScope.launch {
            mediaRepository.getPriorities()?.let { _priorities.value = it }
        }
    }

    fun updateFilter(level: PriorityLevel?) {
        _levelFilter.value = level
    }

    // what the list looked like before the drag started, so a failed save can undo it
    private var orderBeforeDrag: List<Priority>? = null

    fun beginDrag() {
        orderBeforeDrag = _priorities.value
    }

    // local only, the drag calls this a lot. the save happens once on drop
    fun moveItem(from: Int, to: Int) {
        val current = _priorities.value.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        current.add(to, current.removeAt(from))
        _priorities.value = current
    }

    fun saveOrder() {
        val backup = orderBeforeDrag ?: return
        orderBeforeDrag = null
        val reordered = _priorities.value
        if (reordered.map { it.mediaId } == backup.map { it.mediaId }) return

        // renumber top to bottom, the api takes one row at a time so this is a few calls
        val renumbered = reordered.mapIndexed { index, item -> item.copy(orderIndex = index) }
        _priorities.value = renumbered
        viewModelScope.launch {
            val moved = renumbered.filterIndexed { index, item ->
                backup.getOrNull(index)?.mediaId != item.mediaId
            }
            val allSaved = moved.all { item ->
                mediaRepository.setPriority(
                    UpdatePriorityRequest(
                        mediaId            = item.mediaId,
                        priority           = item.priority,
                        orderIndex         = item.orderIndex,
                        estimatedTimeHours = item.estimatedTimeHours,
                        notes              = item.notes,
                    )
                )
            }
            if (!allSaved) {
                _priorities.value = backup
                _actionError.value = R.string.priorities_reorder_failed
            }
        }
    }
}
