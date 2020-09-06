package com.example.spintracks.dal;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

public final class LocalSong implements Song {
    @Embedded
    @NonNull
    SongInfo songInfo;
    @Embedded
    @NonNull
    LocalSongInfo localSongInfo;

    @Override
    public long getSpinId() { return songInfo.spinId; }
    @Override
    public String getTitle() { return songInfo.title; }
    @Override
    public String getAlbum() { return songInfo.album; }
    @Override
    public String getArtist() { return songInfo.artist; }
    @Override
    public String getBeatsInfo() { return songInfo.beatsInfo; }
    @Override
    public int getDuration() { return songInfo.duration; }
    @Override
    public String getUri() { return localSongInfo.uri; }
    @Override
    public int getSource() { return songInfo.source; }
}
