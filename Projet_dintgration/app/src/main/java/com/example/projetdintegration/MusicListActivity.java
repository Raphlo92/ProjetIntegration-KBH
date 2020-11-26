package com.example.projetdintegration;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.Utilities.PopupHelper;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;

public class MusicListActivity extends AppCompatActivity {
    private static final String TAG = "MusicListActivity";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    DBHelper dbHelper;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;
    Playlists DBPlaylistsReader;
    static MusicListAdapter adapter;
    static int playlistId;
    MediaPlaybackService.LocalBinder binder;
    Service mPService;
    boolean mPBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service){
                Log.d(TAG, "onServiceConnected: binder Created");
                binder = (MediaPlaybackService.LocalBinder) service;
                mPService = binder.getService();
                mPBound = true;
                generateListView(playlistId);
            }
            @Override
            public void onServiceDisconnected(ComponentName arg0){
                mPBound = false;
            }
        };
        Intent intent = new Intent(this, MediaPlaybackService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        playlistId = getIntent().getIntExtra(DBHelper.Contract.TablePlaylist._ID, -1);
        setContentView(R.layout.activity_list);

        PopupHelper popupHelper = new PopupHelper(this);

        final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView1.setImageResource(R.drawable.ic_add);
        imageView1.setVisibility(View.VISIBLE);
        imageView1.setOnClickListener(view -> {
            Log.d(TAG, "imageView1: onClickListener() ");
            popupHelper.showSearchForm();
        });

        Log.d(TAG, "onCreate: Started.");
        Log.d(TAG, "onCreate: playlistId: " + playlistId);

        //region DB
        dbHelper = new DBHelper(getApplicationContext());
        DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        DBMusicsWriter = new Musics(dbHelper.getWritableDatabase());
        DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
        //

        Categories.exists(dbHelper.getReadableDatabase(), "Ro");

        //region Navigation
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer_description,
                R.string.navigation_close_drawer_description);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (playlistId > -1){
            navigationView.setNavigationItemSelectedListener(new NavigationManager(this,this));
            if (playlistId == DBPlaylistsReader.getFavoritesId()){
                navigationView.setCheckedItem(R.id.nav_favoris);
            }
            else{
                navigationView.setCheckedItem(R.id.nav_liste_lecture);
            }
        }
        else{
            navigationView.setNavigationItemSelectedListener(new NavigationManager(this,this) {
                @Override
                public void gotoBibliotheque(){ }
            });
            navigationView.setCheckedItem(R.id.nav_bibliotheque);
        }


        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
        //endregion
    }

    void generateListView(int playlistId){
        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();
        final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        final TextView pageTitle = (TextView) findViewById(R.id.PageTitle);
        pageTitle.setText(R.string.nav_bibliotheque);

        imageView1.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        if(playlistId > -1){

            dbMusics = DBPlaylistsReader.getAllMusicsInPlaylist(playlistId);
            if(playlistId != DBPlaylistsReader.getFavoritesId()){
                Playlist playlist = Playlists.getPlaylistById(dbHelper.getReadableDatabase(), playlistId);
                PopupHelper popupHelper = new PopupHelper(this);

                pageTitle.setText(playlist.getName());
                imageView2.setImageResource(R.drawable.ic_baseline_more_vert_24);
                imageView2.setOnClickListener(view -> {
                    Log.d(TAG, "imageView2: onClickListener() ");
                    popupHelper.showPlaylistOptions(view, playlist);
                });

                imageView1.setVisibility(View.INVISIBLE);
                imageView2.setVisibility(View.VISIBLE);
            }
            else{
                pageTitle.setText(R.string.nav_favoris);
            }
        }
        else{

            dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
            imageView1.setImageResource(R.drawable.ic_baseline_search_24);
            imageView2.setImageResource(R.drawable.androidlogo);
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);


        final ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new MusicListAdapter(this, R.layout.music_listitem_layout, musics, playlistId, binder);
        listView.setAdapter(adapter);

        }
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Started");
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public static void RefreshView(Context context){
        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context);
        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        Musics DBMusicsWriter = new Musics(dbHelper.getWritableDatabase());
        Playlists DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
        if(playlistId > -1){
            dbMusics = DBPlaylistsReader.getAllMusicsInPlaylist(playlistId);
        }
        else{
            dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
        }

        for (IDBClass music: dbMusics) {
            musics.add((Music) music);
        }

        adapter.clear();
        adapter.addAll(musics);
        adapter.notifyDataSetChanged();

    }

    public static void RefreshViewFromList(Context context, ArrayList<Music> musics){
        adapter.clear();
        adapter.addAll(musics);
        adapter.notifyDataSetChanged();
    }
}
