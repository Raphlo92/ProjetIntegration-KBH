package com.example.projetdintegration;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MediaPlaybackService extends Service {
    private static final String TAG = "MediaPlaybackService";
    public static boolean running = false;
    public static boolean repeat = false;
    public static boolean shuffle = false;
    public static MediaPlayer mediaPlayer;
    public static PlayerApi spotifyPlayer;
    static Boolean playing = true;
    static boolean spotifyPlayerIsReady = false;
    public static int playingId = 0;
    public static boolean playNext = false;
    public static boolean trackHasStarted = false;
    public static PlayerState spotifyPlayerState;
    static ArrayList<Music> musicArrayList = new ArrayList<>();
    static ArrayList<Music> musicArrayListCopy = new ArrayList<>();
    static Notification mediaPlayingNotification;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MediaActivityChannel";
            String description = "MediaActivityChannel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("MAC", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotification(){

        mediaPlayingNotification = new Notification.Builder(this, "MAC")
                .setContentTitle("Media playing")
                .setContentText(musicArrayList.get(playingId).getName())
                .setSmallIcon(R.drawable.ic_music_note_24)
                .setTicker("ok?")
                .build();
        stopForeground(true);
        startForeground(1, mediaPlayingNotification);
    }

    @Override
    public IBinder onBind(Intent intent){
        /*mediaPlayingNotification =
                new Notification.Builder(this, "MAC")
                        .setContentTitle("Media playing")
                        .setContentText(musicArrayList.get(playingId).getName())
                        .setSmallIcon(R.drawable.ic_music_note_24)
                        .setTicker("ok?")
                        .build();*/

        createNotificationChannel();
        //startForeground(1, mediaPlayingNotification);

        return binder;
    }

    public void PlayNow(ArrayList<Music> playlist, int songId){
        Log.d(TAG, "updateMusicList: playingId = " + songId);
        Log.i(TAG,playlist.toString());
        //if(musicArrayList.isEmpty())
            musicArrayList = playlist;
        //else {

        //}

        playingId = songId;

        try{
            RestartPlayer();
            PlayFromPause();
            //pour avoir une copie en tout temps
            musicArrayListCopy.clear();
            musicArrayListCopy.addAll(musicArrayList);
            if(shuffle)
                shuffleMusicList();
        }catch (IOException e){
            Log.e(TAG, "updateMusicList: error " + e );
        }
    }

    public void Add(Music music)
    {
        musicArrayList.add(music);
        musicArrayListCopy.add(music);
    }

    public void AddNext(Music music){
        if(musicArrayList.size() == 0){
            Add(music);
            PlayNow(musicArrayList, 0);
        }
        else if(musicArrayList.size() == playingId + 1){
            Add(music);

        }
        else{
            musicArrayList.add(playingId + 1, music);
            musicArrayListCopy.add(playingId + 1, music);
        }
    }

    public void shuffleMusicList(){
        Music songPlaying = musicArrayList.get(playingId);
        Collections.shuffle(musicArrayList);
        playingId = musicArrayList.indexOf(songPlaying);
    }

    public void resetMusicList(){
        Music songPlaying = musicArrayList.get(playingId);
        musicArrayList.clear();
        musicArrayList.addAll(musicArrayListCopy);
        playingId = musicArrayList.indexOf(songPlaying);
        Log.e(TAG, "resetMusicList: " + playingId );
    }

    public void Play() throws IOException {
        if(musicArrayList.size() != 0){
            mediaPlayingNotification =
                    new Notification.Builder(this, "MAC")
                            .setContentTitle("Media playing")
                            .setContentText(musicArrayList.get(playingId).getName())
                            .setSmallIcon(R.drawable.ic_music_note_24)
                            .setTicker("ok?")
                            .build();
            if (mediaPlayer == null) {
                initializePlayer();
                /*mediaPlayer.setOnCompletionListener(mp -> {
                    try {
                        PlayNext();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });*/
            }
            if(!running) {
                if(musicArrayList.get(playingId).getType().equals("Spotify")){
                    spotifyPlayer.play(musicArrayList.get(playingId).getPath());
                }else
                    mediaPlayer.start();
                running = true;
            }
            startForeground(1, mediaPlayingNotification);
        }
    }

    public void PlayFromPause() throws IOException {
        if(musicArrayList.get(playingId).getType().equals("Spotify") && spotifyPlayerIsReady){
            if(!playing && spotifyPlayerState != null && spotifyPlayerState.isPaused){
                spotifyPlayer.resume();
                playing = true;
            }else{
                Play();
            }
        }else if(mediaPlayer != null) {
            if (!playing) {
                mediaPlayer.start();
                playing = true;
            }
        }
        else
        {
            Play(); //Pour si c'etait apres un Stop()
        }
    }

    public void StopPlayer() {
        if(spotifyPlayer != null && !spotifyPlayerState.isPaused)
            spotifyPlayer.pause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            playing = false;
            running = false;
        }
    }

    public void initializePlayer() throws IOException {
        if(mediaPlayer == null && !musicArrayList.get(playingId).getType().equals("Spotify")) {
            Uri file = getMedia();
            Log.e(TAG, "initializePlayer: file = " + file);
            mediaPlayer = MediaPlayer.create(this, file);
            mediaPlayer.setOnCompletionListener(mp -> {
                try {
                    PlayNext();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if(spotifyPlayer == null && LierSpotifyActivity.appRemote != null && LierSpotifyActivity.appRemote.isConnected()){
            spotifyPlayer = LierSpotifyActivity.appRemote.getPlayerApi();
            spotifyPlayer.subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
                @Override
                public void onEvent(PlayerState playerState) {
                    //boolean songPlayingIsNotRight = playerState.track.uri != spotifyPlayerState.track.uri || ;
                    Log.d(TAG,"EventCall in spotify");
                    if(!trackHasStarted && playerState.playbackPosition > 0 && playerState.track.duration > 0 && !playerState.isPaused)
                        trackHasStarted = true;
                    boolean hasEnded = trackHasStarted && playerState.isPaused && playerState.playbackPosition == 0;
                    if(spotifyPlayerState != null && hasEnded){
                        trackHasStarted = false;
                        try{
                            PlayNext();
                            spotifyPlayer.pause();
                        }catch (IOException ioe){ ioe.printStackTrace();}
                    }//else if(songPlayingIsNotRight && !playerState.isPaused){

                    //}
                    spotifyPlayerState = playerState;
                }
            });
            spotifyPlayerIsReady = true;
        }
    }

    public void PlayNext() throws IOException, NullPointerException {
        Log.i(TAG,repeat + " : repeat");
        Log.i(TAG, musicArrayList.toString());
        if(playingId < musicArrayList.size() - 1 && !repeat){
            playingId++;
            RestartPlayer();
        }
        else if(playingId == musicArrayList.size() - 1 && !repeat){
            playingId = 0;
            RestartPlayer();
        }else if(repeat){
            RestartPlayer();
        }
    }

    public void PlayPrevious(View v) throws IOException {
        Log.e(TAG, "PlayPrevious: " + playingId );
        if(musicArrayList.get(playingId).getType().equals("Spotify")) {
            if(spotifyPlayerIsReady){
                spotifyPlayer.pause();
                if(playingId == 0 ||(spotifyPlayerState != null && spotifyPlayerState.playbackPosition /1000 > 5))
                    RestartPlayer();
                else{
                    playingId--;
                    RestartPlayer();
                    PlayFromPause();
                }
            }
        }else{
            if (mediaPlayer != null) {
                if (playingId == 0 || mediaPlayer.getCurrentPosition() / 1000 > 5) {
                    RestartPlayer();
                } else {
                    playingId--;
                    RestartPlayer();
                    PlayFromPause();
                }
            }
        }
    }

    public void RestartPlayer() throws IOException, NullPointerException {
        if(spotifyPlayerIsReady && spotifyPlayerState != null && !spotifyPlayerState.isPaused){
            spotifyPlayer.pause();
            running = false;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        initializePlayer();
        updateNotification();
        playing = false;
        running = false;
        PlayFromPause();
        playing = true;
        running = true;
    }

    public void Stop(View v) {
        StopPlayer();
    }

    public void Pause() {
        if(musicArrayList.get(playingId).getType().equals("Spotify")) {
            if(spotifyPlayerIsReady){
                spotifyPlayer.pause();
            }
        }else{
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        }
    }

    static Uri getMedia() {
        if (musicArrayList.size() != 0) {
            return Uri.parse(musicArrayList.get(playingId).getPath());
        }
        return null;
    }

}
