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

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MediaPlaybackService extends Service {

    public static boolean running = false;
    public static MediaPlayer mediaPlayer;
    static Boolean playing = true;
    static int playingId = 0;
    static String[] mediaList = {"bladee", "boku", "sea", "tacoma_narrows"};
    static String VIDEO_SAMPLE = mediaList[playingId];
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

        //NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mediaPlayingNotification = new Notification.Builder(this, "MAC")
                .setContentTitle("Media playing")
                .setContentText(VIDEO_SAMPLE)
                .setSmallIcon(R.drawable.ic_music_note_24)
                .setTicker("ok?")
                .build();
        stopForeground(true);
        startForeground(1, mediaPlayingNotification);
    }

    @Override
    public IBinder onBind(Intent intent){
        mediaPlayingNotification =
                new Notification.Builder(this, "MAC")
                        .setContentTitle("Media playing")
                        .setContentText(VIDEO_SAMPLE)
                        .setSmallIcon(R.drawable.ic_music_note_24)
                        .setTicker("ok?")
                        .build();

        createNotificationChannel();
        startForeground(1, mediaPlayingNotification);

        return binder;
    }

    public void Play() throws IOException {
        if (mediaPlayer == null) {
            initializePlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    StopPlayer();
                }
            });
        }
        if(!running) {
            mediaPlayer.start();
            running = true;
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
            Uri file = getMedia(VIDEO_SAMPLE);
            mediaPlayer = MediaPlayer.create(this, file);
        }
    }

    public void PlayNext() throws IOException {
        if(playingId < mediaList.length - 1){
            playingId++;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer();
        }
        else if(playingId == mediaList.length - 1){
            playingId = 0;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer();
        }
    }

    public void PlayPrevious(View v) throws IOException {
        if(playingId == 0 || mediaPlayer.getCurrentPosition()/1000 > 5){
            RestartPlayer();
        }
        else {
            playingId--;
            VIDEO_SAMPLE = mediaList[playingId];
            RestartPlayer();
            PlayFromPause();
        }
    }

    public void RestartPlayer() throws IOException {
        mediaPlayer.stop();
        mediaPlayer = null;
        initializePlayer();
        updateNotification();
        playing = false;
        PlayFromPause();
        playing = true;
        running = true;
    }

    public void Stop(View v) {
        StopPlayer();
    }

    public void Pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse( "android.resource://" + getPackageName() +
                "/raw/" + mediaName);
    }

}
