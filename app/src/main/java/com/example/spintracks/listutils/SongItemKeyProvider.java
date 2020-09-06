package com.example.spintracks.listutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import com.example.spintracks.dal.Song;

import java.util.HashMap;
import java.util.List;

public class SongItemKeyProvider extends ItemKeyProvider<Long> {
    private List<? extends Song> songs = null;
    private HashMap<Long, Integer> idToSongIndex = null;
    public SongItemKeyProvider() {
        super(ItemKeyProvider.SCOPE_MAPPED);
    }

    public void setSongs(List<? extends Song> items) {
        songs = items;
        idToSongIndex = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            idToSongIndex.put(songs.get(i).getSpinId(), i);
        }
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        return songs.get(position).getSpinId();
    }

    @Override
    public int getPosition(@NonNull Long key) {
        return idToSongIndex.get(key);
    }
}
