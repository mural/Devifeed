package com.mural.devifeed

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mural.devifeed.adapter.FeedItemRecyclerViewAdapter
import com.mural.devifeed.api.DevifeedApi
import com.mural.devifeed.api.NetworkState
import com.mural.devifeed.db.DevifeedDatabase
import com.mural.devifeed.model.FeedPost
import com.mural.devifeed.repository.FeedRepository
import com.mural.devifeed.viewmodel.FeedViewModel
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*
import java.util.concurrent.Executors

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    private val database by lazy {
        DevifeedDatabase.create(applicationContext)
    }
    private val api by lazy {
        DevifeedApi.create()
    }
    private val DISK_IO = Executors.newSingleThreadExecutor()

    private val viewModel: FeedViewModel by viewModels {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                val repository = FeedRepository(
                    database = database,
                    api = api,
                    ioExecutor = DISK_IO
                )
                return FeedViewModel(repository, handle, twoPane, this@ItemListActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Dismiss all (not ready)", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        if (item_detail_container != null) {
            twoPane = true
        }

        setupRecyclerView(item_list)
        setupSwipeToRefresh()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val adapter = FeedItemRecyclerViewAdapter(viewModel)
        recyclerView.adapter = adapter
        viewModel.posts.observe(this, Observer<PagedList<FeedPost>> {
            adapter.submitList(it) {
                val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    recyclerView.scrollToPosition(position)
                }
            }
        })
    }

    private fun setupSwipeToRefresh() {
        viewModel.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
}
