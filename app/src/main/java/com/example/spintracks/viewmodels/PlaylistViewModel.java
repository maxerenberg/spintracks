package com.example.spintracks.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.spintracks.SharedExecutor;
import com.example.spintracks.dal.LocalSong;
import com.example.spintracks.dal.MusicDao;
import com.example.spintracks.dal.MusicDatabase;

import java.util.List;

public class PlaylistViewModel extends AndroidViewModel {
    private final MusicDao dao;

    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        MusicDatabase db = MusicDatabase.getDatabase(application);
        dao = db.musicDao();
    }

    public LiveData<List<String>> getPlaylists() {
        return dao.selectPlaylists();
    }

    public LiveData<List<LocalSong>> getLocalPlaylistSongs(String playlistName) {
        return dao.selectLocalPlaylistSongs(playlistName);
    }

    public void deletePlaylist(String playlistName) {
        SharedExecutor.execute(() -> {
            dao.deletePlaylist(playlistName);
        });
    }

    public MusicDao getDao() {
        return dao;
    }
}
