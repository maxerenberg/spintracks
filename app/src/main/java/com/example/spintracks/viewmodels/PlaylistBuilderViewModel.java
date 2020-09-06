package com.example.spintracks.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.spintracks.dal.BeatsInfoFetcher;
import com.example.spintracks.dal.MusicDao;
import com.example.spintracks.dal.MusicDatabase;
import com.example.spintracks.dal.Song;
import com.example.spintracks.dal.SpinPlaylistEntry;
import com.example.spintracks.SharedExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PlaylistBuilderViewModel extends AndroidViewModel {

    private static final String TAG = PlaylistBuilderViewModel.class.getName();
    private HashMap<Long, Song> spinIdToSong;
    private final MusicDao dao;
    private final BeatsInfoFetcher fetcher;

    public PlaylistBuilderViewModel(Application application) {
        super(application);
        fetcher = new BeatsInfoFetcher(application);
        spinIdToSong = new HashMap<>();
        MusicDatabase db = MusicDatabase.getDatabase(application);
        dao = db.musicDao();
    }

    public void addSong(Song song) {
        spinIdToSong.put(song.getSpinId(), song);
    }

    public void removeSong(Song song) {
        spinIdToSong.remove(song.getSpinId());
    }

    public boolean containsSong(Song song) {
        return spinIdToSong.containsKey(song.getSpinId());
    }

    public void saveSongsToPlaylist(String playlistName) {
        SharedExecutor.execute(() -> {
            dao.insertPlaylistEntries(spinIdToSong.keySet()
                    .stream()
                    .map(spinId -> new SpinPlaylistEntry(playlistName, spinId))
                    .collect(Collectors.toList()));
            fetcher.fetchAndSaveBeatsInfo(dao, new ArrayList<>(spinIdToSong.values()));
        });
    }
}
