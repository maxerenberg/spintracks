package com.example.spintracks.dal;

public interface Song {
    long getSpinId();
    String getTitle();
    String getAlbum();
    String getArtist();
    String getUri();
    String getBeatsInfo();
    int getSource();
    int getDuration();
}
