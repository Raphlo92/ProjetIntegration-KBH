package com.example.projetdintegration;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.util.function.Consumer;

public class LierSpotifyActivity extends AppCompatActivity {

    private static final String TAG = "LierSpotifyActivity";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    public static SpotifyAppRemote appRemote;
    public static final String CONTENT_API_RECOMMENDED_CALL = "default-cars";
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
        connexionSpotify(this, getDefaultConnectionListener(this,this,this::connected));
    }

    public static void connexionSpotify(Context context,Connector.ConnectionListener listener){
        ConnectionParams connectionParams = new ConnectionParams.Builder(MainActivity.CLIENT_ID)
                .setRedirectUri(MainActivity.REDIRECT_URI).showAuthView(true).build();
        SpotifyAppRemote.connect(context, connectionParams,listener);
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    private void connected(SpotifyAppRemote spotifyAppRemote) {
        appRemote = spotifyAppRemote;
        Log.i(TAG, "set SharedPreferences Value");
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(MainActivity.OPTIONS_DEJA_CONNECTE_SPOTIFY,true);
        editor.apply();
        startActivity(new Intent(this, SpotifyMusicListActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(MainActivity.OPTIONS_DEJA_CONNECTE_SPOTIFY,true);
        editor.apply();
    }

    public static Connector.ConnectionListener getDefaultConnectionListener(Context context, AppCompatActivity currentActivity, Consumer<SpotifyAppRemote> fonctionToCallOnConnection){
        return new Connector.ConnectionListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                fonctionToCallOnConnection.accept(spotifyAppRemote);
            }
            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, throwable.getMessage(), throwable);
                if(throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException){

                }else if (throwable instanceof CouldNotFindSpotifyApp) {
                    createAlertBox(context,"Spotify n'a pas été trouvé, voulez-vous télécharger l'application du Google Play Store ?",
                            "OUI", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try{
                                        currentActivity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.spotify.music")));
                                    }catch (ActivityNotFoundException anfe){
                                        currentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
                                    }
                                }
                            },"NON", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
                Log.d("SpotifyConnectionError",throwable.getMessage(), throwable);
            }
        };
    }
    public static AlertDialog.Builder createAlertBox(Context context, String message, String positiveButtonText,
                                              DialogInterface.OnClickListener positiveResponseAction,
                                              String negativeButtonText,
                                              DialogInterface.OnClickListener negativeResponseAction){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setPositiveButton(positiveButtonText,positiveResponseAction)
                .setNegativeButton(negativeButtonText,negativeResponseAction);
        return builder;
    }
}