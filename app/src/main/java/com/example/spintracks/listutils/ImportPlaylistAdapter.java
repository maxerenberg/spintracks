package com.example.spintracks.listutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spintracks.R;

import java.util.List;

public class ImportPlaylistAdapter extends RecyclerView.Adapter<ImportPlaylistAdapter.ImportPlaylistViewHolder> {
    class ImportPlaylistViewHolder extends RecyclerView.ViewHolder {
        final RadioButton button;

        private ImportPlaylistViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.importPlaylistButton);
        }
    }

    private List<String> playlists;

    public void setPlaylists(List<String> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ImportPlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.import_playlist_list_item, parent, false);
        return new ImportPlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportPlaylistViewHolder holder, int position) {
        String playlistName = playlists.get(position);
        String buttonText = "Import playlist from " + playlistName.toLowerCase() + " workouts";
        holder.button.setText(buttonText);
    }

    @Override
    public int getItemCount() {
        return playlists == null ? 0 : playlists.size();
    }
}
