package com.example.projet_dintgration;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.VideoView;


import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MediaActivity extends AppCompatActivity{

    SeekBar seekBar;
    Handler handler = new Handler();
    VideoView videoView;
    private static final String VIDEO_SAMPLE = "tacoma_narrows";
    private static final String TAG = "MediaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);

        Button playButton = findViewById(R.id.playButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);
        seekBar = findViewById(R.id.seekBar);
        videoView = findViewById(R.id.videoView);

        playButton.setOnClickListener(new GestionnairePlay());
        pauseButton.setOnClickListener(new GestionnairePause());
        stopButton.setOnClickListener(new GestionnaireStop());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(videoView != null && fromUser){
                    videoView.seekTo(progress*1000);
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
    }

    public class GestionnairePause implements View.OnClickListener {
        public void onClick(View v){
            Log.d(TAG, "onClick: paused");
            Pause(v);
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
        videoView.setVideoURI(videoUri);
    }

    private void releasePlayer(){
        videoView.stopPlayback();
    }

    @Override
    protected void onStart(){
        super.onStart();
        initializePlayer();
        videoView.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        StopPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoView.pause();
        }
    }

    private Uri getMedia(String mediaName)
    {
        return Uri.parse("android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }

    public void SeekBarUpdater(){
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(videoView != null){
                    int currentPosition = videoView.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void Play(View v) {
        if(videoView == null) {
            initializePlayer();
            seekBar.setMax(videoView.getDuration()/1000);
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    StopPlayer();
            }
            });
        }
        videoView.start();
    }

    public void Pause(View v)
    {
        if(videoView != null){
            videoView.pause();
        }
    }

    public void StopPlayer(){
        if(videoView != null) {
            releasePlayer();
            videoView = null;
            Toast.makeText(this, "mediaPlayer released", Toast.LENGTH_LONG).show();
        }
    }

    public void Stop(View v){
        StopPlayer();
    }

}
