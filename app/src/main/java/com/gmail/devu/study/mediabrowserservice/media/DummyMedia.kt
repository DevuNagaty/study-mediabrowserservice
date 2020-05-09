package com.gmail.devu.study.mediabrowserservice.media

import android.support.v4.media.MediaMetadataCompat
import java.util.concurrent.TimeUnit

object DummyMedia {
    val ITEMS: MutableList<MediaMetadataCompat> = mutableListOf()

    // Sample tracks
    // Use the sample music of UAMP (https://github.com/android/uamp)
    //   https://storage.googleapis.com/uamp/catalog.json
    private data class TrackInfo(var source: String, var image: String, var duration: Long) {
    }
    private val tracks = arrayOf(
        TrackInfo(  // #1
            "https://storage.googleapis.com/automotive-media/Drop_and_Roll.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            121),
        TrackInfo(  // #2
            "https://storage.googleapis.com/automotive-media/Motocross.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            182),
        TrackInfo(  // #3
            "https://storage.googleapis.com/automotive-media/Wish_You_d_Come_True.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            169),
        TrackInfo(  // #4
            "https://storage.googleapis.com/automotive-media/Awakening.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            220),
        TrackInfo(  // #5
            "https://storage.googleapis.com/automotive-media/Home.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            213),
        TrackInfo(  // #6
            "https://storage.googleapis.com/automotive-media/Tell_The_Angels.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            208),
        TrackInfo(  // #7
            "https://storage.googleapis.com/automotive-media/Hey_Sailor.mp3",
            "https://storage.googleapis.com/automotive-media/album_art_2.jpg",
            193)
    )

    // Dummy data
    private const val COUNT = 25

    init {
        for (i in 1..COUNT) {
            addMediaItem(createDummyMediaItem(i))
        }
    }

    private fun addMediaItem(item: MediaMetadataCompat) {
        ITEMS.add(item)
    }

    private fun createDummyMediaItem(index: Int): MediaMetadataCompat {
        val sample_track = tracks[(index - 1) % tracks.size]

        val title = "Dummy Media #%02d".format(index)
        val artist = "Unknown Artist"
        val album = "Unknown Album"
        val item = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "id%02d".format(index))
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                TimeUnit.SECONDS.toMillis(sample_track.duration))
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, sample_track.source)

            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Display: " + title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Display: " + artist)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, "Display: " + album)
            .build()
        return item
    }
}
