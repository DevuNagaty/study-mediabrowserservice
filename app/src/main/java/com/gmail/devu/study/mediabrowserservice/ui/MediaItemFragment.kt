package com.gmail.devu.study.mediabrowserservice.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gmail.devu.study.mediabrowserservice.R
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient

class MediaItemFragment : Fragment() {
    private val TAG = this::class.java.simpleName

    // Adapter call this listener
    interface OnListFragmentInteractionListener {
        fun onClicked(item: MediaItemData)
    }

    private lateinit var viewModel: MediaItemFragmentViewModel
    private lateinit var viewAdapter: MediaItemRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate()")

        // Get a ViewModel
        val context = requireActivity()
        val factory =
            MediaItemFragmentViewModel.Factory(MediaPlaybackServiceClient.getInstance(context))
        viewModel =
            ViewModelProvider(context, factory).get(MediaItemFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        val view = inflater.inflate(R.layout.fragment_media_item, container, false)

        // Set the adapter
        viewAdapter = MediaItemRecyclerViewAdapter(listener)
        if (view is RecyclerView) {
            with(view) {
                adapter = viewAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated()")

        viewModel.isReady.observe(viewLifecycleOwner, Observer { isReady ->
            if (isReady) {
                // Connected to the MediaBrowserServiceCompat
                // Call subscribe () to browse the media root
                viewModel.loadMediaItems()
            }
        })

        viewModel.mediaItems.observe(viewLifecycleOwner, Observer { items ->
            // MediaBrowserCompat.SubscriptionCallback of subscribe() is called
            viewAdapter.submitList(items)
        })
    }

    private val listener = object : OnListFragmentInteractionListener {
        override fun onClicked(item: MediaItemData) {
            // Item is selected
            viewModel.playMediaItem(item)
            findNavController().navigate(R.id.action_mediaItemFragment_to_mediaPlaybackFragment)
        }
    }
}
