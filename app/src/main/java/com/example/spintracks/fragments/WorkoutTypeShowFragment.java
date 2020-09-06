package com.example.spintracks.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spintracks.PlayerActivity;
import com.example.spintracks.R;
import com.example.spintracks.viewmodels.PlaylistViewModel;

public class WorkoutTypeShowFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout_type_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.easy_card).setOnClickListener(v -> {
            PlayerActivity.setWorkoutType("EASY");
            goToPlaylistsShowFragment();
        });
        view.findViewById(R.id.medium_card).setOnClickListener(v -> {
            PlayerActivity.setWorkoutType("MEDIUM");
            goToPlaylistsShowFragment();
        });
        view.findViewById(R.id.hard_card).setOnClickListener(v -> {
            PlayerActivity.setWorkoutType("HARD");
            goToPlaylistsShowFragment();
        });
    }

    private void goToPlaylistsShowFragment() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_workoutTypeShowFragment_to_PlaylistsShowFragment);
    }
}
