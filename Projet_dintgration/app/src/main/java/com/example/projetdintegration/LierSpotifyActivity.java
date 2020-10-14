package com.example.projetdintegration;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

public class LierSpotifyActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    public static SpotifyAppRemote appRemote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lier_spotify);

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
            public void gotoLierSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_lier);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }

    @Override
    protected void onStart(){
        super.onStart();
        connexionSpotify(this, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                connected(spotifyAppRemote);
            }

            @Override
            public void onFailure(Throwable throwable) {
                if(throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException){

                }else if (throwable instanceof CouldNotFindSpotifyApp) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LierSpotifyActivity.this);
                    builder.setMessage("Spotify n'a pas été trouvé, voulez-vous télécharger l'application du Google Play Store ?")
                            .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try{
                                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.spotify.music")));
                                    }catch (android.content.ActivityNotFoundException anfe){
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                                    }
                                }
                            }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }

                Log.d("SpotifyConnectionError",throwable.getMessage(), throwable);
            }
        });
    }

    public static void connexionSpotify(Context context,Connector.ConnectionListener listener){
        ConnectionParams connectionParams = new ConnectionParams.Builder(MainActivity.CLIENT_ID)
                .setRedirectUri(MainActivity.REDIRECT_URI).showAuthView(true).build();
        SpotifyAppRemote.connect(context, connectionParams,listener);
    }

    private void connected(SpotifyAppRemote spotifyAppRemote) {
        appRemote = spotifyAppRemote;
        startActivity(new Intent(this, SpotifyMusicListActivity.class));
    }
}