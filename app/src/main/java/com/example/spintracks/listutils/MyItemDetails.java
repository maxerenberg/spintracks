package com.example.spintracks.listutils;

import android.view.MotionEvent;

import androidx.recyclerview.selection.ItemDetailsLookup;

public class MyItemDetails<T> extends ItemDetailsLookup.ItemDetails<T> {
    private int position;
    private T item;

    public MyItemDetails(int position, T item) {
        this.position = position;
        this.item = item;
    }

    @Override
    public int getPosition() { return position; }

    @Override
    public T getSelectionKey() { return item; }

    @Override
    public boolean inSelectionHotspot(MotionEvent e) {
        // This is necessary to enable single-tap selection. See
        // https://stackoverflow.com/questions/55118388/select-reyclerview-itemjust-single-tap-with-recycler-view-selection-library
        return true;
    }
}
