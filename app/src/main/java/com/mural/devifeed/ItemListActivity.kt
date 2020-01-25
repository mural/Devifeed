package com.mural.devifeed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mural.devifeed.api.DevifeedApi
import com.mural.devifeed.api.NetworkState
import com.mural.devifeed.databinding.ItemPostBinding
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
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(item_list)
        setupSwipeToRefresh()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val adapter = SimpleItemRecyclerViewAdapter(this, viewModel, twoPane)
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

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val viewModel: FeedViewModel,
        private val twoPane: Boolean
    ) :
        PagedListAdapter<FeedPost, SimpleItemRecyclerViewAdapter.PostViewHolder>(FEED_COMPARATOR) {

        init {
//            onClickListener = View.OnClickListener { v ->
//                val item = v.tag as DummyContent.DummyItem
//                if (twoPane) {
//                    val fragment = ItemDetailFragment().apply {
//                        arguments = Bundle().apply {
//                            putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
//                        }
//                    }
//                    parentActivity.supportFragmentManager
//                        .beginTransaction()
//                        .replace(R.id.item_detail_container, fragment)
//                        .commit()
//                } else {
//                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
//                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
//                    }
//                    v.context.startActivity(intent)
//                }
//            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemPostBinding.inflate(layoutInflater, parent, false)
            itemBinding.titleHandler = PostListHandler()

            return PostViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            if (super.getItemCount() > 0) {
                val item = getItem(position)
                holder.bind(item)
            }
        }

        override fun onBindViewHolder(
            holder: PostViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (!payloads.isNotEmpty()) {
                onBindViewHolder(holder, position)
            }
        }

        inner class PostViewHolder(private val binding: ItemPostBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: FeedPost?) {
                binding.post = item
                binding.model = viewModel
                binding.executePendingBindings()
            }
        }

        companion object {
            val FEED_COMPARATOR = object : DiffUtil.ItemCallback<FeedPost>() {
                override fun areContentsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean =
                    oldItem.id == newItem.id
            }
        }
    }

    open class PostListHandler {
        fun onClickItem(
            view: View,
            post: FeedPost,
            twoPane: Boolean,
            parentActivity: ItemListActivity
        ) {

            if (twoPane) {
                val fragment = ItemDetailFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ItemDetailFragment.ARG_ITEM_ID, post)
                    }
                }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(view.context, ItemDetailActivity::class.java).apply {
                    putExtra(ItemDetailFragment.ARG_ITEM_ID, post)
                }
                view.context.startActivity(intent)
            }
        }
    }
}
