package com.example.projet_dintgration;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MediaActivity extends AppCompatActivity {

    SeekBar seekBar;
    Handler handler = new Handler();
    VideoView videoView;
    TextView currentTime;
    TextView maxTime;
    TextView mediaName;
    Boolean playing = true;
    ImageButton playButton;
    int playingId = 0;
    static String[] mediaList = {"bladee", "boku", "sea", "tacoma_narrows"};
    String VIDEO_SAMPLE = mediaList[0];
    private static final String TAG = "MediaActivity";

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
        videoView = findViewById(R.id.videoView);
        currentTime = findViewById(R.id.currentTime);
        maxTime = findViewById(R.id.maxTime);
        mediaName = findViewById(R.id.mediaName);

        playButton.setOnClickListener(new GestionnairePlayPause());
        stopButton.setOnClickListener(new GestionnaireStop());
        rewindButton.setOnClickListener(new GestionnaireRewind());
        forwardButton.setOnClickListener(new GestionnaireForward());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (videoView != null && fromUser) {
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

    public class GestionnairePlayPause implements View.OnClickListener {
        public void onClick(View v) {
            if(!playing) {
                Log.d(TAG, "onClick: playing");
                Play(v);
                playButton.setImageResource(R.drawable.ic_baseline_pause_24);
                playing = true;
            }
            else{
                Log.d(TAG, "onClick: paused");
                Pause(v);
                playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                playing = false;
            }
        }
    }

    public class GestionnaireStop implements View.OnClickListener {
        public void onClick(View v) {
            Log.d(TAG, "onClick: stopped");
            Stop(v);
        }
    }

    public class GestionnaireRewind implements View.OnClickListener{
        public void onClick(View v){
            PlayPrevious(v);
        }
    }

    public class GestionnaireForward implements View.OnClickListener{
        public void onClick(View v){
            PlayNext(v);
        }
    }

    public void initializePlayer() {
        if (videoView == null) {
            videoView = findViewById(R.id.videoView);
        }
        Uri videoUri = getMedia(VIDEO_SAMPLE);
        videoView.setVideoURI(videoUri);
    }

    @Override
    protected void onStart() {
        super.onStart();
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
        playing = true;
        initializePlayer();
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
            SetInfos();
            }
        });
    }

    @Override
    protected void onStop() {
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

    private Uri getMedia(String mediaName) {
        return Uri.parse("android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }

    public void SeekBarUpdater() {
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (videoView != null) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBar.setProgress(currentPosition / 1000);
                    String time = currentPosition % (1000*60*60) / (1000*60) + ":" + (currentPosition % (1000 * 60 * 60) % (1000 * 60) / 1000);
                    currentTime.setText(time);
                }
                handler.postDelayed(this, 100);
            }
        });
    }

    public void Play(View v) {
        if (videoView == null) {
            initializePlayer();
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    StopPlayer();
                }
            });
        }
        videoView.start();
        playButton.setImageResource(R.drawable.ic_baseline_pause_24);
    }

    public void Pause(View v) {
        if (videoView != null) {
            videoView.pause();
        }
    }

    public void StopPlayer() {
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
            playing = false;
            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    public void PlayNext(View v) {
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

    public void PlayPrevious(View v) {
        if(playingId == 0 || videoView.getCurrentPosition()/1000 > 5){
            RestartPlayer(v);
        }
        else {
            playingId--;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer(v);
        }
    }

    public void RestartPlayer(View v){
        Stop(v);
        Play(v);
        playing = true;
    }

    public void SetInfos(){
        String time = videoView.getDuration() % (1000*60*60) / (1000*60) + ":" + (videoView.getDuration() % (1000 * 60 * 60) % (1000 * 60) / 1000);
        maxTime.setText(time);
        seekBar.setMax(videoView.getDuration() / 1000);
        mediaName.setText(VIDEO_SAMPLE);
    }

    public void Stop(View v) {
        StopPlayer();
    }

}
