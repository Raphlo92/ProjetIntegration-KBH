package com.example.projetdintegration;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "40353253f030456297bcab99af268e6c";
    public static final String REDIRECT_URI = "com.example.projetdintegration://callback";

    //just a simple comment
    private static boolean firstRun = true;
    private static final String TAG = "MainActivity";
    Context context = this;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    DBHelper dbHelper;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int playlistId = getIntent().getIntExtra(DBHelper.Contract.TablePlaylist._ID, -1);

        dbHelper = new DBHelper(getApplicationContext());
        DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        DBMusicsWriter = new Musics(dbHelper.getWritableDatabase());

        Log.d(TAG, "onCreate: Started.");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer_description,
                R.string.navigation_close_drawer_description);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationManager(this,this) {
            @Override
            public void gotoHome(){ }
        });
        navigationView.setCheckedItem(R.id.nav_home);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());



        final ListView listView = (ListView) findViewById(R.id.listView);
        final TextView pageTitle = (TextView) findViewById(R.id.PageTitle);
        pageTitle.setText(R.string.nav_home);

        //final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        //imageView2.setImageResource(R.drawable.ic_add);
        //imageView2.setVisibility(View.INVISIBLE);

        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();

        if(playlistId > -1){
            Playlists DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
            dbMusics = DBPlaylistsReader.getAllMusicsInPlaylist(playlistId);
        }
        else{
            dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
        }

        for (IDBClass music: dbMusics) {
            musics.add((Music) music);
        }

        FileListAdapter adapter = new FileListAdapter(this, R.layout.mainactivity_adapter_layout, musics);
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Started");
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }


    public void openMediaActivity(View v) {
        Log.d(TAG, "openMediaActivity: Started");
        Intent intent = new Intent(this, MediaActivity.class);
        startActivity(intent);
    }
}