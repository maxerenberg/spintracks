
package com.example.spintracks.listutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spintracks.R;
import com.example.spintracks.dal.Song;

import java.util.List;

public class PlaylistSongListAdapter extends RecyclerView.Adapter<PlaylistSongListAdapter.PlaylistSongViewHolder> {
    class PlaylistSongViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        private PlaylistSongViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.playlist_song_textview);
        }
    }

    private List<? extends Song> songs = null;

    public void setSongs(List<? extends Song> playlist) {
        songs = playlist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistSongListAdapter.PlaylistSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_song_list_item, parent, false);
        return new PlaylistSongListAdapter.PlaylistSongViewHolder(itemView);
    }

    @Override
    public int getItemCount() { return songs == null ? 0 : songs.size(); }

    @Override
    public void onBindViewHolder(PlaylistSongListAdapter.PlaylistSongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.textView.setText(song.getTitle());
    }
}
