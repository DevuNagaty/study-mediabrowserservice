package com.gmail.devu.study.mediabrowserservice.ui

import androidx.recyclerview.widget.DiffUtil

data class MediaItemData(
    val id: String,
    val title: String,
    val artist: String
) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MediaItemData>() {
            override fun areItemsTheSame(oldItem: MediaItemData, newItem: MediaItemData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MediaItemData,
                newItem: MediaItemData
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}