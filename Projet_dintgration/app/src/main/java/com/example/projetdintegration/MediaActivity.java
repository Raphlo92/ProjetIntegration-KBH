package com.example.projetdintegration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.projetdintegration.DBHelpers.DBInitializer;
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
    ImageButton playButton;
    ImageButton shuffleButton;
    ImageButton repeatButton;
    ImageView coverArt;
    int playingId = 0;
    private static final String TAG = "MediaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);

        playButton = findViewById(R.id.playButton);
        shuffleButton = findViewById(R.id.shuffleButton);
        repeatButton = findViewById(R.id.repeatButton);
        ImageButton rewindButton = findViewById(R.id.rewindButton);
        ImageButton forwardButton = findViewById(R.id.forwardButton);
        seekBar = findViewById(R.id.seekBar);

        currentTime = findViewById(R.id.currentTime);
        maxTime = findViewById(R.id.maxTime);
        mediaName = findViewById(R.id.mediaName);
        videoView = findViewById(R.id.videoView);
        coverArt = findViewById(R.id.coverArt);

        playButton.setOnClickListener(new GestionnairePlayPause());
        rewindButton.setOnClickListener(new GestionnaireRewind());
        forwardButton.setOnClickListener(new GestionnaireForward());
        shuffleButton.setOnClickListener(new GestionnaireShuffle());
        repeatButton.setOnClickListener(new GestionnaireRepeat());

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
        //InfoUpdater();
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

    public class GestionnaireShuffle implements View.OnClickListener{
        public void onClick(View v){
            if(MediaPlaybackService.musicArrayList.size() != 0) {
                if (!MediaPlaybackService.shuffle) {
                    shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
                    mPService.shuffleMusicList();
                    MediaPlaybackService.shuffle = true;
                } else {
                    shuffleButton.setImageResource(R.drawable.ic_baseline_trending_flat_24);
                    mPService.resetMusicList();
                    MediaPlaybackService.shuffle = false;
                }
            }
        }
    }

    public class GestionnaireRepeat implements View.OnClickListener{
        public void onClick(View v){
            if(!MediaPlaybackService.repeat){
                repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
                MediaPlaybackService.repeat = true;
            }
            else{
                repeatButton.setImageResource(R.drawable.ic_baseline_norepeat);
                MediaPlaybackService.repeat = false;
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
                    initializePlayer();
                    SetInfos();
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
                    int minutes = currentPosition / (60 * 1000);
                    int seconds = (currentPosition / 1000) % 60;
                    String time = String.format("%d:%02d", minutes, seconds);
                    currentTime.setText(time);
                }
                handler.postDelayed(this, 100);
            }
        });
    }

    /*public void InfoUpdater(){
        MediaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetInfos();
                handler.postDelayed(this, 1000);
            }

        });
    }*/

    @Override
    public void onResume(){
        super.onResume();
        RefreshButtons();
    }

    public void RefreshButtons(){
        if(MediaPlaybackService.shuffle)
            shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
        else
            shuffleButton.setImageResource(R.drawable.ic_baseline_trending_flat_24);
        if(MediaPlaybackService.repeat)
            repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
        else
            repeatButton.setImageResource(R.drawable.ic_baseline_norepeat);
        if(MediaPlaybackService.playing)
            playButton.setImageResource(R.drawable.ic_baseline_pause_24);
        else
            playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
    }

    /*public void Pause() {
        mPService.Pause();
    }*/

    /*public void PlayNext(View v) throws IOException {
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
    }*/

    /*public void PlayPrevious(View v) throws IOException {
        if(playingId == 0 || videoView.getCurrentPosition()/1000 > 5){
            RestartPlayer(v);
        }
        else {
            playingId--;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer(v);
        }
    }*/

    /*public void RestartPlayer(View v) throws IOException {
        mPService.RestartPlayer();
    }*/

    /*public void SetInfos(){
        if (MediaPlaybackService.mediaPlayer != null){
            playingId = MediaPlaybackService.playingId;
            int minutes = MediaPlaybackService.mediaPlayer.getDuration() / (60 * 1000);
            int seconds = (MediaPlaybackService.mediaPlayer.getDuration() / 1000) % 60;
            String time = String.format("%d:%02d", minutes, seconds);
            maxTime.setText(time);
            seekBar.setMax(MediaPlaybackService.mediaPlayer.getDuration() / 1000);
            mediaName.setText(MediaPlaybackService.musicArrayList.get(playingId).getName());
            if(MediaPlaybackService.musicArrayList.get(playingId).getType().equals("audio")){
                videoView.setVisibility(View.INVISIBLE);
                coverArt.setVisibility(View.VISIBLE);
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(MediaPlaybackService.musicArrayList.get(playingId).getPath());
                byte[] data = mmr.getEmbeddedPicture();
                Log.i(TAG, "SetInfos: data = " + data );

                if(data != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    coverArt.setImageBitmap(bitmap);
                }
                else{
                    coverArt.setImageResource(R.drawable.ic_music_note_24);
                }
                coverArt.setAdjustViewBounds(true);
            }
            else {
                videoView.setVisibility(View.VISIBLE);
                coverArt.setVisibility(View.INVISIBLE);
                if (videoView == null) {
                    initializePlayer();
                }
                if(MediaPlaybackService.playing) {
                    videoView.start();
                    videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
                }
            }
        }*/
    public void SetInfos(){

        if (MediaPlaybackService.mediaPlayer != null){
            playingId = MediaPlaybackService.playingId;
            int minutes = MediaPlaybackService.mediaPlayer.getDuration() / (60 * 1000);
            int seconds = (MediaPlaybackService.mediaPlayer.getDuration() / 1000) % 60;
            String time = String.format("%d:%02d", minutes, seconds);
            maxTime.setText(time);
            seekBar.setMax(MediaPlaybackService.mediaPlayer.getDuration() / 1000);
            mediaName.setText(MediaPlaybackService.musicArrayList.get(playingId).getName());
            if(MediaPlaybackService.musicArrayList.get(playingId).getType().contains("audio")){
                videoView.setVisibility(View.INVISIBLE);
                coverArt.setVisibility(View.VISIBLE);
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(MediaPlaybackService.musicArrayList.get(playingId).getPath());
                byte[] data = mmr.getEmbeddedPicture();
                Log.i(TAG, "SetInfos: data = " + data );

                if(data != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    coverArt.setImageBitmap(bitmap);
                }
                else{
                    coverArt.setImageResource(R.drawable.ic_music_note_24);
                }
                coverArt.setAdjustViewBounds(true);
            }
            else {
                videoView.setVisibility(View.VISIBLE);
                coverArt.setVisibility(View.INVISIBLE);
                if (videoView == null) {
                    initializePlayer();
                }
                videoView.start();
                videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
            }
        }
        else{
            videoView.setVisibility(View.INVISIBLE);
            coverArt.setVisibility(View.VISIBLE);
            coverArt.setImageResource(R.drawable.ic_music_note_24);
        }
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse( "android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }


}
