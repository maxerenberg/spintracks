package com.example.spintracks.dal;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SongInfo {
    @PrimaryKey
    public long spinId;
    @NonNull
    public String title;
    @NonNull
    public String artist;
    @NonNull
    public String album;
    @NonNull
    public int track;
    @NonNull
    public int duration;
    @NonNull
    public int source;
    public String beatsInfo;

    @Ignore
    public static final int SOURCE_LOCAL = 0;

    SongInfo(long spinId, String title, String album, String artist, int track, int duration, int source) {
        this.spinId = spinId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.track = track;
        this.duration = duration;
        this.source = source;
        this.beatsInfo = null;
    }
}
