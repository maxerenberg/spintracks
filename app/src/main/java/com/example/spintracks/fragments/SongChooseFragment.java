package com.example.spintracks.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.spintracks.R;
import com.example.spintracks.dal.Song;
import com.example.spintracks.listutils.MyDetailsLookup;
import com.example.spintracks.listutils.SongChooseListAdapter;
import com.example.spintracks.listutils.SongItemKeyProvider;
import com.example.spintracks.viewmodels.LocalSongViewModel;
import com.example.spintracks.viewmodels.PlaylistBuilderViewModel;
import com.example.spintracks.viewmodels.SongViewModel;

import java.util.List;

public class SongChooseFragment extends Fragment {
    public final static String songSelectorAttributeKey = "SongChooseFragment.songSelectionAttribute";
    public final static String songSelectorValueKey = "SongChooseFragment.songSelectionValue";
    public final static int SONG_SELECTOR_ALBUM = 0;
    public final static int SONG_SELECTOR_ARTIST = 1;
    public final static int SONG_SELECTOR_PLAYLIST = 2;
    public final static int SONG_SELECTOR_ALL = 3;
    private int songSelectorAttribute = -1;
    private String songSelectorValue = null;

    public SongChooseFragment() {
        super();
    }

    public SongChooseFragment(int songSelectorAttribute, String songSelectorValue) {
        this.songSelectorAttribute = songSelectorAttribute;
        this.songSelectorValue = songSelectorValue;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (songSelectorAttribute == -1) {
            songSelectorAttribute = getArguments().getInt(songSelectorAttributeKey);
            songSelectorValue = getArguments().getString(songSelectorValueKey);
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_song_choose, container, false);
        // Set title of selector (e.g. album name)
        ((TextView) rootView.findViewById(R.id.song_selector)).setText(songSelectorValue);
        // Create model and RecyclerView
        // TODO: use different ViewModel depending on music sourceexec@csclub.uwaterloo.ca
        SongViewModel<? extends Song> model = new ViewModelProvider(requireActivity())
                .get(LocalSongViewModel.class);
        RecyclerView recyclerView = rootView.findViewById(R.id.songs_choose_recyclerview);
        setupRecyclerview(recyclerView, model);
        // Checking the selector will check all the songs in the list
        final CheckBox selectorCheckBox = rootView.findViewById(R.id.song_selector_checkbox);
        selectorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    SongChooseListAdapter.SongChooseViewHolder holder =
                            (SongChooseListAdapter.SongChooseViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    holder.songCheckBox.setChecked(isChecked);
                }
            }
        });
        return rootView;
    }

    private <S extends Song> void setupRecyclerview(RecyclerView recyclerView, SongViewModel<S> model) {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        final SongChooseListAdapter<S> adapter = new SongChooseListAdapter<>(
                new ViewModelProvider(requireActivity()).get(PlaylistBuilderViewModel.class)
        );
        recyclerView.setAdapter(adapter);

        SongItemKeyProvider keyProvider = new SongItemKeyProvider();
        SelectionTracker selectionTracker = new SelectionTracker.Builder<Long>(
                "songs-selection",
                recyclerView,
                keyProvider,
                new MyDetailsLookup<>(recyclerView),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();
        adapter.setSelectionTracker(selectionTracker);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        getLiveSongs(model).observe(activity, new Observer<List<S>>() {
            @Override
            public void onChanged(List<S> songs) {
                keyProvider.setSongs(songs);
                adapter.setSongs(songs);
            }
        });
    }

    private <S extends Song> LiveData<List<S>> getLiveSongs(SongViewModel<S> model) {
        switch (songSelectorAttribute) {
            case SongChooseFragment.SONG_SELECTOR_ALBUM:
                return model.getSongsByAlbum(songSelectorValue);
            case SongChooseFragment.SONG_SELECTOR_ARTIST:
                return model.getSongsByArtist(songSelectorValue);
            case SongChooseFragment.SONG_SELECTOR_ALL:
                return model.getAllSongs();
            default:
                return null;
        }
    }
}
