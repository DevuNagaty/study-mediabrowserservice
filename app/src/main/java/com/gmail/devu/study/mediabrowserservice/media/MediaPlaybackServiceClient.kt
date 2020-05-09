package com.gmail.devu.study.mediabrowserservice.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData

class MediaPlaybackServiceClient(context: Context) {
    private val TAG = this::class.java.simpleName

    /**
     * Connection status with the MediaPlayback Service
     */
    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)
    }

    /**
     * MediaBrowserCompat
     */
    // This value is valid after connecting to MediaPlayback service
    val rootMediaId: String get() = mediaBrowser.root

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaPlaybackService::class.java),
        MediaBrowserConnectionCallback(context),
        null
    ).apply {
        // Connects to MediaPlayback service@MediaBrowserServiceCompat immediately upon instantiation
        connect()
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully completed.
         */
        override fun onConnected() {
            Log.v(TAG, "onConnected()")
            // Creates a [MediaControllerCompat] from a session token
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            isConnected.postValue(true)
        }

        /**
         * Invoked when a connection to the browser service has been lost.
         */
        override fun onConnectionSuspended() {
            Log.v(TAG, "onConnectionSuspended()")
            isConnected.postValue(false)
        }

        /**
         * Invoked when the connection to the media browser service failed.
         */
        override fun onConnectionFailed() {
            Log.v(TAG, "onConnectionFailed()")
            isConnected.postValue(false)
        }
    }

    /**
     * Queries for information about the media items that are contained within
     * the specified id and subscribes to receive updates when they change.
     */
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Log.v(TAG, "subscribe(%s)".format(parentId))
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Log.v(TAG, "unsubscribe(%s)".format(parentId))
        mediaBrowser.unsubscribe(parentId, callback)
    }

    /**
     * MediaControllerCompat
     */
    // The app subscribes to these live data to update metadata and status display
    val nowPlaying = MutableLiveData<MediaMetadataCompat>().apply {
        postValue(NOTHING_PLAYING)
    }
    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }

    // The app use this control for issuing a request of play, pause, etc
    val transportControls: MediaControllerCompat.TransportControls get() = mediaController.transportControls

    // Initialized when connection to MediaBrowserCompat is established.
    private lateinit var mediaController: MediaControllerCompat

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.v(TAG, "onMetadataChanged(%s)".format(metadata?.description))
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.v(TAG, "onPlaybackStateChanged(%s)".format(state.toString()))
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Log.v(TAG, "onQueueChanged(%s)".format(queue.toString()))
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            Log.v(TAG, "onSessionEvent(%s)".format(event.toString()))
        }
    }

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: MediaPlaybackServiceClient? = null
        fun getInstance(context: Context) = instance
            ?: synchronized(this) {
                instance
                    ?: MediaPlaybackServiceClient(
                        context
                    )
                        .also { instance = it }
            }
    }
}

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0L)
    .build()

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()
