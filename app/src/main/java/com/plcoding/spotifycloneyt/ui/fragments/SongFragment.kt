package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.entities.Song
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import com.plcoding.spotifycloneyt.exoplayer.toSong
import com.plcoding.spotifycloneyt.others.Status
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment: Fragment(R.layout.fragment_song){

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var currPlayingSong: Song?= null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel=ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        subscribeToObservers()

        ivPlayPauseDetail.setOnClickListener {
            currPlayingSong?.let {
                mainViewModel.playOrToggleSing(it,true)
            }
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
               seekBar?.let {
                   mainViewModel.seekTo(it.progress.toLong())
                   shouldUpdateSeekbar = true
               }
            }
        })
    }

    private fun updateTitleAndSongImage(song: Song){
        val title = song.title
        tvSongName.text=title
        glide.load(song.genre).into(ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
           it?.let {
               when(it.status){
                   Status.SUCCESS -> {
                       it.data?.let { songs ->
                           if(currPlayingSong == null && songs.isNotEmpty()){
                               currPlayingSong = songs[0]
                               updateTitleAndSongImage(songs[0])
                           }
                       }


                   }
                   else -> Unit

               }
           }
        }

        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner) {
           if(it==null) return@observe
            currPlayingSong = it.toSong()
            updateTitleAndSongImage(currPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){

            playbackState=it
            ivPlayPauseDetail.setImageResource(
             if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )

            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {

            if(shouldUpdateSeekbar) {
               seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max=it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text=dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text= dateFormat.format(ms)
    }




}