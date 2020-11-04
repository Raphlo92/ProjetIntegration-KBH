package com.example.projetdintegration;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.google.android.material.navigation.NavigationView;

public class NavigationManager implements NavigationView.OnNavigationItemSelectedListener {

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
            case R.id.nav_spotify_logout:
                gotoLogoutSpotify();
                break;
            case R.id.nav_spotify_chanson_aimee:
                gotoLikedSongsSpotify();
                break;
            case R.id.nav_spotify_liste_lecture:
                gotoListeLectureSpotify();
                break;
            case R.id.nav_spotify_recently_listened:
                gotoRecentlyListenedSpotify();
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
        DBHelper dbHelper = new DBHelper(context);
        Playlists playlistsReader = new Playlists(dbHelper.getReadableDatabase());

        Intent intent = new Intent(context, MusicListActivity.class);
        intent.putExtra(DBHelper.Contract.TablePlaylist._ID, playlistsReader.getFavoritesId());
        context.startActivity(intent);
    }

    public void gotoBibliotheque() {
        Log.d(TAG, "gotoBibliotheque: Started");
        startActivity(MusicListActivity.class);
    }

    public void gotoListeLecture() {
        Log.d(TAG, "gotoListeLecture: Started");
        startActivity(PlaylistListActivity.class);
    }

    public void gotoLierSpotify(){
        startActivity(LierSpotifyActivity.class);
    }
    public void gotoBibliothequeSpotify(){ startActivity(SpotifyMusicListActivity.class);}
    protected void gotoLikedSongsSpotify(){startActivity(SpotifyLikedSongsActivity.class);}
    protected void gotoLogoutSpotify(){startActivity(SpotifyDeconnectionActivity.class);}
    protected void gotoListeLectureSpotify(){startActivity(SpotifyPlaylistActivity.class);}
    protected void gotoRecentlyListenedSpotify(){startActivity(SpotifyRecentlyListenedActivity.class);}
    static public void afficherOptionConnecteSpotify(Menu menu) {
        Log.d(TAG, "afficherOptionConnecteSpotify: Started");
        menu.findItem(R.id.nav_spotify_lier).setVisible(false);
        modifierVisibiliteMenu(true, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout,R.id.nav_spotify_recently_listened);
    }


    static private void afficherOptionDeconnecteSpotify(Menu menu) {
        Log.d(TAG, "afficherOptionDeconnecteSpotify: Started");
        menu.findItem(R.id.nav_spotify_lier).setVisible(true);
        modifierVisibiliteMenu(false, menu, R.id.nav_spotify_liste_lecture, R.id.nav_spotify_chanson_aimee,
                R.id.nav_spotify_bibliotheque, R.id.nav_spotify_logout,R.id.nav_spotify_recently_listened);
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
