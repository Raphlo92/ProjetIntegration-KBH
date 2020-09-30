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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void openMusicPage() {
        Intent intent = new Intent(this, Music_Page.class);
        startActivity(intent);
    }
}

class NavigationManager implements NavigationView.OnNavigationItemSelectedListener {

    Activity currentActivity;
    Context context;
    public NavigationManager(Activity current, Context packageContext){
        currentActivity = current;
        context = packageContext;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                gotoHome();
                break;
            case R.id.nav_favoris:
                gotoFavoris();
                break;
            case R.id.nav_liste_lecture:
                gotoListeLecture();
                break;
            case R.id.nav_bibliotheque:
                gotoBibliotheque();
                break;
            // TODO terminer toutes les options du switch case
        }
        return true;
    }

    private void startActivity(Class<?> cls){
        currentActivity.startActivity(new Intent(context,cls));
    }

    public void gotoHome() {
        startActivity(MainActivity.class);
    }

    public void gotoFavoris() {
        //TODO startActivity();
    }

    public void gotoBibliotheque() {
        startActivity(Music_Page.class);
    }

    public void gotoListeLecture() {
        // TODO startActivity();
    }

    static public void afficherOptionConnecteSpotify(Menu menu) {
        menu.findItem(R.id.nav_spotify_lier).setVisible(false);
        modifierVisibiliteMenu(true, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout);
    }

    static public void afficherOptionDeconnecteSpotify(Menu menu) {
        menu.findItem(R.id.nav_spotify_lier).setVisible(true);
        modifierVisibiliteMenu(false, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout);
    }

    static private void modifierVisibiliteMenu(boolean estVisible, Menu menu, int... options) {
        for (int option : options) {
            menu.findItem(option).setVisible(estVisible);
        }
    }
}