package com.gmail.devu.study.mediabrowserservice.ui

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.devu.study.mediabrowserservice.media.EMPTY_PLAYBACK_STATE
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient
import com.gmail.devu.study.mediabrowserservice.media.extensions.*

class MediaPlaybackFragmentViewModel(
    private val app: Application,
    mediaPlaybackServiceClient: MediaPlaybackServiceClient
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    /**
     * Utility class used to represent the metadata necessary to display the media item currently being played.
     */
    data class NowPlayingMetadata(
        val id: String,
        val albumArtUri: Uri?,
        val title: String?,
        val artist: String?,
        val duration: Long
    ) {
        companion object {
            fun convert(metadata: MediaMetadataCompat): NowPlayingMetadata {
                return NowPlayingMetadata(
                    metadata.id,
                    null,
                    metadata.displayTitle,
                    metadata.displaySubtitle,
                    metadata.duration
                )
            }
        }
    }

    var nowPlayingMetadata = MutableLiveData<NowPlayingMetadata>()
    private val nowPlayingObserver = Observer<MediaMetadataCompat> {
        Log.v(TAG, "nowPlayingObserver(%s)".format(it?.description))
        if (it?.duration!! > 0L) {
            nowPlayingMetadata.postValue(NowPlayingMetadata.convert(it))
        }
    }

    var playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        Log.v(TAG, "playbackStateObserver(%s)".format(it?.toString()))
        playbackState.postValue(it)
    }

    val playbackPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }
    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private val client = mediaPlaybackServiceClient.also {
        it.nowPlaying.observeForever(nowPlayingObserver)
        it.playbackState.observeForever(playbackStateObserver)
        checkPlaybackPosition()
    }

    private val POSITION_UPDATE_INTERVAL_MILLIS = 100L
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val nowPosition = playbackState.value?.currentPlayBackPosition
        if (playbackPosition.value != nowPosition) {
            playbackPosition.postValue(nowPosition)
        }
        if (updatePosition) {
            checkPlaybackPosition()
        }
    }, POSITION_UPDATE_INTERVAL_MILLIS)

    /**
     * Control APIs
     */
    fun play() {
        client.transportControls.play()
    }

    fun pause() {
        client.transportControls.pause()
    }

    fun prev() {
        client.transportControls.skipToPrevious()
    }

    fun next() {
        client.transportControls.skipToNext()
    }

    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers
        client.nowPlaying.removeObserver(nowPlayingObserver)
        client.playbackState.removeObserver(playbackStateObserver)

        // Stop updating the position
        updatePosition = false

    }

    class Factory(
        private val app: Application,
        private val mediaPlaybackServiceClient: MediaPlaybackServiceClient
    ) :
        ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaPlaybackFragmentViewModel(app, mediaPlaybackServiceClient) as T
        }
    }
}
