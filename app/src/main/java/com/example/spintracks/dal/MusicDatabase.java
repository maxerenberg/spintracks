package com.example.spintracks.dal;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.spintracks.SharedExecutor;

@Database(entities = {SongInfo.class, LocalSongInfo.class, SpinPlaylistEntry.class},
          version = 1, exportSchema = false)
public abstract class MusicDatabase extends RoomDatabase {
    public abstract MusicDao musicDao();

    private static volatile MusicDatabase INSTANCE;
    private static volatile boolean loadedLocalSongs = false;

    private static final int NUMBER_OF_THREADS = 4;
    private static final String TAG = MusicDatabase.class.getSimpleName();

    public static MusicDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MusicDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MusicDatabase.class, "music_database")
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    loadLocalSongsAsync(context);
                                }
                            })
                            .build();
                }
            }
        } else if (!loadedLocalSongs) {
            loadLocalSongsAsync(context);
        }
        return INSTANCE;
    }

    private static void loadLocalSongsAsync(final Context context) {
        SharedExecutor.execute(() -> loadLocalSongs(context));
    }

    private static synchronized void loadLocalSongs(Context context) {
        if (loadedLocalSongs) return;
        MusicDao dao = INSTANCE.musicDao();
        HashSet<Long> previousIds = new HashSet<>();
        HashSet<Long> newIds = new HashSet<>();
        previousIds.addAll(dao.selectSpinIdSync());
        // Query the media collection
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            while (cursor.moveToNext()) {
                long localId = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String album = cursor.getString(albumColumn);
                String artist = cursor.getString(artistColumn);
                String title = cursor.getString(titleColumn);
                int track = cursor.getInt(trackColumn);
                int duration = cursor.getInt(durationColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, localId);
                //Log.i(TAG, "FOUND SONG: " + contentUri + ", " + localId + ", " + name
                //        + ", " + album + ", " + artist + ", " + title + ", " + track);
                long spinId = makeSpinId(localId);
                newIds.add(spinId);
                // Only add a song to the database if it doesn't exist already
                if (!previousIds.contains(spinId)) {
                    SongInfo songInfo = new SongInfo(spinId, title, album, artist, track, duration, SongInfo.SOURCE_LOCAL);
                    dao.insertSongInfo(songInfo);
                    LocalSongInfo localSongInfo = new LocalSongInfo(spinId, localId, contentUri.toString());
                    dao.insertLocalSongInfo(localSongInfo);
                }
            }
        } catch (SecurityException e) {
            // permission has not been granted yet
            return;
        }
        // remove all songs which no longer exist locally
        for (Long spinId : previousIds) {
            if (!newIds.contains(spinId)) dao.deleteSong(spinId);
        }
        loadedLocalSongs = true;
    }

    private static long makeSpinId(long localId) {
        // We'll never have more than 10 sources of songs, so just multiply the
        //  source-specific id by 10 and add the source constant
        return localId * 10 + SongInfo.SOURCE_LOCAL;
    }

    private static void loadLocalPlaylists(Context context) {
        /*
        Google Play Music uses a non-standard Content URI and column names.
        See https://stackoverflow.com/questions/9669358/querying-google-play-music-database-in-android
            https://stackoverflow.com/questions/27270977/cannot-get-playlist-created-in-google-play-music
        */
        String[] projection = new String[] {
                "_id",
                "playlist_name"
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        try (Cursor cursor = context.getContentResolver().query(
                //MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                Uri.parse("content://com.google.android.music.MusicContent/playlists"),
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int playlistIdColumn = 0;
            int playlistNameColumn = 1;
            while (cursor.moveToNext()) {
                long playlistId = cursor.getLong(playlistIdColumn);
                String playlistName = cursor.getString(playlistNameColumn);
                Uri playlistUri = Uri.parse("content://com.google.android.music.MusicContent/playlists/" +
                        playlistId + "/members");
                System.err.println( "FOUND PLAYLIST: " + playlistId + ", " + playlistName
                        + ", " + playlistUri);
                String[] subProjection = new String[] {
                        "SourceId",
                        MediaStore.Audio.Media.TITLE
                };
                try (Cursor subCursor = context.getContentResolver().query(
                        playlistUri,
                        subProjection,
                        null,null,null
                )) {
                    int audioIdColumn = 0;
                    int titleColumn = 1;
                    while (subCursor.moveToNext()) {
                        long audioId = subCursor.getLong(audioIdColumn);
                        String title = subCursor.getString(titleColumn);
                        System.err.println("FOUND PLAYLIST SONG: " + audioId + ", " + title);
                    }
                }
            }
        }
    }
}
