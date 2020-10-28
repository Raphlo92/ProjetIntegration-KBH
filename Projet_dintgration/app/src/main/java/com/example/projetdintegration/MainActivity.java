package com.example.projetdintegration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "40353253f030456297bcab99af268e6c";
    public static final String REDIRECT_URI = "com.example.projetdintegration://callback";
    public static final String OPTIONS_DEJA_CONNECTE_SPOTIFY = "CONNECTE_A_SPOTIFY";
    //just a simple comment
    private static boolean firstRun = true;

    private static final String TAG = "MainActivity";
    Context context = this;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        SharedPreferences options = getPreferences(MODE_PRIVATE);
        Log.i(TAG,"GEtSharedPreferencesOPtionVAlue: " + options.getBoolean(OPTIONS_DEJA_CONNECTE_SPOTIFY,false));
        if(options.getBoolean(OPTIONS_DEJA_CONNECTE_SPOTIFY,false) && LierSpotifyActivity.appRemote == null){
            LierSpotifyActivity.connexionSpotify(this,LierSpotifyActivity.getDefaultConnectionListener(this,this,this::connectedToSpotify));
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
        navigationView.setNavigationItemSelectedListener(new NavigationManager(this, this) {
            @Override
            public void gotoHome() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_home);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }

    private void connectedToSpotify(SpotifyAppRemote appRemote){
        LierSpotifyActivity.appRemote = appRemote;
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Started");
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void openMusicPage() {
        Log.d(TAG, "openMusicPage: Started");
        Intent intent = new Intent(this, MediaActivity.class);
        startActivity(intent);
    }
}

class NavigationManager implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "NavigationManager";

    AppCompatActivity currentActivity;
    Context context;

    public NavigationManager(AppCompatActivity current, Context packageContext) {
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
            case R.id.nav_mediaActivity:
                Log.d(TAG, "onNavigationItemSelected: Switched to bibliotheque");
                startActivity(MediaActivity.class);
                break;
            case R.id.nav_spotify_lier:
                gotoLierSpotify();
                break;
            case R.id.nav_spotify_bibliotheque:
                gotoBibliothequeSpotify();
                break;
            case R.id.nav_spotify_chanson_aimee:
                gotoLikedSongsSpotify();
                break;
            case R.id.nav_spotify_liste_lecture:
                gotoListeLectureSpotify();
                break;
            case R.id.nav_spotify_logout:
                gotoDeconnectionSpotify();
                break;
        }
        return true;
    }
    private void startActivity(Class<?> cls) {
        Log.d(TAG, "startActivity: Started");
        currentActivity.startActivity(new Intent(context, cls));
    }
    protected void gotoHome() {
        Log.d(TAG, "gotoHome: Started");
        startActivity(MainActivity.class);
    }
    protected void gotoFavoris() {
        Log.d(TAG, "gotoFavoris: Started");
        //TODO startActivity();
    }
    protected void gotoBibliotheque() {
        Log.d(TAG, "gotoBibliotheque: Started");
        startActivity(MusicListActivity.class);
    }
    protected void gotoListeLecture() {
        Log.d(TAG, "gotoListeLecture: Started");
        // TODO startActivity();
    }
    protected void gotoLierSpotify(){
        startActivity(LierSpotifyActivity.class);
    }
    protected void gotoBibliothequeSpotify(){
        startActivity(SpotifyMusicListActivity.class);
    }
    protected void gotoLikedSongsSpotify(){startActivity(SpotifyLikedSongsActivity.class);}
    protected void gotoListeLectureSpotify() { startActivity(SpotifyPlaylistActivity.class);}
    protected void gotoDeconnectionSpotify(){startActivity(SpotifyDeconnectionActivity.class);}
    static private void afficherOptionConnecteSpotify(Menu menu) {
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