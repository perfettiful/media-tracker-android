package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.DetailResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaDetailViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    sealed class DetailUiState {
        data object Loading : DetailUiState()
        data class Loaded(
            val detail: MediaDetail,
            val reviews: List<Review>,
            // null means not in the library yet
            val libraryStatus: LibraryStatus? = null,
            val isAddingToLibrary: Boolean = false,
        ) : DetailUiState()
        data class Error(val msgResId: Int) : DetailUiState()
    }

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadedId: Int? = null

    // error state retry, drop the guard so load actually refetches
    fun retry() {
        val id = loadedId ?: return
        loadedId = null
        load(id)
    }

    fun load(mediaId: Int) {
        // screen recomposes plenty, only fetch when the id actually changes
        if (loadedId == mediaId) return
        loadedId = mediaId
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            _uiState.value = when (val result = mediaRepository.getMediaDetail(mediaId)) {
                is DetailResult.Success -> DetailUiState.Loaded(
                    detail        = result.detail,
                    reviews       = result.reviews,
                    libraryStatus = mediaRepository.getLibraryStatus(mediaId),
                )
                DetailResult.NetworkError -> DetailUiState.Error(R.string.error_network)
                DetailResult.UnknownError -> DetailUiState.Error(R.string.detail_failed)
            }
        }
    }

    // silent refetch for coming back from write review, keeps showing the old
    // content while the new copy loads and only swaps on success
    fun refresh() {
        val id = loadedId ?: return
        if (_uiState.value !is DetailUiState.Loaded) return
        viewModelScope.launch {
            val result = mediaRepository.getMediaDetail(id)
            if (result is DetailResult.Success) {
                _uiState.value = DetailUiState.Loaded(
                    detail        = result.detail,
                    reviews       = result.reviews,
                    libraryStatus = mediaRepository.getLibraryStatus(id),
                )
            }
        }
    }

    fun onAddWantTo() {
        val current = _uiState.value as? DetailUiState.Loaded ?: return
        // already in the library, or a request is mid flight, dont fire another
        if (current.libraryStatus != null || current.isAddingToLibrary) return

        _uiState.update { (it as DetailUiState.Loaded).copy(isAddingToLibrary = true) }
        viewModelScope.launch {
            val newStatus = mediaRepository.addToLibrary(current.detail.id, LibraryStatus.WANT_TO)
            _uiState.update { state ->
                (state as? DetailUiState.Loaded)?.copy(
                    libraryStatus     = newStatus,
                    isAddingToLibrary = false,
                ) ?: state
            }
        }
    }
}
