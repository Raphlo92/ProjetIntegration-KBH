package com.example.projetdintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static boolean firstRun = true;
    private final int SPLASH_SREEN = 4000;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;
    int dbVersion = 1;
    DBHelper dbHelper = new DBHelper(this);
    Context context = this;
    Animation topAnim, BottomAnim;
    ImageView image1, image2, logo;
    TextView slogan;
    Service mPService;
    boolean mPBound;
    static MediaPlaybackService.LocalBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        animtionsplashsreen();

        //Intent intent = new Intent(this, DBInitializer.DBInitialisingService.class);
        //startService(intent);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), dbVersion, DBHelper.DB_VERSION);

        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service){
                Log.d(TAG, "onServiceConnected: binder Created");
                binder = (MediaPlaybackService.LocalBinder) service;
                mPService = binder.getService();
                mPBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName arg0){
                mPBound = false;
            }
        };

        Intent MediaIntent = new Intent(this, MediaPlaybackService.class);
        bindService(MediaIntent, connection, Context.BIND_AUTO_CREATE);
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