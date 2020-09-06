package com.example.spintracks.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.example.spintracks.MainActivity;
import com.example.spintracks.PermissionsCallback;
import com.example.spintracks.R;
import com.google.android.material.snackbar.Snackbar;

public class PlaylistSourceFragment extends Fragment {
    public static final String MUSIC_SOURCE_KEY = "PlaylistSourceFragment.MUSIC_SOURCE_KEY";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_source, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final String playlistName = getArguments().getString(PlaylistCreateFragment.playlistNameKey);
        String[] sources = new String[]{"LOCAL"};
        view.findViewById(R.id.playlist_source_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedSource = ((RadioGroup) view.findViewById(R.id.playlistSourceRadioGroup))
                        .getCheckedRadioButtonId();
                if (selectedSource == -1) {
                    Snackbar.make(view, "Please select a source", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                final Bundle bundle = new Bundle();
                bundle.putString(PlaylistCreateFragment.playlistNameKey, playlistName);
                if (selectedSource == R.id.music_source_local) {
                    bundle.putString(MUSIC_SOURCE_KEY, sources[0]);
                } else {
                    throw new RuntimeException("unidentified music source: " + selectedSource);
                }
                // Check permissions
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    MainActivity.setPermissionsCallback(new PermissionsCallback() {
                        @Override
                        public void afterPermissionsRequest(int grantResult) {
                            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_playlistSourceDialogFragment_to_songgroup_outer_fragment, bundle);
                            }
                        }
                    });
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MainActivity.MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                } else {
                    Navigation.findNavController(view)
                            .navigate(R.id.action_playlistSourceDialogFragment_to_songgroup_outer_fragment, bundle);
                }
            }
        });
    }
}
