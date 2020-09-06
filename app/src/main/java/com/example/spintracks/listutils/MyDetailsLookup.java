package com.example.spintracks.listutils;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public final class MyDetailsLookup<T> extends ItemDetailsLookup<T> {
    private final RecyclerView recyclerView;

    public MyDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public @Nullable ItemDetails<T> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof SelectableListAdapter.SelectableViewHolder) {
                return ((SelectableListAdapter.SelectableViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
