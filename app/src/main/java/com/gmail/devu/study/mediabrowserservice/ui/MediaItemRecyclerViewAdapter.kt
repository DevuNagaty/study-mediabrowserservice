package com.gmail.devu.study.mediabrowserservice.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gmail.devu.study.mediabrowserservice.R
import com.gmail.devu.study.mediabrowserservice.ui.MediaItemFragment.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_media_item_list_row.view.*

class MediaItemRecyclerViewAdapter(
    private val mListener: OnListFragmentInteractionListener?
) : ListAdapter<MediaItemData, MediaItemRecyclerViewAdapter.ViewHolder>(MediaItemData.diffCallback) {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as MediaItemData
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onClicked(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_media_item_list_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.mIdView.text = item.id
        holder.mTitleView.text = item.title
        holder.mSubtitleView.text = item.artist

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_id
        val mTitleView: TextView = mView.title
        val mSubtitleView: TextView = mView.subtitle
    }
}
