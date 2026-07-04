package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.DetailResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaDetailViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    sealed class DetailUiState {
        data object Loading : DetailUiState()
        data class Loaded(val detail: MediaDetail, val reviews: List<Review>) : DetailUiState()
        data class Error(val msgResId: Int) : DetailUiState()
    }

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadedId: Int? = null

    fun load(mediaId: Int) {
        // screen recomposes plenty, only fetch when the id actually changes
        if (loadedId == mediaId) return
        loadedId = mediaId
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            _uiState.value = when (val result = mediaRepository.getMediaDetail(mediaId)) {
                is DetailResult.Success -> DetailUiState.Loaded(result.detail, result.reviews)
                DetailResult.NetworkError -> DetailUiState.Error(R.string.error_network)
                DetailResult.UnknownError -> DetailUiState.Error(R.string.detail_failed)
            }
        }
    }
}
