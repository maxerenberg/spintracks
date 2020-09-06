package com.example.spintracks.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.spintracks.dal.Song;

import java.util.List;

public interface SongViewModel<S extends Song> {
    LiveData<List<S>> getAllSongs();
    LiveData<List<String>> getAlbums();
    LiveData<List<String>> getArtists();
    LiveData<List<S>> getSongsByAlbum(String album);
    LiveData<List<S>> getSongsByArtist(String artist);
}
