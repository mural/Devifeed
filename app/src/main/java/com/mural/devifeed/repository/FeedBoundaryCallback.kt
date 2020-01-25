package com.mural.devifeed.repository

import androidx.annotation.MainThread
import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import com.mural.devifeed.api.DevifeedApi
import com.mural.devifeed.api.ListingParser
import com.mural.devifeed.model.FeedPost
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class SubredditBoundaryCallback(
    private val webservice: DevifeedApi,
    private val handleResponse: (ListingParser?) -> Unit,
    private val ioExecutor: Executor,
    private val networkPageSize: Int
) : PagedList.BoundaryCallback<FeedPost>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.getTop(
                limit = networkPageSize
            )
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: FeedPost) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.getTopAfter(
                after = itemAtEnd.name,
                limit = networkPageSize
            )
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        response: Response<ListingParser>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: FeedPost) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<ListingParser> {
        return object : Callback<ListingParser> {
            override fun onFailure(
                call: Call<ListingParser>,
                t: Throwable
            ) {
                it.recordFailure(t)
            }

            override fun onResponse(
                call: Call<ListingParser>,
                response: Response<ListingParser>
            ) {
                insertItemsIntoDb(response, it)
            }
        }
    }
}