package com.example.projetdintegration;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.google.android.material.navigation.NavigationView;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

import java.io.File;
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
    Categories DBCategoriesReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int playlistId = getIntent().getIntExtra(DBHelper.Contract.TablePlaylist._ID, -1);

        dbHelper = new DBHelper(getApplicationContext());
        DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        DBCategoriesReader = new Categories(dbHelper.getReadableDatabase());
        DBMusicsWriter = new Musics(dbHelper.getWritableDatabase());

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

        //final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        //imageView2.setImageResource(R.drawable.ic_add);
        //imageView2.setVisibility(View.INVISIBLE);

        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();

        if (playlistId > -1) {
            Playlists DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
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

        final RecyclerView[] scrollviews = {
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
        };

        //final ListView listView2 = (ListView) findViewById(R.id.listView2);

        //final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        //imageView2.setImageResource(R.drawable.ic_add);
        //imageView2.setVisibility(View.INVISIBLE);

        ArrayList<IDBClass> list = DBCategoriesReader.Select(null, null, null, null, null, null);
        ArrayList<IDBClass> categoriesUsed = DBMusicsReader.getAllUsedCategories();
        ArrayList<Category> categories = new ArrayList<>();
        Random rand = new Random();

        //Determiner 10 categories random
        //Pour chaques categories ajouter dynamiquement les musiques
        //En utilisant des adapters
        ArrayList<IDBClass> dbMusics;
        ArrayList<Music> musics;

        int max = Math.min(categoriesUsed.size(), 10);

        for (int i = 0; i < max; i++) {
            dbMusics = new ArrayList<>();
            musics = new ArrayList<>();
            int randomIndex = rand.nextInt(categoriesUsed.size());
            IDBClass randomElement = categoriesUsed.get(randomIndex);
            Category RandCat = (Category) randomElement;
            categories.add(RandCat);
            categoriesUsed.remove(randomIndex);

            String whereClause = DBHelper.Contract.TableMusic.COLUMN_NAME_ID_CATEGORY + " = ?";
            String[] whereArgs = {RandCat.getId() + ""};


            dbMusics = DBMusicsReader.Select(null, whereClause, whereArgs, null, null, null);
            musics = new ArrayList<>();

            for (IDBClass music : dbMusics) {
                //music = dbMusics.get(rand.nextInt(dbMusics.size()));
                musics.add((Music) music);
            }

            FileListAdapter adapter = new FileListAdapter(this, R.layout.mainactivity_imagebutton_adapter, musics);
            scrollviewTitles[i].setText(RandCat.getName());
            scrollviews[i].setAdapter(adapter);
            LinearLayoutManager layout = new LinearLayoutManager(this);
            layout.setOrientation(RecyclerView.HORIZONTAL);
            scrollviews[i].setLayoutManager(layout);

        }

    }
}