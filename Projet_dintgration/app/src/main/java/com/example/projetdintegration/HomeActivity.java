package com.example.projetdintegration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static boolean firstRun = true;
    private final int SPLASH_SREEN = 4000;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;
    DBHelper dbHelper;
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


        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(HomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(HomeActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_LONG)
                        .show();
                try {
                    wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System. exit(1);
            }

        };


        TedPermission.with(HomeActivity.this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        animtionsplashsreen();


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, DBInitializer.DBInitialisingService.class);
            startService(intent);

            ServiceConnection connection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    Log.d(TAG, "onServiceConnected: binder Created");
                    binder = (MediaPlaybackService.LocalBinder) service;
                    mPService = binder.getService();
                    mPBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {
                    mPBound = false;
                }
            };

            Intent MediaIntent = new Intent(this, MediaPlaybackService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
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