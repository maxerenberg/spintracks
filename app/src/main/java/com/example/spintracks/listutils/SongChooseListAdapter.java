package com.example.spintracks.listutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;

import com.example.spintracks.R;
import com.example.spintracks.dal.Song;
import com.example.spintracks.viewmodels.PlaylistBuilderViewModel;

import java.util.HashMap;
import java.util.List;

public class SongChooseListAdapter<S extends Song> extends SelectableListAdapter<SongChooseListAdapter.SongChooseViewHolder, Long> {
    public class SongChooseViewHolder extends SelectableListAdapter.SelectableViewHolder {
        public final TextView songTextView;
        public final CheckBox songCheckBox;

        public SongChooseViewHolder(View itemView) {
            super(itemView);
            songTextView = itemView.findViewById(R.id.song_choose_name);
            songCheckBox = itemView.findViewById(R.id.song_item_checkbox);
            songCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int position = SongChooseViewHolder.this.getAdapterPosition();
                    S song = songs.get(position);
                    if (isChecked) {
                        playlistModel.addSong(song);
                    } else {
                        playlistModel.removeSong(song);
                    }
                }
            });
        }

        @Override
        ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            final int position = getAdapterPosition();
            return new MyItemDetails<>(position, songs.get(position).getSpinId());
        }
    }

    private List<S> songs = null;
    private HashMap<Long, S> idToSong = null;
    private HashMap<Long, Boolean> itemInitialized = null;
    private PlaylistBuilderViewModel playlistModel = null;

    public SongChooseListAdapter(PlaylistBuilderViewModel model) {
        super();
        playlistModel = model;
    }

    public void setSongs(List<S> songs) {
        this.songs = songs;
        idToSong = new HashMap<>();
        itemInitialized = new HashMap<>();
        for (S song : songs) {
            idToSong.put(song.getSpinId(), song);
            itemInitialized.put(song.getSpinId(), false);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongChooseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_choose_list_item, parent, false);
        return new SongChooseViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return songs == null ? 0 : songs.size();
    }

    @Override
    protected Long getItem(int position) {
        return songs.get(position).getSpinId();
    }

    @Override
    protected void bindData(SongChooseListAdapter.SongChooseViewHolder holder, Long spinId, boolean isSelected) {
        S song = idToSong.get(spinId);
        holder.songTextView.setText(song.getTitle());
        if (!itemInitialized.get(song.getSpinId())) {
            itemInitialized.replace(song.getSpinId(), true);
            holder.songCheckBox.setChecked(playlistModel.containsSong(song));
        } else {
            holder.songCheckBox.setChecked(isSelected);
        }
    }
}
