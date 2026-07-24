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
            // favorites are separate from library on purpose
            val isFavorited: Boolean = false,
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
                    isFavorited   = mediaRepository.isFavorited(mediaId),
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
                    isFavorited   = mediaRepository.isFavorited(id),
                )
            }
        }
    }

    // optimistic toggle, the heart flips before the request even leaves.
    // flips back if the server disagrees
    fun onToggleSave() {
        val current = _uiState.value as? DetailUiState.Loaded ?: return
        val wasFavorited = current.isFavorited

        _uiState.value = current.copy(isFavorited = !wasFavorited)
        viewModelScope.launch {
            val confirmed = if (wasFavorited) mediaRepository.removeFavorite(current.detail.id)
                            else mediaRepository.addFavorite(current.detail.id)
            if (!confirmed) {
                _uiState.update { state ->
                    (state as? DetailUiState.Loaded)?.copy(isFavorited = wasFavorited) ?: state
                }
            }
        }
    }

    fun onAddWantTo() {
        val current = _uiState.value as? DetailUiState.Loaded ?: return
        if (current.libraryStatus != null) return

        // show the shelf right away, addToLibrary hands back null on a real
        // failure which rolls this back to the add button
        _uiState.value = current.copy(libraryStatus = LibraryStatus.WANT_TO)
        viewModelScope.launch {
            val confirmed = mediaRepository.addToLibrary(current.detail.id, LibraryStatus.WANT_TO)
            _uiState.update { state ->
                (state as? DetailUiState.Loaded)?.copy(libraryStatus = confirmed) ?: state
            }
        }
    }
}
