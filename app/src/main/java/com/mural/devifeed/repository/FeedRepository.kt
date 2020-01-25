package com.mural.devifeed.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.toLiveData
import com.mural.devifeed.api.DevifeedApi
import com.mural.devifeed.api.ListingParser
import com.mural.devifeed.api.NetworkState
import com.mural.devifeed.db.DevifeedDatabase
import com.mural.devifeed.model.FeedPost
import com.mural.devifeed.model.Listing
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class FeedRepository(
    val database: DevifeedDatabase,
    private val api: DevifeedApi,
    private val ioExecutor: Executor,
    private val networkPageSize: Int = NETWORK_PAGE_SIZE
) {
    companion object {
        private const val NETWORK_PAGE_SIZE = 15
    }

    private fun insertResultIntoDatabase(body: ListingParser?) {
        body!!.data.children.let { posts ->
            database.runInTransaction {
                val start = database.getFeedDao().getNextIndex()
                val items = posts.mapIndexed { index, child ->
                    child.data.topIndex = start + index
                    child.data
                }
                database.getFeedDao().insert(items)
            }
        }
    }

    @MainThread
    private fun refresh(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        api.getTop(networkPageSize).enqueue(
            object : Callback<ListingParser> {
                override fun onFailure(call: Call<ListingParser>, t: Throwable) {
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(
                    call: Call<ListingParser>,
                    response: Response<ListingParser>
                ) {
                    ioExecutor.execute {
                        database.runInTransaction {
                            database.getFeedDao().deleteAll()
                            insertResultIntoDatabase(response.body())
                        }
                        networkState.postValue(NetworkState.LOADED)
                    }
                }
            }
        )
        return networkState
    }

    @MainThread
    fun posts(pageSize: Int): Listing<FeedPost> {
        val boundaryCallback = SubredditBoundaryCallback(
            webservice = api,
            handleResponse = this::insertResultIntoDatabase,
            ioExecutor = ioExecutor,
            networkPageSize = networkPageSize
        )
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = refreshTrigger.switchMap {
            refresh()
        }

        val livePagedList = database.getFeedDao().list()
            .toLiveData(pageSize = pageSize, boundaryCallback = boundaryCallback)

        return Listing(
            pagedList = livePagedList,
            retry = { boundaryCallback.helper.retryAllFailed() },
            refresh = { refreshTrigger.value = null },
            refreshState = refreshState
        )
    }
}