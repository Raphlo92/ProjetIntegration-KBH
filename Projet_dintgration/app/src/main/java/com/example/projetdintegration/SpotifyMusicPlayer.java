package com.example.projetdintegration;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class SpotifyMusicPlayer extends AppCompatActivity {
    public static final String EXTRA_SPOTIFY_MUSIC_PLAYER_URI = "PLAYER_URI";
    private static final String PLAY_BUTTON_LISTENER = "play_button_listener";
    private static final String PAUSE_BUTTON_LISTENER = "pause_button_listener";
    private static final String NEXT_BUTTON_LISTENER = "next_button_listener";
    private static final String PREVIOUS_BUTTON_LISTENER = "previous_button_listener";
    private static final String STOP_BUTTON_LISTENER = "stop_button_listener";

    Handler handler;
    Boolean playerIsPaused;
    ImageButton playButton;
    ImageButton nextButton;
    ImageButton previousButton;
    ImageButton stopButton;
    TextView mediaName;
    TextView trackDuration;
    TextView trackPosition;
    SeekBar playerSeekBar;
    PlayerApi player;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);
        initializePlayerState();
        initializeDisplayComponents();
        initializeImageButtons();
        initializeButtonsClickListeners();
        handler = new Handler();
        initializeSeekBar();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer_description,
                R.string.navigation_close_drawer_description);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationManager(this, this) {
            @Override
            public void gotoMedia() {}
        });
        navigationView.setCheckedItem(R.id.nav_mediaActivity);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());

    }
    private void initializeSeekBar(){
        playerIsPaused = true;
        playerSeekBar = findViewById(R.id.seekBar);
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    long time = progress * 1000;
                    player.seekTo(time);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarUpdater();
    }

    private void initializeButtonsClickListeners() {
        stopButton.setOnClickListener(getButtonsListeners(STOP_BUTTON_LISTENER));
        previousButton.setOnClickListener(getButtonsListeners(PREVIOUS_BUTTON_LISTENER));
        nextButton.setOnClickListener(getButtonsListeners(NEXT_BUTTON_LISTENER));
        playButton.setOnClickListener(getButtonsListeners(PLAY_BUTTON_LISTENER));
    }

    private void initializePlayerState(){
        player = LierSpotifyActivity.appRemote.getPlayerApi();
        player.play(getIntent().getExtras().getString(EXTRA_SPOTIFY_MUSIC_PLAYER_URI));
        player.subscribeToPlayerState().setEventCallback(this::syncWithPlayerState);
    }
    private void initializeImageButtons(){
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.forwardButton);
        previousButton = findViewById(R.id.rewindButton);
        //stopButton = findViewById(R.id.stopButton);
    }
    private void initializeDisplayComponents(){
        mediaName = findViewById(R.id.mediaName);
        trackDuration = findViewById(R.id.maxTime);
        trackPosition = findViewById(R.id.currentTime);
    }

    private View.OnClickListener getButtonsListeners(String buttonFunction){
        switch (buttonFunction){
            case PLAY_BUTTON_LISTENER: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    player.resume();
                }
            };
            case PAUSE_BUTTON_LISTENER: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    player.pause();
                }
            };
            case NEXT_BUTTON_LISTENER: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    player.skipNext();
                }
            };
            case PREVIOUS_BUTTON_LISTENER: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    player.skipPrevious();
                }
            };
            case STOP_BUTTON_LISTENER: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    player.pause();
                    player.seekTo(0);
                }
            };
            default: return new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            };
        }
    };

    private void syncWithPlayerState(PlayerState playerState){
        final Track track = playerState.track;
        playerIsPaused = playerState.isPaused;
        displayInfos(track.name + " - " + track.album.name + " de " + track.artist.name,track.duration);
        managePlayPauseButton(playerState.isPaused);
        if(playerSeekBar.getMax() != playerState.track.duration / 1000)
            playerSeekBar.setMax((int)playerState.track.duration / 1000);
        if(!playerState.isPaused)
            manageSeekBar(playerState.playbackPosition);
    }

    private void manageSeekBar(long playbackPosition) {
        int totalTimeMinutes = (int) (playbackPosition / (1000 * 60));
        int totalTimeSeconds = (int)((playbackPosition / 1000) % 60);
        playerSeekBar.setProgress((int)playbackPosition / 1000);
        trackPosition.setText(String.format("%d:%02d", totalTimeMinutes, totalTimeSeconds));
    }

    private void seekBarUpdater(){
        SpotifyMusicPlayer.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!playerIsPaused){
                    manageSeekBar((playerSeekBar.getProgress() + 1) * 1000);
                }
                handler.postDelayed(this,1000);
            }
        });
    }

    private void displayInfos(String fullTrackName, long totalTime){
        mediaName.setText(fullTrackName);
        int totalTimeMinutes = (int) (totalTime / (1000 * 60));
        int totalTimeSeconds = (int)((totalTime / 1000) % 60);
        trackDuration.setText(String.format("%d:%02d", totalTimeMinutes, totalTimeSeconds));
    }
    private void managePlayPauseButton(Boolean isPaused){
        if(!isPaused){
            playButton.setImageResource(R.drawable.ic_baseline_pause_24);
            playButton.setOnClickListener(getButtonsListeners(PAUSE_BUTTON_LISTENER));
        }else{
            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            playButton.setOnClickListener(getButtonsListeners(PLAY_BUTTON_LISTENER));
        }
    }
}