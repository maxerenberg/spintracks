package com.example.spintracks.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.spintracks.MainActivity;
import com.example.spintracks.PermissionsCallback;
import com.example.spintracks.PlayerActivity;
import com.example.spintracks.R;
import com.example.spintracks.dal.BeatsInfoFetcher;
import com.example.spintracks.listutils.PlaylistSongListAdapter;
import com.example.spintracks.viewmodels.PlaylistViewModel;

import java.util.ArrayList;

public class PlaylistSongsShowFragment extends Fragment {
    private int songSource = -1;
    private boolean hasStoragePermissions = false;
    private boolean fetchedBeatsInfo = false;
    private static final String TAG = PlaylistSongsShowFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_playlist_songs_show, container, false);
        final AppCompatActivity activity = (AppCompatActivity) requireActivity();
        final BeatsInfoFetcher fetcher = new BeatsInfoFetcher(activity);
        NavController navController = NavHostFragment.findNavController(this);
        final String playlistName = getArguments().getString(PlaylistCreateFragment.playlistNameKey);
        // set title
        ((TextView) rootView.findViewById(R.id.playlist_name_show)).setText(playlistName);
        // create model and RecyclerView
        RecyclerView recyclerView = rootView.findViewById(R.id.songs_show_recyclerview);
        PlaylistViewModel model = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        final PlaylistSongListAdapter adapter = new PlaylistSongListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        // check permissions just in case they were revoked
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            MainActivity.setPermissionsCallback(grantResult -> {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    hasStoragePermissions = true;
                }
            });
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MainActivity.MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        } else {
            hasStoragePermissions = true;
        }
        // call different method depending on source...
        model.getLocalPlaylistSongs(playlistName).observe(activity, songs -> {
            adapter.setSongs(songs);
            songSource = songs.size() == 0 ? -1 : songs.get(0).getSource();
            // only fetch the beats info once
            if (!fetchedBeatsInfo && hasStoragePermissions) {
                fetcher.fetchAndSaveBeatsInfo(model.getDao(), new ArrayList<>(songs));
                fetchedBeatsInfo = true;
            }
        });

        Button startButton = rootView.findViewById(R.id.playlist_start_button);
        startButton.setOnClickListener(v -> {
            if (songSource == -1) return;
            Intent intent = new Intent(activity, PlayerActivity.class);
            intent.putExtra(PlaylistCreateFragment.playlistSourceKey, songSource);
            intent.putExtra(PlaylistCreateFragment.playlistNameKey, playlistName);
            startActivity(intent);
        });

        ImageButton moreButton = rootView.findViewById(R.id.playlist_more_button);
        final PopupMenu popup = new PopupMenu(activity, moreButton);
        popup.inflate(R.menu.menu_delete);
        moreButton.setOnClickListener(v -> popup.show());
        final Fragment fragment = this;
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    new DeleteConfirmDialogFragment(
                            "Are you sure you want to delete \"" + playlistName + "\"?",
                            () -> {
                                model.deletePlaylist(playlistName);
                                navController.navigate(R.id.action_playlistSongsShowFragment_to_PlaylistShowFragment);
                            })
                            .show(fragment.getFragmentManager(), "delete");
                    return true;
                default:
                    return false;
            }
        });

        return rootView;
    }
}
