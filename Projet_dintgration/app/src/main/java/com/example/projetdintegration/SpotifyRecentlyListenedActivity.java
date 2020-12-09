package com.example.projetdintegration;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.projetdintegration.DBHelpers.Classes.SpotifyMusic;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;

import static com.example.projetdintegration.SpotifyMusicListActivity.SPOTIFY_ID_PLAYLIST;


public class SpotifyRecentlyListenedActivity extends AppCompatActivity {

    private static final String TAG = "SpotifyRecentlyListened";
    public static final String SPOTIFY_ID_RECENTLY_PLAYED = "com.spotify.recently-played";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    ContentApi contenu;
    ListView listView;
    boolean userScrolled;
    RelativeLayout progressBar;
    Boolean noMoreDataToFetch;
    int startIndexOfDataFetch;
    MediaPlaybackService.LocalBinder binder;
    ImageView image;
    ImagesApi imagesApi;
    static SpotifyNavigationItem selectedItem;
    AdapterView.OnItemClickListener playableListener;
    AbsListView.OnScrollListener onScrollListener;
    CallResult.ResultCallback<ListItems> fetchChildrenCallback;
    CallResult.ResultCallback<ListItems> displayListItemsCallBack;
    Service mPService;
    boolean mPBound;
    public static final String EXTRA_ITEM_SELECTED = "EXTRA_ITEM_SELECTED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service){
                binder = (MediaPlaybackService.LocalBinder) service;
                mPService = binder.getService();
                mPBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName arg0){
                mPBound = false;
            }
        };
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
            public void gotoRecentlyListenedSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_recently_listened);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }
    private void initializeListeners() {
        playableListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyNavigationItem currentSelectedItem = new SpotifyNavigationItem(((SpotifyMusic) parent.getItemAtPosition(position)).transformToListItem());
                if(SpotifyMusicListActivity.determineIfHasChildren(currentSelectedItem, selectedItem))
                    sendPlayableToMusicPlayer(position);
                else{
                    selectedItem = currentSelectedItem;
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
                    loadMoreContent(selectedItem.getBaseListItem());
                }
            }
        };
    }
    private void initializeSpotifyResultCallBacks(){
        fetchChildrenCallback = new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                for(ListItem item : listItems.items)
                    if(item.id.equals(SPOTIFY_ID_RECENTLY_PLAYED)) {
                        selectedItem = new SpotifyNavigationItem(item);
                        getElementChildren(item, displayListItemsCallBack);
                    }
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
                SpotifyMusicListActivity.navigationList = new SpotifyNavigationList(navigationItems);
                displayListItems(navigationItems);
            }
        };
    }
    private void initializeListViewContent(){
        noMoreDataToFetch = false;
        startIndexOfDataFetch = 0;
        userScrolled = false;
        contenu = LierSpotifyActivity.appRemote.getContentApi();
        if(getIntent().getBooleanExtra(EXTRA_ITEM_SELECTED,false))
            getElementChildren(selectedItem.getBaseListItem(),displayListItemsCallBack);
        else
            contenu.getRecommendedContentItems(LierSpotifyActivity.CONTENT_API_RECOMMENDED_CALL).setResultCallback(fetchChildrenCallback);
    }
    private void determineContentViewToSet(){
        if(selectedItem != null) {
            if (!selectedItem.getID().equals(SPOTIFY_ID_PLAYLIST))
                manageAlbumView();
            else
                setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.PageTitle);
            categorieName.setText(R.string.spotify_recently_played_base_text);
            categorieName.setText(selectedItem.getTitle());
        }else{
            setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.PageTitle);
            categorieName.setText(R.string.spotify_recently_played_base_text);
        }
    }
    private void manageAlbumView() {
        setContentView(R.layout.album_view_layout);
        image = findViewById(R.id.album_view_image);
        imagesApi = LierSpotifyActivity.appRemote.getImagesApi();
        imagesApi.getImage(selectedItem.getImageURI()).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
            @Override
            public void onResult(Bitmap bitmap) {
                image.setImageBitmap(bitmap);
            }
        });
    }
    public void sendPlayableToMusicPlayer(int position) {
        Intent intent = new Intent(this, MediaActivity.class);
        binder.getService().PlayNow(SpotifyMusicListActivity.navigationList.musics,position);
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
                    SpotifyMusicListActivity.navigationList.updateContentInList(items, progressBar);
                }else {
                    progressBar.setVisibility(View.GONE);
                    noMoreDataToFetch = true;
                }
            }
        });
    }
    private void displayListItems(ArrayList<SpotifyNavigationItem> navigationItems){
        SpotifyMusicListActivity.navigationList.transformToXML(listView,this, binder);
        SpotifyMusicListActivity.navigationList.setListOnClickListener(playableListener,listView);
        SpotifyMusicListActivity.navigationList.setListOnScrollListener(onScrollListener,listView);
    }
    private void getElementChildren(ListItem item, CallResult.ResultCallback<ListItems> callback){
        contenu.getChildrenOfItem(item,SpotifyMusicListActivity.NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(callback);
    }
    private void reloadActivity(Boolean extraLastSelected){
        Intent intent = new Intent(this, SpotifyRecentlyListenedActivity.class);
        intent.putExtra(EXTRA_ITEM_SELECTED,extraLastSelected);
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