package com.example.spintracks.listutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;

import com.example.spintracks.R;

import java.util.List;

public class SongGroupListAdapter extends SelectableListAdapter<SongGroupListAdapter.SongGroupViewHolder, String> {
    class SongGroupViewHolder extends SelectableListAdapter.SelectableViewHolder {
        final TextView songItemView;

        private SongGroupViewHolder(View itemView) {
            super(itemView);
            songItemView = itemView.findViewById(R.id.songgroup_textview);
        }

        @Override
        ItemDetailsLookup.ItemDetails<String> getItemDetails() {
            int position = getAdapterPosition();
            return new MyItemDetails<>(position, groups.get(position));
        }
    }

    private List<String> groups;

    public SongGroupListAdapter() {
        super();
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songgroup_list_item, parent, false);
        return new SongGroupViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return groups == null ? 0 : groups.size();
    }

    @Override
    protected String getItem(int position) { return groups.get(position); }

    @Override
    protected void bindData(SongGroupViewHolder holder, String groupName, boolean isSelected) {
        holder.songItemView.setText(groupName);
        holder.songItemView.setActivated(isSelected);
    }
}
