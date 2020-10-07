package com.example.projet_dintgration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.Classes.Music;
import com.example.projet_dintgration.DBHelpers.DBHelper;
import com.example.projet_dintgration.DBHelpers.Musics;
import com.example.projet_dintgration.NavigationManager;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {
    private static final String TAG = "MusicListActivity";
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
        setContentView(R.layout.activity_music_list);
        Log.d(TAG, "onCreate: Started.");

        //region DB
        dbHelper = new DBHelper(getApplicationContext());
        DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        DBMusicsWriter = new Musics(dbHelper.getWritableDatabase());
        //

        //testValues
        dbHelper.enterTestValues();

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
        navigationView.setNavigationItemSelectedListener(new NavigationManager(this,this) {
            @Override
            public void gotoBibliotheque(){ }
        });
        navigationView.setCheckedItem(R.id.nav_bibliotheque);
        NavigationManager.afficherOptionDeconnecteSpotify(navigationView.getMenu());
        //endregion

        final ListView listView = (ListView) findViewById(R.id.musicListView);

        Music song1 = new Music(0, "Song1", 125, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song2 = new Music(0, "Song2", 180, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song3 = new Music(0, "Song3", 220, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song41 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song42 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song43 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song44 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song45 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song46 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song47 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song4 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song5 = new Music(0, "Song5", 195, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song6 = new Music(0, "Song6", 145, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song7 = new Music(0, "Song7", 105, "audio", "./nowhere", "Rock", "John", "Others", false);


        ArrayList<IDBClass> dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
        ArrayList<Music> musics = new ArrayList<>();

        for (IDBClass music: dbMusics) {
            musics.add((Music) music);
        }

        /*
        musics.add(song1);
        musics.add(song2);
        musics.add(song3);
        musics.add(song4);

        musics.add(song41);
        musics.add(song42);
        musics.add(song43);
        musics.add(song44);
        musics.add(song45);
        musics.add(song46);
        musics.add(song47);

        musics.add(song5);
        musics.add(song6);
        musics.add(song7);
        */
        MusicListAdapter adapter = new MusicListAdapter(this, R.layout.listitem_layout, musics);
        listView.setAdapter(adapter);

    }
}
