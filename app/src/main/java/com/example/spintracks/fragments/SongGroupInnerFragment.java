package com.example.spintracks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spintracks.R;
import com.example.spintracks.dal.Song;
import com.example.spintracks.fragments.SongChooseFragment;
import com.example.spintracks.listutils.MyDetailsLookup;
import com.example.spintracks.listutils.SongGroupListAdapter;
import com.example.spintracks.listutils.StringItemKeyProvider;
import com.example.spintracks.viewmodels.LocalSongViewModel;
import com.example.spintracks.viewmodels.SongViewModel;

import java.util.List;

public class SongGroupInnerFragment extends Fragment {
    private final int songSelectorAttribute;
    private final String selectionId;

    SongGroupInnerFragment(int songSelectorAttribute, String selectionId) {
        this.songSelectorAttribute = songSelectorAttribute;
        this.selectionId = selectionId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_songgroup_inner, container, false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        RecyclerView recyclerView = rootView.findViewById(R.id.songgroup_recycler_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        final SongGroupListAdapter adapter = new SongGroupListAdapter();
        recyclerView.setAdapter(adapter);

        StringItemKeyProvider keyProvider = new StringItemKeyProvider();
        SelectionTracker selectionTracker = new SelectionTracker.Builder<String>(
                selectionId,
                recyclerView,
                keyProvider,
                new MyDetailsLookup<>(recyclerView),
                StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build();
        adapter.setSelectionTracker(selectionTracker);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        // TODO: choose model depending on source of songs
        SongViewModel<? extends Song> model = new ViewModelProvider(activity).get(LocalSongViewModel.class);
        getLiveGroups(model).observe(activity, (groups) -> {
            keyProvider.setItems(groups);
            adapter.setGroups(groups);
        });
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String groupName, boolean selected) {
                super.onItemStateChanged(groupName, selected);
                if (selected) {
                    final Bundle bundle = new Bundle();
                    bundle.putInt(SongChooseFragment.songSelectorAttributeKey, songSelectorAttribute);
                    bundle.putString(SongChooseFragment.songSelectorValueKey, groupName);
                    Navigation.findNavController(rootView)
                            .navigate(R.id.action_songgroup_inner_fragment_to_songChooseFragment, bundle);
                }
            }
        });

        return rootView;
    }

    private LiveData<List<String>> getLiveGroups(SongViewModel<? extends Song> model) {
        switch (songSelectorAttribute) {
            case SongChooseFragment.SONG_SELECTOR_ALBUM:
                return model.getAlbums();
            case SongChooseFragment.SONG_SELECTOR_ARTIST:
                return model.getArtists();
            default:
                return null;
        }
    }
}
