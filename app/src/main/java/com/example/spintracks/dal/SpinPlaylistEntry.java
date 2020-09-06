package com.example.spintracks.dal;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = SongInfo.class, parentColumns = "spinId", childColumns = "spinId",
                onDelete = CASCADE) },
        primaryKeys = { "playlistName", "spinId" },
        indices = { @Index("spinId") })
public class SpinPlaylistEntry {
    @NonNull
    public String playlistName;
    @NonNull
    public Long spinId;

    public SpinPlaylistEntry(String playlistName, long spinId) {
        this.playlistName = playlistName;
        this.spinId = spinId;
    }
}
