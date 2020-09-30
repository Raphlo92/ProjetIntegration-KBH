package com.example.projet_dintgration;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Music_Page extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    ImageButton Playbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music__page);

    }

    public void play(View v){
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.boku);
            Playbutton=(ImageButton)findViewById(R.id.play);
            Playbutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(mediaPlayer.isPlaying()){
                        Playbutton.setBackgroundResource(R.drawable.play);
                    }
                    else{
                        Playbutton.setBackgroundResource(R.drawable.pause);
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopplayer();
                }
            });
        }
        mediaPlayer.start();
    }

    public void pause(View v){
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            Playbutton.setBackgroundResource(R.drawable.pause);
        }
    }

    public void stop(View v)
    {
        stopplayer();
    }

    private void stopplayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "mediaPlayer released", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        stopplayer();
    }
}