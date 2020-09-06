package com.example.spintracks.listutils;

import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SelectableListAdapter<VH extends SelectableListAdapter.SelectableViewHolder, T>
        extends RecyclerView.Adapter<VH> {
    // TODO: change this to an outer class so that we can prevent the unchecked cast
    //  in MyDetailsLookup::getItemDetails()
    public abstract class SelectableViewHolder extends RecyclerView.ViewHolder {
        SelectableViewHolder(View itemView) { super(itemView); }

        abstract ItemDetailsLookup.ItemDetails<T> getItemDetails();
    }

    private SelectionTracker selectionTracker = null;

    SelectableListAdapter() {
        setHasStableIds(true);
    }

    public void setSelectionTracker(SelectionTracker selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        T current = getItem(position);
        boolean isSelected = selectionTracker.isSelected(current);
        bindData(holder, current, isSelected);
    }

    protected abstract void bindData(VH holder, T item, boolean isSelected);

    protected abstract T getItem(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }
}
