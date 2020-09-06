package com.example.spintracks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.spintracks.R;
import com.example.spintracks.listutils.ImportPlaylistAdapter;
import com.example.spintracks.listutils.PlaylistAdapter;
import com.example.spintracks.viewmodels.PlaylistBuilderViewModel;
import com.example.spintracks.viewmodels.PlaylistViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class SongGroupOuterFragment extends Fragment {
    private PlaylistBuilderViewModel model;
    private String musicSource;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_songgroup_outer, container, false);
        final AppCompatActivity activity = (AppCompatActivity) requireActivity();
        model = new ViewModelProvider(activity).get(PlaylistBuilderViewModel.class);
        String playlistName = getArguments().getString(PlaylistCreateFragment.playlistNameKey);
        musicSource = getArguments().getString(PlaylistSourceFragment.MUSIC_SOURCE_KEY);
        Button button = rootView.findViewById(R.id.add_to_playlist_button);
        NavController navController = NavHostFragment.findNavController(this);
        button.setOnClickListener(v -> {
            model.saveSongsToPlaylist(playlistName);
            navController.navigate(R.id.action_songgroup_outer_fragment_to_FirstFragment);
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager viewPager = view.findViewById(R.id.songs_choose_pager);
        if (musicSource.equals("LOCAL")) {
            SongsPagerAdapter adapter = new SongsPagerAdapter(getChildFragmentManager());
            viewPager.setAdapter(adapter);
            TabLayout tabLayout = view.findViewById(R.id.songs_choose_tabs);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            RecyclerView recyclerView = view.findViewById(R.id.importPlaylistRecyclerView);
            TextView textView = view.findViewById(R.id.selectImportPlaylistTextView);
            viewPager.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            final PlaylistViewModel model = new ViewModelProvider(this).get(PlaylistViewModel.class);
            final ImportPlaylistAdapter adapter = new ImportPlaylistAdapter();
            recyclerView.setAdapter(adapter);
            // The musicSource should be equal to a workoutType
            model.getPlaylists().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> playlists) {
                    adapter.setPlaylists(playlists);
                }
            });
        }
    }
}
