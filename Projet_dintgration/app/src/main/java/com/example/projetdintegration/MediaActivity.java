package com.example.projetdintegration;

import android.media.AudioManager;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.VideoView;


import androidx.appcompat.app.AppCompatActivity;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.io.IOException;

public class MediaActivity extends AppCompatActivity{

    MediaPlaybackService mPService;
    boolean mPBound = false;
    SeekBar seekBar;
    Handler handler = new Handler();
    VideoView videoView;
    TextView currentTime;
    TextView maxTime;
    TextView mediaName;
    private static final String VIDEO_SAMPLE = "sea";
    private static final String TAG = "MediaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);

        ImageButton playButton = findViewById(R.id.playButton);
        ImageButton stopButton = findViewById(R.id.stopButton);
        seekBar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        maxTime = findViewById(R.id.maxTime);
        mediaName = findViewById(R.id.mediaName);
        videoView = findViewById(R.id.videoView);

        playButton.setOnClickListener(new GestionnairePlay());
        stopButton.setOnClickListener(new GestionnaireStop());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(videoView != null && fromUser){
                    videoView.seekTo(progress*1000);
                    String time = String.valueOf(progress/60) + ":" + String.format("%02d", progress);
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
    }

    public class GestionnairePlay implements View.OnClickListener {
            public void onClick(View v) {
                Log.d(TAG, "onClick: playing");
                Play(v);
            }
    public class GestionnairePlayPause implements View.OnClickListener {
        public void onClick(View v) {
            if(!mPService.playing) {
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
            }
            else{
                Log.d(TAG, "onClick: paused");
                if(MediaPlaybackService.mediaPlayer.isPlaying())
                mPService.Pause();
                videoView.pause();
                playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                mPService.playing = false;
            }
        }
    }

    public class GestionnaireStop implements View.OnClickListener {
        public void onClick(View v) {
            Log.d(TAG, "onClick: stopped");
            mPService.Stop(v);
            StopPlayer();
        }
    }

    public class GestionnairePause implements View.OnClickListener {
        public void onClick(View v){
            Log.d(TAG, "onClick: paused");
            Pause(v);
            try {
                mPService.PlayPrevious(v);
                initializePlayer();
                SetInfos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class GestionnaireStop implements View.OnClickListener{
        public void onClick(View v){
            Log.d(TAG, "onClick: stopped");
            Stop(v);
        }
    }

    public void initializePlayer(){
        if(videoView == null) {
            videoView = findViewById(R.id.videoView);
        }
        Uri videoUri = getMedia(VIDEO_SAMPLE);
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
        Uri videoUri = getMedia(MediaPlaybackService.VIDEO_SAMPLE);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(0,0);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        initializePlayer();
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                String time = String.valueOf(videoView.getDuration()/1000/60) + ":" + String.format("%02d", videoView.getDuration()/1000);
                maxTime.setText(time);
                seekBar.setMax(videoView.getDuration()/1000);
                mediaName.setText(VIDEO_SAMPLE);
            }
        });
        Intent intent = new Intent(this, MediaPlaybackService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
    }

    @Override
    protected void onStop(){
        super.onStop();
        //mPService.StopPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            //mPService.StopPlayer();
        }
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

    /*private Uri getMedia(String mediaName) {
        return Uri.parse("android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }*/

    public void SeekBarUpdater(){
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(videoView != null){
                    int currentPosition = videoView.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    String time = String.valueOf(currentPosition/60) + ":" + String.format("%02d", currentPosition);
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

    public void Play(View v) {
        if(videoView == null) {
            initializePlayer();
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    StopPlayer();
            }
            });
        }
        videoView.start();
    }

    public void Pause(View v) {
        if(videoView != null){
            videoView.pause();
        }
    }

    public void StopPlayer(){
        if(videoView != null) {
            videoView.stopPlayback();
            videoView = null;
            Toast.makeText(this, "mediaPlayer released", Toast.LENGTH_LONG).show();
        }
    }

    public void Stop(View v){
        StopPlayer();
    /*public void Play(View v) {
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
        if (videoView == null) {
            mPService.initializePlayer();
            mPService.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPService.StopPlayer();
                }
            });
        }
        mPService.videoView.start();
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
    }*/

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
        String time = MediaPlaybackService.mediaPlayer.getDuration() % (1000*60*60) / (1000*60) + ":" + (MediaPlaybackService.mediaPlayer.getDuration() % (1000 * 60 * 60) % (1000 * 60) / 1000);
        maxTime.setText(time);
        seekBar.setMax(MediaPlaybackService.mediaPlayer.getDuration() / 1000);
        //mediaName.setText(mPService.VIDEO_SAMPLE);
        mediaName.setText(MediaPlaybackService.VIDEO_SAMPLE);
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse( "android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }

    /*public void Stop(View v) {
        StopPlayer();
    }*/

}
