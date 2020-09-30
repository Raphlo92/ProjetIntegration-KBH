package com.example.projet_dintgration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Started.");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view); // TODO régler erreur
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
        }); // TODO à tester
        navigationView.setCheckedItem(R.id.nav_home);
        NavigationManager.afficherOptionDeconnecteSpotify(navigationView.getMenu());
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
        Intent intent = new Intent(this, Music_Page.class);
        startActivity(intent);
    }
}

class NavigationManager implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "NavigationManager";

    Activity currentActivity;
    Context context;
    public NavigationManager(Activity current, Context packageContext){
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

    static public void afficherOptionConnecteSpotify(Menu menu) {
        Log.d(TAG, "afficherOptionConnecteSpotify: Started");
        menu.findItem(R.id.nav_spotify_lier).setVisible(false);
        modifierVisibiliteMenu(true, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout);
    }

    static public void afficherOptionDeconnecteSpotify(Menu menu) {
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
}