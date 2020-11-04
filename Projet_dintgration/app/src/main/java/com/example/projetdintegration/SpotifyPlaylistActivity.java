package com.example.projetdintegration;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;

import static com.example.projetdintegration.SpotifyMusicListActivity.SPOTIFY_COLLECTION_LINK;
import static com.example.projetdintegration.SpotifyMusicListActivity.SPOTIFY_ID_PLAYLIST;
import static com.example.projetdintegration.SpotifyMusicListActivity.SPOTIFY_PLAYLIST_LINK;

public class SpotifyPlaylistActivity extends AppCompatActivity {
    static final String TAG = "SpotifyPlaylist";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    ContentApi contenu;
    ListView listView;
    boolean userScrolled;
    SpotifyNavigationList navigationList;
    RelativeLayout progressBar;
    Boolean noMoreDataToFetch;
    int startIndexOfDataFetch;
    ImageView image;
    ImagesApi imagesApi;
    static SpotifyNavigationItem selectedPlaylist;
    AdapterView.OnItemClickListener playableListener;
    AbsListView.OnScrollListener onScrollListener;
    CallResult.ResultCallback<ListItems> fetchChildrenCallback;
    CallResult.ResultCallback<ListItems> displayListItemsCallBack;
    public static final String EXTRA_PLAYLIST_ITEM_SELECTED = "EXTRA_PLAYLIST_ITEM_SELECTED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        determineContentViewToSet();
        listView = findViewById(R.id.list_spotify_bibliotheque_start);
        progressBar = findViewById(R.id.loadItemsListView);
        initializeNavigationElements();
        initializeListeners();
        initializeSpotifyResultCallBacks();
        initializeListViewContent();
    }
    private void initializeNavigationElements(){
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
            public void gotoListeLectureSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_liste_lecture);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }
    private void initializeListeners() {
        playableListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyNavigationItem selectedItem = (SpotifyNavigationItem) parent.getItemAtPosition(position);
                if(SpotifyMusicListActivity.determineIfHasChildren(selectedItem))
                    sendPlayableToMusicPlayer(selectedItem.getURI());
                else{
                    selectedPlaylist = selectedItem;
                    reloadActivity(true);
                }
            }
        };
        onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    userScrolled = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {
                    userScrolled = false;
                    loadMoreContent(selectedPlaylist.getBaseListItem());
                }
            }
        };
    }
    private void initializeSpotifyResultCallBacks(){
        fetchChildrenCallback = new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                for(ListItem item : listItems.items)
                    if(item.id.equals(SpotifyMusicListActivity.SPOTIFY_ID_PLAYLIST)) {
                        selectedPlaylist = new SpotifyNavigationItem(item);
                        getElementChildren(item, displayListItemsCallBack);
                    }else if (item.id.equals(SpotifyMusicListActivity.SPOTIFY_ID_LIBRARY))
                        getElementChildren(item, fetchChildrenCallback);
            }
        };
        displayListItemsCallBack = new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                Log.i(TAG, listItems.toString());
                ArrayList<SpotifyNavigationItem> navigationItems = new ArrayList<>();
                for (ListItem item : listItems.items){
                    navigationItems.add(new SpotifyNavigationItem(item));
                }
                navigationList = new SpotifyNavigationList(navigationItems);
                displayListItems(navigationItems);
            }
        };
    }
    private void initializeListViewContent(){
        noMoreDataToFetch = false;
        startIndexOfDataFetch = 0;
        userScrolled = false;
        contenu = LierSpotifyActivity.appRemote.getContentApi();
        if(getIntent().getBooleanExtra(EXTRA_PLAYLIST_ITEM_SELECTED,false))
            getElementChildren(selectedPlaylist.getBaseListItem(),displayListItemsCallBack);
        else
            contenu.getRecommendedContentItems(LierSpotifyActivity.CONTENT_API_RECOMMENDED_CALL).setResultCallback(fetchChildrenCallback);
    }
    private void determineContentViewToSet(){
        if(selectedPlaylist != null) {
            if (!selectedPlaylist.getID().equals(SPOTIFY_ID_PLAYLIST))
                manageAlbumView();
            else
                setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.textView_categorie_name);
            categorieName.setText(R.string.spotify_playlist_view_base_text);
            categorieName.setText(selectedPlaylist.getTitle());
        }else{
            setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.textView_categorie_name);
            categorieName.setText(R.string.spotify_playlist_view_base_text);
        }
    }
    private void manageAlbumView() {
        setContentView(R.layout.album_view_layout);
        image = findViewById(R.id.album_view_image);
        imagesApi = LierSpotifyActivity.appRemote.getImagesApi();
        imagesApi.getImage(selectedPlaylist.getImageURI()).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
            @Override
            public void onResult(Bitmap bitmap) {
                image.setImageBitmap(bitmap);
            }
        });
    }
    private void sendPlayableToMusicPlayer(String uri) {
        Intent intent = new Intent(this,SpotifyMusicPlayer.class);
        intent.putExtra(SpotifyMusicPlayer.EXTRA_SPOTIFY_MUSIC_PLAYER_URI,uri);
        startActivity(intent);
    }
    private void loadMoreContent(ListItem item){
        if(!noMoreDataToFetch){
            startIndexOfDataFetch += SpotifyMusicListActivity.NUMBER_OF_ELEMENTS_TO_LOAD;
            progressBar.setVisibility(View.VISIBLE);
            addElementChildren(item);
        }
    }
    private void addElementChildren(ListItem item){
        contenu.getChildrenOfItem(item,SpotifyMusicListActivity.NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                if(listItems.items.length > 0) {
                    ArrayList<SpotifyNavigationItem> items = new ArrayList<>();
                    for (ListItem item : listItems.items) {
                        items.add(new SpotifyNavigationItem(item));
                    }
                    navigationList.updateContentInList(items, progressBar);
                }else {
                    progressBar.setVisibility(View.GONE);
                    noMoreDataToFetch = true;
                }
            }
        });
    }
    private void displayListItems(ArrayList<SpotifyNavigationItem> navigationItems){
        navigationList.transformToXML(listView,this);
        navigationList.setListOnClickListener(playableListener,listView);
        navigationList.setListOnScrollListener(onScrollListener,listView);
    }
    private void getElementChildren(ListItem item, CallResult.ResultCallback<ListItems> callback){
        contenu.getChildrenOfItem(item,SpotifyMusicListActivity.NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(callback);
    }
    private void reloadActivity(Boolean extraLastSelected){
        Intent intent = new Intent(this,SpotifyPlaylistActivity.class);
        intent.putExtra(EXTRA_PLAYLIST_ITEM_SELECTED,extraLastSelected);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

}