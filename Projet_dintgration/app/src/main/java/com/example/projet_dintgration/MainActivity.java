package com.example.projet_dintgration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private static int SPLASH_TIME_OUT = 400;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                 Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                 startActivity(homeIntent);
                 finish();
            }
        },SPLASH_TIME_OUT);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMusicPage();
            }
        });
    }
    public void openMusicPage(){
        Intent intent = new Intent(this, Music_Page.class);
        startActivity(intent);
    }
}