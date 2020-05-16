package com.gmail.devu.study.mediabrowserservice.media

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private val TAG = this::class.java.simpleName

    private val BROWSABLE_ROOT = "/"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat

    /**
     * ExoPlayer
     */
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            val attributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            setAudioAttributes(attributes, true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate()")

        // Create and initialize the media session
        mediaSession = MediaSessionCompat(this, this.TAG).apply {
            isActive = true
        }

        // Set the media session token to MediaBrowserServiceCompat
        sessionToken = mediaSession.sessionToken

        // Using MediaControllerCompat only to output the status change status to the log.
        mediaController = MediaControllerCompat(this, mediaSession).apply {
            registerCallback(MediaControllerCallback())
        }

        // Connect the media session and ExoPlayer
        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            val dataSourceFactory = DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "devu.study"),
                null
            )
            val playbackPreparer = MediaPlaybackPreparer(
                exoPlayer,
                dataSourceFactory
            )
            connector.setPlayer(exoPlayer)
            connector.setPlaybackPreparer(playbackPreparer)
            connector.setQueueNavigator(MyQueueNavigator(mediaSession))
        }
    }

    /**
     * Called to get the root information for browsing by a particular client.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.v(TAG, "onGetRoot(%s, %d)".format(clientPackageName, clientUid))
        return BrowserRoot(BROWSABLE_ROOT, null)
    }

    /**
     * Called to get information about the children of a media item.
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.v(TAG, "onLoadChildren(%s)".format(parentId))
        if (parentId == BROWSABLE_ROOT) {
            // This sample only supports root browsing
            val children = DummyMedia.ITEMS.map { item ->
                MediaBrowserCompat.MediaItem(
                    item.description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            }
            result.sendResult(children as MutableList<MediaBrowserCompat.MediaItem>?)
        } else {
            result.sendResult(null)
        }
    }

    /**
     * Called to get the search result.
     * But this sample does not support search
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        super.onSearch(query, extras, result)
        Log.v(TAG, "onLoadChildren(%s)".format(query))
        result.sendResult(null)
    }

    /**
     * For study the behavior, Just output the status change to log
     */
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.v(TAG, "onMetadataChanged(%s)".format(metadata?.description))
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.v(TAG, "onPlaybackStateChanged(%s)".format(state.toString()))
        }
    }

    /**
     * Helper class to retrieve the the Metadata necessary for the ExoPlayer MediaSession connection
     * extension to call [MediaSessionCompat.setMetadata].
     */
    private class MyQueueNavigator(mediaSession: MediaSessionCompat) :
        TimelineQueueNavigator(mediaSession) {
        private val window = Timeline.Window()

        /**
         * Gets the [MediaDescriptionCompat] for a given timeline window index.
         *
         *
         * Often artworks and icons need to be loaded asynchronously. In such a case, return a [ ] without the images, load your images asynchronously off the main thread
         * and then call [MediaSessionConnector.invalidateMediaSessionQueue] to make the connector
         * update the queue by calling [.getMediaDescription] again.
         *
         * @param player The current player.
         * @param windowIndex The timeline window index for which to provide a description.
         * @return A [MediaDescriptionCompat].
         */
        override fun getMediaDescription(
            player: Player,
            windowIndex: Int
        ): MediaDescriptionCompat {
            return player.currentTimeline.getWindow(
                windowIndex,
                window
            ).tag as MediaDescriptionCompat
        }
    }
}
