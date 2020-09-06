package com.example.spintracks.dal;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MusicDao {
    @Insert
    void insertSongInfo(SongInfo songInfo);

    @Query("UPDATE SongInfo SET beatsInfo = :beatsInfo WHERE spinId = :spinId")
    void updateBeatsInfo(long spinId, String beatsInfo);

    @Insert
    void insertLocalSongInfo(LocalSongInfo localSongInfo);

    @Query("DELETE FROM SongInfo")
    void deleteAll();

    @Query("DELETE FROM SongInfo WHERE spinId = :spinId")
    void deleteSong(long spinId);

    @Query("SELECT spinId FROM SongInfo")
    List<Long> selectSpinIdSync();

    @Query("SELECT * FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId")
    LiveData<List<LocalSong>> selectLocalAll();

    @Query("SELECT * FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId")
    List<LocalSong> selectLocalAllSync();

    @Query("SELECT DISTINCT album FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId")
    LiveData<List<String>> selectLocalAlbums();

    @Query("SELECT LocalSongInfo.*, SongInfo.* FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId " +
           "WHERE album = :album")
    LiveData<List<LocalSong>> selectLocalByAlbum(String album);

    @Query("SELECT DISTINCT artist FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId")
    LiveData<List<String>> selectLocalArtists();

    @Query("SELECT LocalSongInfo.*, SongInfo.* FROM LocalSongInfo " +
           "INNER JOIN SongInfo ON LocalSongInfo_spinId = SongInfo.spinId " +
           "WHERE artist = :artist")
    LiveData<List<LocalSong>> selectLocalByArtist(String artist);

    @Insert
    void insertPlaylistEntries(List<SpinPlaylistEntry> playlistEntry);

    @Query("SELECT DISTINCT playlistName FROM SpinPlaylistEntry")
    LiveData<List<String>> selectPlaylists();

    @Query("SELECT LocalSongInfo.*, SongInfo.* FROM SpinPlaylistEntry " +
           "INNER JOIN LocalSongInfo ON SpinPlaylistEntry.spinId = LocalSongInfo_spinId " +
           "INNER JOIN SongInfo ON SpinPlaylistEntry.spinId = SongInfo.spinId " +
           "WHERE playlistName = :playlistName")
    LiveData<List<LocalSong>> selectLocalPlaylistSongs(String playlistName);

    @Query("DELETE FROM SpinPlaylistEntry WHERE playlistName = :playlistName")
    void deletePlaylist(String playlistName);
}
