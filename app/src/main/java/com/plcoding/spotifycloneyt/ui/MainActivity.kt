package com.plcoding.spotifycloneyt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeSongAdapter
import com.plcoding.spotifycloneyt.entities.Song
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import com.plcoding.spotifycloneyt.exoplayer.toSong
import com.plcoding.spotifycloneyt.others.Status
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentlyPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscribeTOObservers()
        vpSong.adapter=swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying==true){
                    mainViewModel.playOrToggleSing(swipeSongAdapter.songs[position])
                }else{
                    currentlyPlayingSong=swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSing(it,true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
               R.id.globalActionToSongFragment
            )
        }
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()

                else -> showBottomBar()
            }
        }
    }

    private fun hideBottomBar() {
       ivCurSongImage.isVisible=false
       vpSong.isVisible=false
       ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        ivCurSongImage.isVisible=true
        vpSong.isVisible=true
        ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToSong(song: Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)

        if(newItemIndex !=-1){
            vpSong.currentItem=newItemIndex
            currentlyPlayingSong=song
        }
    }

    private fun subscribeTOObservers() {
        mainViewModel.mediaItems.observe(this){
            it?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {

                        result.data?.let { songs ->
                           swipeSongAdapter.songs=songs
                            if(songs.isNotEmpty()){
                                Log.e("bakwas",songs[0].toString())
                                glide.load((currentlyPlayingSong ?: songs[0]).genre).into(ivCurSongImage)
                            }
                            switchViewPagerToSong(currentlyPlayingSong ?: return@observe)
                        }
                    }

                    Status.ERROR -> Unit
                    Status.LOADING -> Unit

                }
            }
        }

        mainViewModel.currentPlayingSong.observe(this) {
            if(it==null) return@observe
            currentlyPlayingSong=it.toSong()
            glide.load(currentlyPlayingSong?.genre).into(ivCurSongImage)
            switchViewPagerToSong(currentlyPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this){
            playbackState=it
            ivPlayPause.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this) {
          it?.getContentIfNotHandled()?.let { result ->
              when(result.status){
                  Status.ERROR -> Snackbar.make(rootLayout,result.message?: "An unknown error occured",Snackbar.LENGTH_LONG).show()
                else -> Unit
              }
          }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    Status.ERROR -> Snackbar.make(rootLayout,result.message?: "An unknown error occured",Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    }
}