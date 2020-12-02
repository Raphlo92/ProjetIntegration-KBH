package com.example.projetdintegration;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ListView;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.Utilities.PopupHelper;
import com.google.android.material.navigation.NavigationView;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

import java.util.ArrayList;
import java.util.Random;

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
    DBHelper dbHelper;
    Musics DBMusicsReader;
    Musics DBMusicsWriter;
    MediaPlaybackService.LocalBinder binder = HomeActivity.binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PopupHelper popupHelper = new PopupHelper(this);

        ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView1.setVisibility(View.INVISIBLE);

        int playlistId = getIntent().getIntExtra(DBHelper.Contract.TablePlaylist._ID, -1);

        dbHelper = new DBHelper(getApplicationContext());
        DBMusicsReader = new Musics(dbHelper.getWritableDatabase(), context);
        DBMusicsWriter = new Musics(dbHelper.getReadableDatabase(), context);

        Log.d(TAG, "onCreate: Started.");

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
            public void gotoHome() {}
        });
        navigationView.setCheckedItem(R.id.nav_home);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());

        try {
            scrollView_UI();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        final ListView listView = (ListView) findViewById(R.id.listView);
        final TextView pageTitle = (TextView) findViewById(R.id.PageTitle);
        pageTitle.setText(R.string.nav_home);

        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();

        if (playlistId > -1) {
            Playlists DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase(), context);
            dbMusics = DBPlaylistsReader.getAllMusicsInPlaylist(playlistId);
        } else {
            dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
        }

        for (IDBClass music : dbMusics) {
            musics.add((Music) music);
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

    public void scrollView_UI() throws InterruptedException {

        /*final RecyclerView[] scrollviews = {
                (RecyclerView) findViewById(R.id.slideshow1),
                (RecyclerView) findViewById(R.id.slideshow2),
                (RecyclerView) findViewById(R.id.slideshow3),
                (RecyclerView) findViewById(R.id.slideshow4),
                (RecyclerView) findViewById(R.id.slideshow5),
                (RecyclerView) findViewById(R.id.slideshow6),
                (RecyclerView) findViewById(R.id.slideshow7),
                (RecyclerView) findViewById(R.id.slideshow8),
                (RecyclerView) findViewById(R.id.slideshow9),
                (RecyclerView) findViewById(R.id.slideshow10),
        };

        final TextView[] scrollviewTitles = {
                (TextView) findViewById(R.id.slideshow1Title),
                (TextView) findViewById(R.id.slideshow2Title),
                (TextView) findViewById(R.id.slideshow3Title),
                (TextView) findViewById(R.id.slideshow4Title),
                (TextView) findViewById(R.id.slideshow5Title),
                (TextView) findViewById(R.id.slideshow6Title),
                (TextView) findViewById(R.id.slideshow7Title),
                (TextView) findViewById(R.id.slideshow8Title),
                (TextView) findViewById(R.id.slideshow9Title),
                (TextView) findViewById(R.id.slideshow10Title)
        };*/

        //final ListView listView2 = (ListView) findViewById(R.id.listView2);

        //final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        //imageView2.setImageResource(R.drawable.ic_add);
        //imageView2.setVisibility(View.INVISIBLE);

        ArrayList<IDBClass> categoriesUsed = DBMusicsReader.getAllUsedCategories();
        ArrayList<Category> categories = new ArrayList<>();
        Random rand = new Random();

        Log.d(TAG, "scrollView_UI: categoriesUsed.size() = " + categoriesUsed.size());

        //Determiner 10 categories random
        //Pour chaques categories ajouter dynamiquement les musiques
        //En utilisant des adapters

        int max = Math.min(categoriesUsed.size(), 10);

        for (int i = 0; i < max; i++) {
            int randomIndex = rand.nextInt(categoriesUsed.size());
            IDBClass randomElement = categoriesUsed.get(randomIndex);
            Category RandCat = (Category) randomElement;
            Log.d(TAG, "scrollView_UI: RandCat: \n" +
                    "name = " + RandCat.getName());
            categories.add(RandCat);
            categoriesUsed.remove(randomIndex);




            //scrollviewTitles[i].setText(RandCat.getName());
            //scrollviews[i].setAdapter(adapter);
            //scrollviews[i].setLayoutManager(layout);
        }
        CategorieListAdapter adapter = new CategorieListAdapter(this, R.layout.mainactivity_adapter_layout, categories, binder);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(RecyclerView.VERTICAL);
        final RecyclerView catList = (RecyclerView) findViewById(R.id.categorie_list);
        catList.setAdapter(adapter);
        catList.setLayoutManager(layout);

    }
}