package com.example.spintracks.listutils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import java.util.HashMap;
import java.util.List;

public class StringItemKeyProvider extends ItemKeyProvider<String> {
    private List<String> items = null;
    private HashMap<String, Integer> stringToIndex = null;

    public StringItemKeyProvider() {
        super(ItemKeyProvider.SCOPE_MAPPED);
    }

    public void setItems(List<String> items) {
        this.items = items;
        stringToIndex = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            stringToIndex.put(items.get(i), i);
        }
    }

    @Nullable
    @Override
    public String getKey(int position) {
        return items.get(position);
    }

    @Override
    public int getPosition(@NonNull String key) {
        return stringToIndex.get(key);
    }
}
