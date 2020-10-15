package com.example.projetdintegration;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Item;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;

public class SpotifyMusicListActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
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
            public void gotoBibliothequeSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_bibliotheque);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());

        final ListView listView = (ListView) findViewById(R.id.listView);
        ContentApi contenu = LierSpotifyActivity.appRemote.getContentApi();
        contenu.getRecommendedContentItems("default-cars").setResultCallback(new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                Log.i("InfoStart", "Début de l'info sur le list items");
                for (Item item : listItems.items
                     ) {
                    Log.i("DataListItem", item.toString());
                }
                Log.i("InfoStop", "Début de l'info sur le list items");
            }
        });
        ArrayList<Music> musics = new ArrayList<>();

    }
}