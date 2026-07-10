package edu.metrostate.ics342.mediatracker.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.DetailResult
import edu.metrostate.ics342.mediatracker.data.MediaRepository
import edu.metrostate.ics342.mediatracker.data.PostReviewResult
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val REVIEW_MAX_CHARS = 500

class WriteReviewViewModel(
    private val mediaRepository: MediaRepository = DefaultMediaRepository(),
) : ViewModel() {

    sealed class SubmitState {
        data object Idle : SubmitState()
        data object Submitting : SubmitState()
        data object Success : SubmitState()
        data class Error(val msgResId: Int) : SubmitState()
    }

    // just for the summary header, posting works even if this stays null
    private val _media = MutableStateFlow<MediaDetail?>(null)
    val media = _media.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating = _rating.asStateFlow()

    private val _reviewText = MutableStateFlow("")
    val reviewText = _reviewText.asStateFlow()

    private val _shareToFeed = MutableStateFlow(true)
    val shareToFeed = _shareToFeed.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState = _submitState.asStateFlow()

    private var loadedId: Int? = null

    fun load(mediaId: Int) {
        if (loadedId == mediaId) return
        loadedId = mediaId
        viewModelScope.launch {
            val result = mediaRepository.getMediaDetail(mediaId)
            if (result is DetailResult.Success) _media.value = result.detail
        }
    }

    fun onRatingChange(value: Int) {
        _rating.value = value
        clearError()
    }

    fun onReviewTextChange(value: String) {
        _reviewText.value = value.take(REVIEW_MAX_CHARS)
        clearError()
    }

    fun onShareToFeedChange(value: Boolean) { _shareToFeed.value = value }

    private fun clearError() {
        if (_submitState.value is SubmitState.Error) _submitState.value = SubmitState.Idle
    }

    fun onSubmit() {
        val id = loadedId ?: return
        // button is disabled until a star is picked, this is just the backstop
        if (_rating.value < 1) return
        if (_submitState.value is SubmitState.Submitting) return

        viewModelScope.launch {
            _submitState.value = SubmitState.Submitting
            _submitState.value = when (mediaRepository.postReview(
                mediaId     = id,
                rating      = _rating.value,
                reviewText  = _reviewText.value,
                shareToFeed = _shareToFeed.value,
            )) {
                PostReviewResult.Success         -> SubmitState.Success
                PostReviewResult.AlreadyReviewed -> SubmitState.Error(R.string.error_already_reviewed)
                PostReviewResult.NetworkError    -> SubmitState.Error(R.string.error_network)
                PostReviewResult.UnknownError    -> SubmitState.Error(R.string.error_review_failed)
            }
        }
    }
}
