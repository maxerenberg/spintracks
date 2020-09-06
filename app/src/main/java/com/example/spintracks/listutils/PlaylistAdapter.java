package com.example.spintracks.listutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;

import com.example.spintracks.R;

import java.util.List;

public class PlaylistAdapter extends SelectableListAdapter<PlaylistAdapter.PlaylistViewHolder, String> {
    class PlaylistViewHolder extends SelectableListAdapter.SelectableViewHolder {
        TextView textView;

        private PlaylistViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.playlist_create_name);
        }

        @Override
        ItemDetailsLookup.ItemDetails<String> getItemDetails() {
            final int position = getAdapterPosition();
            return new MyItemDetails<>(position, playlists.get(position));
        }
    }

    private List<String> playlists = null;

    public void setPlaylists(List<String> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_list_item, parent, false);
        return new PlaylistViewHolder(v);
    }

    @Override
    protected void bindData(PlaylistAdapter.PlaylistViewHolder holder, String playlist, boolean isSelected) {
        holder.textView.setText(playlist);
        holder.textView.setActivated(isSelected);
    }

    @Override
    protected String getItem(int position) { return playlists.get(position); }

    @Override
    public int getItemCount() { return playlists == null ? 0 : playlists.size(); }
}
