package com.example.projetdintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

import java.io.File;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {


    private static boolean firstRun = true;
    private final int SPLASH_SREEN = 4000;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;
    DBHelper dbHelper;
    Context context = this;
    Animation topAnim, BottomAnim;
    ImageView image1, image2, logo;
    TextView slogan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        animtionsplashsreen();

        Intent intent = new Intent(this, DBInitializer.DBInitialisingService.class);
        startService(intent);
    }

    public void animtionsplashsreen() {
        //this is for the splash sreen
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        BottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image1 = findViewById(R.id.musicdroite);
        image2 = findViewById(R.id.musicgauche);
        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.textView2);

        image2.setAnimation(topAnim);
        image1.setAnimation(topAnim);
        logo.setAnimation(topAnim);
        slogan.setAnimation(BottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SREEN);



    }
}