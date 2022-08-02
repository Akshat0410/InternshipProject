package com.plcoding.spotifycloneyt.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.entities.Song
import com.plcoding.spotifycloneyt.others.Constants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val fireStore = FirebaseFirestore.getInstance()
    private val songsCollection= fireStore.collection(SONG_COLLECTION)

    suspend fun getAllSongsFromCollection() : List<Song>{
        return try{
            songsCollection.get().await().toObjects(Song::class.java)
        }catch (e: Exception){
            emptyList<Song>()
        }
    }
}