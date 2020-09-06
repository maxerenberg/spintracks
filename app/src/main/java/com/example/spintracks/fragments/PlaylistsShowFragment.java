package com.example.spintracks.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spintracks.R;
import com.example.spintracks.listutils.MyDetailsLookup;
import com.example.spintracks.listutils.PlaylistAdapter;
import com.example.spintracks.listutils.StringItemKeyProvider;
import com.example.spintracks.viewmodels.PlaylistViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PlaylistsShowFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_playlists_show, container, false);
        final Activity activity = getActivity();
        // Set up RecyclerView
        final RecyclerView recyclerView = rootView.findViewById(R.id.playlist_recycler_view);
        final TextView emptyView = rootView.findViewById(R.id.empty_playlist_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        final PlaylistViewModel model = new ViewModelProvider(this).get(PlaylistViewModel.class);
        final PlaylistAdapter adapter = new PlaylistAdapter();
        recyclerView.setAdapter(adapter);
        final StringItemKeyProvider keyProvider = new StringItemKeyProvider();
        SelectionTracker selectionTracker = new SelectionTracker.Builder<String>(
                "spin-playlist-selection",
                recyclerView,
                keyProvider,
                new MyDetailsLookup<>(recyclerView),
                StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build();
        adapter.setSelectionTracker(selectionTracker);
        NavController navController = NavHostFragment.findNavController(this);
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String playlistName, boolean selected) {
                super.onItemStateChanged(playlistName, selected);
                if (selected) {
                    final Bundle bundle = new Bundle();
                    bundle.putString(PlaylistCreateFragment.playlistNameKey, playlistName);
                    navController.navigate(R.id.action_PlaylistsShowFragment_to_playlistSongsShowFragment, bundle);
                }
            }
        });
        // set playlists listener
        model.getPlaylists().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> playlists) {
                adapter.setPlaylists(playlists);
                keyProvider.setItems(playlists);
                if (playlists.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }
        });
        // Initialize FAB
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            NavHostFragment.findNavController(PlaylistsShowFragment.this)
                    .navigate(R.id.action_PlaylistsShowFragment_to_PlaylistCreateDialogFragment);
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
