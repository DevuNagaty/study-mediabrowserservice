package com.gmail.devu.study.mediabrowserservice.ui

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.lifecycle.*
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient
import com.gmail.devu.study.mediabrowserservice.media.extensions.id
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPlayEnabled
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPlaying
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPrepared

class MediaItemFragmentViewModel(private val client: MediaPlaybackServiceClient) : ViewModel() {
    private val TAG = this::class.java.simpleName

    val isReady: LiveData<Boolean> = Transformations.map(client.isConnected) { isConnected ->
        isConnected
    }

    private val _mediaItems = MutableLiveData<List<MediaItemData>>()
    val mediaItems: LiveData<List<MediaItemData>> = _mediaItems

    val playingMedia: MutableLiveData<MediaItemData>? = null

    /**
     * Browse media root
     */
    fun loadMediaItems() {
        Log.v(TAG, "loadMediaItmes()")
        client.subscribe(client.rootMediaId, subscriptionCallback)
    }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Log.v(TAG, "onChildrenLoaded(%s)".format(parentId))
            val items = children.map { child ->
                MediaItemData(
                    child.mediaId!!,
                    child.description.title.toString(),
                    child.description.subtitle.toString()
                )
            }
            _mediaItems.postValue(items)
        }
    }

    /**
     * Start playback
     */
    fun playMediaItem(mediaItem: MediaItemData) {
        Log.v(TAG, "playMediaItem(%s)".format(mediaItem.id))
        val nowPlaying = client.nowPlaying.value
        val transportControls = client.transportControls

        val isPrepared = client.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.id == nowPlaying?.id) {
            client.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(TAG, "Playable item clicked but neither play nor pause are enabled!")
                    }
                }
            }
        } else {
            Log.v(TAG, "call playFromMediaId(%s)".format(mediaItem.id))
            transportControls.playFromMediaId(mediaItem.id, null)
        }

        if (playingMedia != null) {
            playingMedia.postValue(mediaItem)
        }
    }

    override fun onCleared() {
        Log.v(TAG, "onCleared()")
        super.onCleared()
        client.unsubscribe(client.rootMediaId, subscriptionCallback)
    }

    class Factory(private val mediaPlaybackServiceClient: MediaPlaybackServiceClient) :
        ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaItemFragmentViewModel(mediaPlaybackServiceClient) as T
        }
    }
}
