package com.mural.devifeed.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.mural.devifeed.ItemListActivity
import com.mural.devifeed.repository.FeedRepository

class FeedViewModel(
    private val repository: FeedRepository,
    savedStateHandle: SavedStateHandle,
    var twoPane: Boolean,
    var parentActivity: ItemListActivity
) : ViewModel() {
    companion object {
        const val KEY_SUBREDDIT = "subreddit"
        const val DEFAULT_SUBREDDIT = "all"
    }

    init {
        if (!savedStateHandle.contains(KEY_SUBREDDIT)) {
            savedStateHandle.set(KEY_SUBREDDIT, DEFAULT_SUBREDDIT)
        }
    }

    private val repoResult = savedStateHandle.getLiveData<String>(KEY_SUBREDDIT).map {
        repository.posts(30)
    }
    val posts = this.repoResult.switchMap { it.pagedList }
    val refreshState = repoResult.switchMap { it.refreshState }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }
}