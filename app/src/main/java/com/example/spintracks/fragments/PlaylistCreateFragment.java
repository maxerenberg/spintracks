package com.example.spintracks.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.spintracks.R;
import com.google.android.material.snackbar.Snackbar;

public class PlaylistCreateFragment extends Fragment {
    public final static String playlistNameKey = "PlaylistCreateFragment.playlistNameKey";
    public final static String playlistSourceKey = "PlaylistCreateFragment.playlistSourceKey";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();
        // Set title
        activity.setTitle("Create new playlist");
        // Set onClick listeners
        view.findViewById(R.id.playlist_create_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playlistName = ((TextView) view.findViewById(R.id.playlist_create_name))
                        .getText().toString();
                if (playlistName.length() == 0) {
                    Snackbar.make(view, "Please enter a name for the playlist", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(playlistNameKey, playlistName);
                NavHostFragment.findNavController(PlaylistCreateFragment.this)
                        .navigate(R.id.action_playlistCreate_to_playlistSource, bundle);
            }
        });
    }
}
