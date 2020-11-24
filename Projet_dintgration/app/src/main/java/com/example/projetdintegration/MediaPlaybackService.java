package com.example.projetdintegration;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.VideoView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.io.IOException;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MediaPlaybackService extends Service {
    private static final String TAG = "MediaPlaybackService";
    public static boolean running = false;
    public static MediaPlayer mediaPlayer;
    private static MediaPlaybackService instance = null;
    static Boolean playing = true;
    static int playingId = 0;
    //static String[] mediaList = {"bladee", "boku", "sea", "tacoma_narrows"};
    static ArrayList<Music> musicArrayList = new ArrayList<>();
    static Notification mediaPlayingNotification;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        MediaPlaybackService getService() {
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

    public void updateMusicList(ArrayList<Music> playlist, int songId){
        Log.d(TAG, "updateMusicList: playingId = " + songId);
        musicArrayList = playlist;
        playingId = songId;
        try{
            RestartPlayer();
            PlayFromPause();
        }catch (IOException e){}
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
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            PlayNext();
                        } catch (IOException e) {
                            StopPlayer();
                        }
                    }
                });
            }
            if(!running) {
                mediaPlayer.start();
                running = true;
            }
            startForeground(1, mediaPlayingNotification);
        }
    }

    public void PlayFromPause() throws IOException {
        if(mediaPlayer != null) {
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            playing = false;
            running = false;
        }
    }

    public void initializePlayer() throws IOException {
        if(mediaPlayer == null) {
            Uri file = getMedia();
            mediaPlayer = MediaPlayer.create(this, file);
        }
    }

    public void PlayNext() throws IOException, NullPointerException {
        if(playingId < musicArrayList.size() - 1){
            playingId++;
            RestartPlayer();
        }
        else if(playingId == musicArrayList.size() - 1){
            playingId = 0;
            RestartPlayer();
        }
    }

    public void PlayPrevious(View v) throws IOException {
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

    public void RestartPlayer() throws IOException, NullPointerException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            initializePlayer();
            updateNotification();
            playing = false;
            PlayFromPause();
            playing = true;
            running = true;
        }
    }

    public void Stop(View v) {
        StopPlayer();
    }

    public void Pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    static Uri getMedia() {
        if (musicArrayList.size() != 0)
            return Uri.parse(musicArrayList.get(playingId).getPath());
        return null;
    }

}
