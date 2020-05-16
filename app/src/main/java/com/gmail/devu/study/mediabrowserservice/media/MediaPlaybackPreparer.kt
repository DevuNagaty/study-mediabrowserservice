package com.gmail.devu.study.mediabrowserservice.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.gmail.devu.study.mediabrowserservice.media.extensions.id
import com.gmail.devu.study.mediabrowserservice.media.extensions.toMediaSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DataSource

class MediaPlaybackPreparer(
    private val exoPlayer: ExoPlayer,
    private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {
    private val TAG = this::class.java.simpleName

    override fun getSupportedPrepareActions(): Long =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

    override fun onPrepare(playWhenReady: Boolean) {
        Log.v(TAG, "onPrepare()")
    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle) {
        Log.v(TAG, "onPrepareFromMediaId(%s, %b)".format(mediaId, playWhenReady))

        val metadataList: List<MediaMetadataCompat> = DummyMedia.ITEMS
        val mediaSource = metadataList.toMediaSource(dataSourceFactory)
        val initialWindowIndex = metadataList.indexOfFirst { it.id == mediaId }

        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(initialWindowIndex, 0)
        exoPlayer.setPlayWhenReady(playWhenReady)
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle) {
        Log.v(TAG, "onPrepareFromSearch(%s)".format(query))
    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle) {
        Log.v(TAG, "onPrepareFromUri(%s)".format(uri))
    }

    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {
        Log.v(TAG, "onCommand(%s, %s)".format(command, extras.toString()))
        return false
    }
}