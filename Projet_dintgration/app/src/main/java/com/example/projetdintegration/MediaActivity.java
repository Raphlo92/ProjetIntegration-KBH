package com.example.projetdintegration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MediaActivity extends AppCompatActivity{

    MediaPlaybackService mPService;
    boolean mPBound = false;
    SeekBar seekBar;
    Handler handler = new Handler();
    VideoView videoView;
    TextView currentTime;
    TextView maxTime;
    TextView mediaName;
    Boolean playing = true;
    ImageButton playButton;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    int playingId = 0;
    static String[] mediaList = {"bladee", "boku", "sea", "tacoma_narrows"};
    String VIDEO_SAMPLE = mediaList[0];
    private static final String TAG = "MediaActivity";
    private MediaController mediaController;
    private ViewGroup.LayoutParams lp;
    private ViewGroup.MarginLayoutParams mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);

        playButton = findViewById(R.id.playButton);
        ImageButton stopButton = findViewById(R.id.stopButton);
        ImageButton rewindButton = findViewById(R.id.rewindButton);
        ImageButton forwardButton = findViewById(R.id.forwardButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        maxTime = findViewById(R.id.maxTime);
        mediaName = findViewById(R.id.mediaName);
        videoView = findViewById(R.id.videoView);

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

        playButton.setOnClickListener(new GestionnairePlayPause());
        stopButton.setOnClickListener(new GestionnaireStop());
        rewindButton.setOnClickListener(new GestionnaireRewind());
        forwardButton.setOnClickListener(new GestionnaireForward());

        String fullscreen = getIntent().getStringExtra("fullScreenInd");
        if("y".equals(fullscreen)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MediaPlaybackService.mediaPlayer.seekTo(progress * 1000);
                    videoView.seekTo(progress * 1000);
                    String time = progress % (1000*60*60) / (1000*60) + ":" + (progress % (1000 * 60 * 60) % (1000 * 60) / 1000);
                    currentTime.setText(time);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBarUpdater();
        lp  = videoView.getLayoutParams();
        mp = (ViewGroup.MarginLayoutParams)videoView.getLayoutParams();

        if(isLandScape()){
            mediaController = new FullScreenMediaController(this);
            mediaController.setVisibility(View.VISIBLE);
            lp.height = getResources().getDisplayMetrics().heightPixels;
            lp.width = getResources().getDisplayMetrics().widthPixels;
            mp.setMargins(0, 0, 0,0);
        }else {
            mediaController = new MediaController(this);
            mediaController.setVisibility(View.GONE);
            lp.height = 0;
            lp.width = 0;
            mp.setMargins(8, 8, 8,8);
        }
        videoView.setLayoutParams(lp);
        videoView.setLayoutParams(mp);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    public class GestionnairePlayPause implements View.OnClickListener {
        public void onClick(View v) {
            if (MediaPlaybackService.mediaPlayer != null) {
                if (!mPService.playing) {
                    Log.d(TAG, "onClick: playing");
                    try {
                        mPService.PlayFromPause();
                        if (videoView == null) {
                            initializePlayer();
                        }
                        videoView.start();
                        videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    mPService.playing = true;
                } else {
                    Log.d(TAG, "onClick: paused");
                    if (MediaPlaybackService.mediaPlayer.isPlaying())
                        mPService.Pause();
                    videoView.pause();
                    playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    mPService.playing = false;
                }
            }
        }
    }

    private boolean isLandScape(){
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        int rotation = display.getRotation();

        if (rotation == Surface.ROTATION_90
                || rotation == Surface.ROTATION_270) {
            return true;
        }
        return false;
    }

    public class GestionnaireStop implements View.OnClickListener {
        public void onClick(View v) {
            Log.d(TAG, "onClick: stopped");
            mPService.Stop(v);
            StopPlayer();
        }
    }

    public class GestionnaireRewind implements View.OnClickListener{
        public void onClick(View v){
            try {
                mPService.PlayPrevious(v);
                initializePlayer();
                SetInfos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class GestionnaireForward implements View.OnClickListener{
        public void onClick(View v){
            try {
                mPService.PlayNext();
                initializePlayer();
                SetInfos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initializePlayer() {
        videoView = findViewById(R.id.videoView);
        Uri videoUri = MediaPlaybackService.getMedia();
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(0,0);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlaybackService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service){
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) service;
            mPService = binder.getService();
            mPBound = true;
            try {
                if(!MediaPlaybackService.running) {
                    mPService.Play();
                    initializePlayer();
                    videoView.start();
                    SetInfos();
                }else
                {
                    SetInfos();
                    initializePlayer();
                    videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
                    videoView.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0){
            mPBound = false;
        }
    };

    public void SeekBarUpdater() {
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MediaPlaybackService.mediaPlayer != null) {
                    int currentPosition = MediaPlaybackService.mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition / 1000);
                    String time = currentPosition % (1000*60*60) / (1000*60) + ":" + (currentPosition % (1000 * 60 * 60) % (1000 * 60) / 1000);
                    currentTime.setText(time);
                }
                handler.postDelayed(this, 100);
            }
        });
    }

    public void Pause() {
        mPService.Pause();
    }

    public void StopPlayer() {
        if (videoView != null) {
            videoView.pause();
            videoView = null;
            playing = false;
            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    public void PlayNext(View v) throws IOException {
        if(playingId < mediaList.length - 1){
            playingId++;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer(v);
        }
        else if(playingId == mediaList.length - 1){
            playingId = 0;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer(v);
        }
    }

    public void PlayPrevious(View v) throws IOException {
        if(playingId == 0 || videoView.getCurrentPosition()/1000 > 5){
            RestartPlayer(v);
        }
        else {
            playingId--;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer(v);
        }
    }

    public void RestartPlayer(View v) throws IOException {
        mPService.RestartPlayer();
    }

    public void SetInfos(){
        if (MediaPlaybackService.mediaPlayer != null){
            String time = MediaPlaybackService.mediaPlayer.getDuration() % (1000*60*60) / (1000*60) + ":" + (MediaPlaybackService.mediaPlayer.getDuration() % (1000 * 60 * 60) % (1000 * 60) / 1000);
            maxTime.setText(time);
            seekBar.setMax(MediaPlaybackService.mediaPlayer.getDuration() / 1000);
            mediaName.setText(MediaPlaybackService.musicArrayList.get(playingId).getName());
        }
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse( "android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }


}
