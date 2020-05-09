package com.gmail.devu.study.mediabrowserservice.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gmail.devu.study.mediabrowserservice.R
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPlaying
import com.gmail.devu.study.mediabrowserservice.media.extensions.isSkipToNextEnabled
import com.gmail.devu.study.mediabrowserservice.media.extensions.isSkipToPreviousEnabled
import com.google.android.exoplayer2.ui.DefaultTimeBar
import kotlinx.android.synthetic.main.fragment_media_playback.view.*


/**
 * NOTE: This fragment is using the exo_playback_control_view layout of ExoPlayer UI
 * https://github.com/google/ExoPlayer/blob/release-v2/library/ui/src/main/res/layout/exo_playback_control_view.xml
 */
class MediaPlaybackFragment : Fragment() {
    private val TAG = this::class.java.simpleName

    private lateinit var viewModel: MediaPlaybackFragmentViewModel

    /**
     * Views on the exo_playback_control_view
     */
    private lateinit var playButton: View
    private lateinit var pauseButton: View
    private lateinit var prevButton: View
    private lateinit var nextButton: View
    private lateinit var durationView: TextView
    private lateinit var positionView: TextView
    private lateinit var timeBar: DefaultTimeBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate()")

        // Get a ViewModel
        val context = requireActivity()
        val factory = MediaPlaybackFragmentViewModel.Factory(
            context.applicationContext as Application,
            MediaPlaybackServiceClient.getInstance(context)
        )
        viewModel =
            ViewModelProvider(context, factory).get(MediaPlaybackFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated()")

        playButton = view.findViewById(R.id.exo_play)
        pauseButton = view.findViewById(R.id.exo_pause)
        prevButton = view.findViewById(R.id.exo_prev)
        nextButton = view.findViewById(R.id.exo_next)
        durationView = view.findViewById(R.id.exo_duration)
        positionView = view.findViewById(R.id.exo_position)
        timeBar = view.findViewById(R.id.exo_progress)

        viewModel.nowPlayingMetadata.observe(viewLifecycleOwner, Observer { metadata ->
            view.title.text = metadata.title
            view.artist.text = metadata.artist
            view.item_id.text = metadata.id
            durationView.text = activity?.let { timestampToMSS(it, metadata.duration) }
            timeBar.setDuration(metadata.duration)
        })

        viewModel.playbackState.observe(viewLifecycleOwner, Observer { state ->
            if (state.isPlaying == true) {
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            } else {
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }

            setButtonEnabled(prevButton, state.isSkipToPreviousEnabled)
            setButtonEnabled(nextButton, state.isSkipToNextEnabled)
        })

        viewModel.playbackPosition.observe(viewLifecycleOwner, Observer { position ->
            positionView.text = activity?.let { timestampToMSS(it, position) }
            timeBar.setPosition(position)
        })

        playButton.setOnClickListener {
            viewModel.play()
        }
        pauseButton.setOnClickListener {
            viewModel.pause()
        }
        prevButton.setOnClickListener {
            viewModel.prev()
        }
        nextButton.setOnClickListener {
            viewModel.next()
        }
    }

    private fun setButtonEnabled(view: View, enabled: Boolean) {
        // <integer name="exo_media_button_opacity_percentage_enabled">100</integer>
        // <integer name="exo_media_button_opacity_percentage_disabled">33</integer>
        view.isEnabled = enabled
        view.alpha = if (enabled) 1.0f else 0.33f
    }

    private fun timestampToMSS(context: Context, position: Long): String {
        val totalSeconds = Math.floor(position / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)
        return if (position < 0) context.getString(R.string.duration_unknown)
        else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
    }
}
