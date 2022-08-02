package com.plcoding.spotifycloneyt.adapters

import android.util.Log
import androidx.recyclerview.widget.AsyncListDiffer
import com.plcoding.spotifycloneyt.R
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeSongAdapter : BaseSongAdapter(R.layout.swipe_item) {
    override var differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            Log.e("AdapterSwipe",song.toString())
            val text = song.title
            tvPrimary.text=text

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}