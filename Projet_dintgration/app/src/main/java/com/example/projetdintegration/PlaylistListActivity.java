package com.example.projetdintegration;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.Utilities.PopupHelper;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class PlaylistListActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistActivity";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    static DBHelper dbHelper;
    Playlists DBPlaylistsReader;
    Playlists DBPlaylistsWriter;
    PlaylistListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Log.d(TAG, "onCreate: Started.");

        //region DB
        dbHelper = new DBHelper(getApplicationContext());
        DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
        DBPlaylistsWriter = new Playlists(dbHelper.getWritableDatabase());
        //endregion



        PopupHelper popupHelper = new PopupHelper(this);

        //testing categories for Kevin ;)
        String[] columns = {
                DBHelper.Contract.TableCategory._ID,
                DBHelper.Contract.TableCategory.COLUMN_NAME_NAME
        };
        ArrayList<IDBClass> list = new Categories(dbHelper.getReadableDatabase()).Select(columns, null, null, null, null, null);
        Log.d(TAG, "onCreate: listSize = " + list.size());
        for (IDBClass item : list) {
            Log.d(TAG, "onCreate: Categories: cat = " + item.getName());
        }


        final TextView pageTitle = (TextView) findViewById(R.id.PageTitle);
        pageTitle.setText(R.string.nav_liste_lecture);

        final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView1.setVisibility(View.INVISIBLE);

        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView2.setImageResource(R.drawable.ic_add);
        imageView2.setVisibility(View.VISIBLE);
        imageView2.setOnClickListener(view -> {
            Log.d(TAG, "imageView2: onClickListener() ");
            popupHelper.showCreateForm();
        });

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
        navigationView.setNavigationItemSelectedListener(new NavigationManager(this, this) {
            @Override
            public void gotoListeLecture() {

            }
        });
        navigationView.setCheckedItem(R.id.nav_liste_lecture);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
        //endregion

        final ListView listView = (ListView) findViewById(R.id.listView);

        ArrayList<IDBClass> dbPlaylists = DBPlaylistsReader.Select(null, null, null, null, null, null);
        ArrayList<Playlist> playlists = new ArrayList<>();

        for (IDBClass playlist : dbPlaylists) {
            playlists.add((Playlist) playlist);
        }


        adapter = new PlaylistListAdapter(this, R.layout.playlist_listitem_layout, playlists);
        listView.setAdapter(adapter);

    }

    public static void RefreshView(Context context){
        Playlists DBPlaylistsReader = new Playlists(new DBHelper(context).getReadableDatabase());
        ArrayList<IDBClass> dbPlaylists = DBPlaylistsReader.Select(null, null, null, null, null, null);
        ArrayList<Playlist> playlists = new ArrayList<>();

        for (IDBClass playlist : dbPlaylists) {
            playlists.add((Playlist) playlist);
        }

        //adapter.clear();
        //adapter.addAll(playlists);
        //adapter.notifyDataSetChanged();

    }
}
