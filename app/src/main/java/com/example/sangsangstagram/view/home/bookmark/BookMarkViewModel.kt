package com.example.sangsangstagram.view.home.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.sangsangstagram.R
import com.example.sangsangstagram.data.AuthRepository
import com.example.sangsangstagram.data.PostRepository
import com.example.sangsangstagram.view.home.post.PostItemUiState
import com.example.sangsangstagram.view.home.post.PostListUiState
import com.example.sangsangstagram.view.home.post.toUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookMarkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostListUiState(currentUserUuid = requireNotNull(AuthRepository.currentUserUuid))
    )
    val uiState = _uiState.asStateFlow()

    private var bounded = false

    fun bind(
        initPostPagingData: PagingData<PostItemUiState>?
    ) {
        if (bounded) return
        bounded = true
        if (initPostPagingData != null) {
            _uiState.update { it.copy(pagingData = initPostPagingData) }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val pagingFlow = PostRepository.getBookMarkFeeds()
            pagingFlow.cachedIn(viewModelScope)
                .collect { pagingData ->
                    _uiState.update { uiState ->
                        uiState.copy(pagingData = pagingData.map { it.toUiState() })
                    }
                }
        }
    }

    fun toggleBookMark(postUuid: String) {
        viewModelScope.launch {
            val result = PostRepository.toggleBookMark(postUuid)
            if (result.isFailure) {
                _uiState.update { it.copy(userMessage = R.string.failed) }
            }
        }
    }

    fun toggleLike(postUuid: String) {
        viewModelScope.launch {
            val result = PostRepository.toggleLike(postUuid)
            if (result.isFailure) {
                _uiState.update { it.copy(userMessage = R.string.failed) }
            }
        }
    }

    fun deleteSelectedPost(uiState: PostItemUiState) {
        viewModelScope.launch {
            check(true)
            val result = PostRepository.deletePost(uiState.uuid)
            _uiState.update {
                it.copy(
                    userMessage = if (result.isSuccess) {
                        R.string.post_deleted
                    } else {
                        R.string.failed
                    }
                )
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}