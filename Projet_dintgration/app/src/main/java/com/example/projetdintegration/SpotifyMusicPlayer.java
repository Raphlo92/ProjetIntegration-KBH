package com.example.projetdintegration;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.PlayerApi;

public class SpotifyMusicPlayer extends AppCompatActivity {
    public static final String EXTRA_SPOTIFY_MUSIC_PLAYER_URI = "PLAYER_URI";
    PlayerApi player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);
        player = LierSpotifyActivity.appRemote.getPlayerApi();
        player.play(getIntent().getExtras().getString(EXTRA_SPOTIFY_MUSIC_PLAYER_URI));
    }
}