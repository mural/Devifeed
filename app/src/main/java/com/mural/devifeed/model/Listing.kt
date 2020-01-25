package com.mural.devifeed.model

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mural.devifeed.api.NetworkState

data class Listing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val refreshState: LiveData<NetworkState>,
    val refresh: () -> Unit,
    val retry: () -> Unit
)