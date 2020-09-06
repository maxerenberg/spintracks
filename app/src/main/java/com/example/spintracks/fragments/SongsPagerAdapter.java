package com.example.spintracks.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.spintracks.fragments.SongChooseFragment;
import com.example.spintracks.fragments.SongGroupInnerFragment;

public class SongsPagerAdapter extends FragmentPagerAdapter {
    public SongsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new SongGroupInnerFragment(SongChooseFragment.SONG_SELECTOR_ALBUM, "album-selection");
                break;
            case 1:
                fragment = new SongGroupInnerFragment(SongChooseFragment.SONG_SELECTOR_ARTIST, "artist-selection");
                break;
            case 2:
                fragment = new SongChooseFragment(SongChooseFragment.SONG_SELECTOR_ALL, "All Songs");
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        switch (i) {
            case 0:
                return "ALBUMS";
            case 1:
                return "ARTISTS";
            case 2:
                return "SONGS";
            default:
                return "ERROR";
        }
    }
}
