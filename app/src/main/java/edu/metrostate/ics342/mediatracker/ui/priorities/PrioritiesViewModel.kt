package edu.metrostate.ics342.mediatracker.ui.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Priority
import edu.metrostate.ics342.mediatracker.data.model.PriorityLevel
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
}
