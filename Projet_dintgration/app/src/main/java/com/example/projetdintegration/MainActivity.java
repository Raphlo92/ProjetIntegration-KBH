package com.example.projetdintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.google.android.material.navigation.NavigationView;

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


        if (firstRun) {
            firstRun = false;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    //ArrayList<File> files = MusicFileExplorer.getAllMusicFiles();
                    ArrayList<File> files = new ArrayList<>();
                    MusicFileExplorer.getAllChildren(MusicFileExplorer.DIRECTORY_MUSIC, files);
                    new DBInitializer(context).Init(files);
                }
            });

            th.start();
        }

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



        final ListView listView = (ListView) findViewById(R.id.listView1);

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

class NavigationManager implements NavigationView.OnNavigationItemSelectedListener {
    
    private static final String TAG = "NavigationManager";

    AppCompatActivity currentActivity;
    Context context;
    public NavigationManager(AppCompatActivity current, Context packageContext){
        Log.d(TAG, "NavigationManager: Created");
        currentActivity = current;
        context = packageContext;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected: Started");
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                Log.d(TAG, "onNavigationItemSelected: Switched to home");
                gotoHome();
                break;
            case R.id.nav_favoris:
                Log.d(TAG, "onNavigationItemSelected: Switched to favoris");
                gotoFavoris();
                break;
            case R.id.nav_liste_lecture:
                Log.d(TAG, "onNavigationItemSelected: Switched to liste de lecture");
                gotoListeLecture();
                break;
            case R.id.nav_bibliotheque:
                Log.d(TAG, "onNavigationItemSelected: Switched to bibliotheque");
                gotoBibliotheque();
                break;
            /*case R.id.nav_music_page:
                Log.d(TAG, "onNavigationItemSelected: Switched to bibliotheque");
                //gotoBibliotheque();
                startActivity(MediaActivity.class);
                break;*/
            case R.id.nav_spotify_lier:
                gotoLierSpotify();
                break;
            case R.id.nav_spotify_bibliotheque:
                gotoBibliothequeSpotify();
                break;
            // TODO terminer toutes les options du switch case
        }
        return true;
    }



    private void startActivity(Class<?> cls){
        Log.d(TAG, "startActivity: Started");
        currentActivity.startActivity(new Intent(context,cls));
    }

    public void gotoHome() {
        Log.d(TAG, "gotoHome: Started");
        startActivity(MainActivity.class);
    }

    public void gotoFavoris() {
        Log.d(TAG, "gotoFavoris: Started");
        //TODO startActivity();
    }

    public void gotoBibliotheque() {
        Log.d(TAG, "gotoBibliotheque: Started");
        startActivity(MusicListActivity.class);
    }

    public void gotoListeLecture() {
        Log.d(TAG, "gotoListeLecture: Started");
        // TODO startActivity();
    }

    public void gotoLierSpotify(){
        startActivity(LierSpotifyActivity.class);
    }
    public void gotoBibliothequeSpotify(){

    }
    static public void afficherOptionConnecteSpotify(Menu menu) {
        Log.d(TAG, "afficherOptionConnecteSpotify: Started");
        menu.findItem(R.id.nav_spotify_lier).setVisible(false);
        modifierVisibiliteMenu(true, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout);
    }


    static private void afficherOptionDeconnecteSpotify(Menu menu) {
        Log.d(TAG, "afficherOptionDeconnecteSpotify: Started");
        menu.findItem(R.id.nav_spotify_lier).setVisible(true);
        modifierVisibiliteMenu(false, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout);
    }

    static private void modifierVisibiliteMenu(boolean estVisible, Menu menu, int... options) {
        Log.d(TAG, "modifierVisibiliteMenu: Started");
        for (int option : options) {
            menu.findItem(option).setVisible(estVisible);
        }
    }
    static public void determinerOptionsAfficher(Menu menu){
        if(LierSpotifyActivity.appRemote != null && LierSpotifyActivity.appRemote.isConnected()){
            afficherOptionConnecteSpotify(menu);
        }else{
            afficherOptionDeconnecteSpotify(menu);
        }
    }
}