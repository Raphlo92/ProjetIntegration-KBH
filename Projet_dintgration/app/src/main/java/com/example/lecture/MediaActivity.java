package com.example.lecture;

import android.media.MediaPlayer;
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


import androidx.appcompat.app.AppCompatActivity;

public class MediaActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    SurfaceView surfaceView;
    Handler handler = new Handler();
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
        surfaceView = findViewById(R.id.surfaceView);

        playButton.setOnClickListener(new GestionnairePlay());
        pauseButton.setOnClickListener(new GestionnairePause());
        stopButton.setOnClickListener(new GestionnaireStop());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
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

    public void SeekBarUpdater(){
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void Play(View v) {
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bladee);
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();



            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    StopPlayer();
            }
            });
        }
        mediaPlayer.start();
    }

    public void Pause(View v)
    {
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    public void StopPlayer(){
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "mediaPlayer released", Toast.LENGTH_LONG).show();
        }
    }

    public void Stop(View v){
        StopPlayer();
    }
    @Override
    protected void onStop(){
        super.onStop();
        StopPlayer();
    }

}
