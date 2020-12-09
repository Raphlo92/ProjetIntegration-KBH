package com.example.projetdintegration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

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
    ImageButton playButton;
    ImageButton shuffleButton;
    ImageButton repeatButton;
    ImageView coverArt;
    int playingId = 0;
    private static final String TAG = "MediaActivity";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_activity);

        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
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
                public void gotoMedia() {
                }
            });
            navigationView.setCheckedItem(R.id.nav_mediaActivity);
            NavigationManager.determinerOptionsAfficher(navigationView.getMenu());



            final TextView pageTitle = (TextView) findViewById(R.id.PageTitle);
            pageTitle.setText(R.string.nav_mediaActivity);

            final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
            final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
            imageView1.setVisibility(View.INVISIBLE);
            imageView2.setVisibility(View.INVISIBLE);
        }

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
                    if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify")){
                        MediaPlaybackService.spotifyPlayer.seekTo(progress * 1000);
                    }else {
                        MediaPlaybackService.mediaPlayer.seekTo(progress * 1000);
                        videoView.seekTo(progress * 1000);
                    }
                    String time = progress % (1000 * 60 * 60) / (1000 * 60) + ":" + (progress % (1000 * 60 * 60) % (1000 * 60) / 1000);
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
        if(LierSpotifyActivity.appRemote != null && LierSpotifyActivity.appRemote.isConnected()){
            LierSpotifyActivity.appRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {
                    Log.d(TAG,"EventCall in spotify");
                    manageSeekBar(playerState.playbackPosition);
                    MediaPlaybackService.spotifyPlayerState = playerState;
                    setInfosSpotify();
                }
            });
        }
        SeekBarUpdater();
        //InfoUpdater();
    }

    public class GestionnairePlayPause implements View.OnClickListener {
        public void onClick(View v) {
            if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify")){
                if(!mPService.playing){
                    try{
                        mPService.PlayFromPause();
                    }catch (IOException ioe){ioe.printStackTrace();}
                    playButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    mPService.playing = true;
                }else {
                    if(mPService.playing && !MediaPlaybackService.spotifyPlayerState.isPaused){
                        mPService.Pause();
                    }
                    playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    mPService.playing = false;
                }
            }else if (MediaPlaybackService.mediaPlayer != null) {
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
                if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify"))
                    setInfosSpotify();
                else
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
                if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify"))
                    setInfosSpotify();
                else
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
        if(!MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify")) {
            Uri videoUri = MediaPlaybackService.getMedia();
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setVolume(0, 0);
                }
            });
        }
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
                    if(!MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify")) {
                        videoView.start();
                        SetInfos();
                    }else {
                        setInfosSpotify();
                    }
                }else
                {
                    initializePlayer();
                    if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify") && MediaPlaybackService.spotifyPlayerState != null){
                        setInfosSpotify();
                    }else if(MediaPlaybackService.mediaPlayer != null && (!MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify"))){
                        SetInfos();
                        videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
                        videoView.start();
                    }
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
                if(MediaPlaybackService.musicArrayList.get(MediaPlaybackService.playingId).getType().equals("Spotify")){
                    if(MediaPlaybackService.spotifyPlayerState != null && !MediaPlaybackService.spotifyPlayerState.isPaused){
                        manageSeekBar((seekBar.getProgress() + 1) * 1000);
                    }
                    handler.postDelayed(this,1000);
                }else if (MediaPlaybackService.mediaPlayer != null) {
                    int currentPosition = MediaPlaybackService.mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition / 1000);
                    int minutes = currentPosition / (60 * 1000);
                    int seconds = (currentPosition / 1000) % 60;
                    String time = String.format("%d:%02d", minutes, seconds);
                    currentTime.setText(time);
                    handler.postDelayed(this, 100);
                }
            }
        });
    }
    private void manageSeekBar(long playbackPosition) {
        int totalTimeMinutes = (int) (playbackPosition / (1000 * 60));
        int totalTimeSeconds = (int)((playbackPosition / 1000) % 60);
        seekBar.setProgress((int)playbackPosition / 1000);
        currentTime.setText(String.format("%d:%02d", totalTimeMinutes, totalTimeSeconds));
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
                if(MediaPlaybackService.playing) {
                    videoView.start();
                    videoView.seekTo(MediaPlaybackService.mediaPlayer.getCurrentPosition());
                }
            }
        }*/
    public void SetInfos(){

        if (MediaPlaybackService.mediaPlayer != null && !MediaPlaybackService.musicArrayList.get(playingId).getType().equals("Spotify")){
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
        }else if(MediaPlaybackService.musicArrayList.get(playingId).getType().equals("Spotify"))
            setInfosSpotify();
        else{
            videoView.setVisibility(View.INVISIBLE);
            coverArt.setVisibility(View.VISIBLE);
            coverArt.setImageResource(R.drawable.ic_music_note_24);
        }
    }

    public void setInfosSpotify(){
        if(MediaPlaybackService.spotifyPlayerIsReady && MediaPlaybackService.spotifyPlayerState != null && MediaPlaybackService.spotifyPlayerState.track != null && MediaPlaybackService.musicArrayList.get(playingId).getType().equals("Spotify")){
            int totalTimeMinutes = (int)MediaPlaybackService.spotifyPlayerState.track.duration / (1000 * 60);
            int totalTimeSeconds = (int)((MediaPlaybackService.spotifyPlayerState.track.duration / 1000) % 60);
            maxTime.setText(String.format("%d:%02d", totalTimeMinutes, totalTimeSeconds));
            seekBar.setMax((int)(MediaPlaybackService.spotifyPlayerState.track.duration / 1000));
            String fullTrackName = MediaPlaybackService.spotifyPlayerState.track.name + " - " +
                    MediaPlaybackService.spotifyPlayerState.track.album.name + " de " + MediaPlaybackService.spotifyPlayerState.track.artist.name;
            mediaName.setText(fullTrackName);
            videoView.setVisibility(View.INVISIBLE);
            coverArt.setVisibility(View.VISIBLE);
            ImagesApi imagesApi = LierSpotifyActivity.appRemote.getImagesApi();
            imagesApi.getImage(MediaPlaybackService.spotifyPlayerState.track.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                @Override
                public void onResult(Bitmap bitmap) {
                    coverArt.setAdjustViewBounds(false);
                    coverArt.setImageBitmap(bitmap);
                    coverArt.setAdjustViewBounds(true);
                }
            });
            coverArt.setImageResource(R.drawable.ic_music_note_24);
            coverArt.setAdjustViewBounds(true);
        }
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse( "android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }


}
