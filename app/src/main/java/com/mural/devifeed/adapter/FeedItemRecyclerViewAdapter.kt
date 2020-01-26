package com.mural.devifeed.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mural.devifeed.ItemDetailActivity
import com.mural.devifeed.ItemDetailFragment
import com.mural.devifeed.ItemListActivity
import com.mural.devifeed.R
import com.mural.devifeed.databinding.ItemPostBinding
import com.mural.devifeed.model.FeedPost
import com.mural.devifeed.viewmodel.FeedViewModel
import java.util.*

class FeedItemRecyclerViewAdapter(private val viewModel: FeedViewModel) :
    PagedListAdapter<FeedPost, FeedItemRecyclerViewAdapter.PostViewHolder>(FEED_COMPARATOR) {

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
        if (payloads.isEmpty()) {
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

        @BindingAdapter("profileImage")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String) {
            Glide.with(view.context)
                .load(imageUrl).apply(RequestOptions().circleCrop())
                .into(view)
        }

        @BindingAdapter("timeAgo")
        @JvmStatic
        fun TextView.setDateText(timeAgo: Long) {
            val timeDifInSeconds = (Date().time / 1000) - timeAgo
            val timeDifInHours = timeDifInSeconds / 3600 //hour has 3600 secs
            text = when (timeDifInHours) {
                0L -> resources.getString(R.string.post_time_recent)
                else -> resources.getString(R.string.post_time_hours, timeDifInHours.toString())
            }
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