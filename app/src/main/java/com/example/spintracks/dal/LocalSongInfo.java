package com.example.spintracks.dal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import static androidx.room.ForeignKey.CASCADE;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(
        entity = SongInfo.class, parentColumns = "spinId", childColumns = "LocalSongInfo_spinId", onDelete = CASCADE))
public class LocalSongInfo {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "LocalSongInfo_spinId")
    public long spinId;
    @NonNull
    public long localId;
    @NonNull
    public String uri;

    public LocalSongInfo(long spinId, long localId, String uri) {
        this.spinId = spinId;
        this.localId = localId;
        this.uri = uri;
    }
}
