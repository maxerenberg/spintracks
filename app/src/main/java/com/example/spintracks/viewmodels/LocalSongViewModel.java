package com.example.spintracks.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.spintracks.dal.LocalSong;
import com.example.spintracks.dal.MusicDao;
import com.example.spintracks.dal.MusicDatabase;

import java.util.List;

public class LocalSongViewModel extends AndroidViewModel implements SongViewModel<LocalSong> {
    private LiveData<List<LocalSong>> allSongs;
    private LiveData<List<String>> albums;
    private LiveData<List<String>> artists;

    private final MusicDao dao;

    public LocalSongViewModel(Application application) {
        super(application);
        MusicDatabase db = MusicDatabase.getDatabase(application);
        dao = db.musicDao();
        allSongs = dao.selectLocalAll();
        albums = dao.selectLocalAlbums();
        artists = dao.selectLocalArtists();

    }

    @Override
    public LiveData<List<LocalSong>> getAllSongs() { return allSongs; }
    @Override
    public LiveData<List<String>> getAlbums() { return albums; }
    @Override
    public LiveData<List<String>> getArtists() { return artists; }

    @Override
    public LiveData<List<LocalSong>> getSongsByAlbum(String album) {
        return dao.selectLocalByAlbum(album);
    }

    @Override
    public LiveData<List<LocalSong>> getSongsByArtist(String artist) {
        return dao.selectLocalByArtist(artist);
    }
}
