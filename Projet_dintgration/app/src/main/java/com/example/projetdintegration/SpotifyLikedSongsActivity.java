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
import android.telecom.Call;
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

public class SpotifyLikedSongsActivity extends AppCompatActivity {
    static final String TAG = "SpotifyLikedSongs";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    MediaPlaybackService.LocalBinder binder;
    ContentApi contenu;
    ListView listView;
    boolean userScrolled;
    RelativeLayout progressBar;
    Boolean noMoreDataToFetch;
    int startIndexOfDataFetch;
    ImageView image;
    ImagesApi imagesApi;
    CallResult.ResultCallback<ListItems> fetchChildrenCallback;
    CallResult.ResultCallback<ListItems> displayListItemsCallBack;
    AdapterView.OnItemClickListener playableListener;
    AbsListView.OnScrollListener onScrollListener;
    ListItem likedSongsItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view_layout);
        progressBar = (RelativeLayout) findViewById(R.id.loadItemsListView);
        listView = (ListView) findViewById(R.id.list_spotify_bibliotheque_start);
        image = findViewById(R.id.album_view_image);
        imagesApi = LierSpotifyActivity.appRemote.getImagesApi();
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
            public void gotoLikedSongsSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_chanson_aimee);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }
    private void initializeListeners() {
        playableListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyNavigationItem selectedItem =new SpotifyNavigationItem(((SpotifyMusic) parent.getItemAtPosition(position)).transformToListItem());
                Log.i("SpotifyLikedSongs",selectedItem.baseNavigationItem.toString());
                if (SpotifyMusicListActivity.determineIfHasChildren(selectedItem, new SpotifyNavigationItem(likedSongsItem)))
                    sendPlayableToMusicPlayer(position);
                else
                    getElementChildren(selectedItem.getBaseListItem(), displayListItemsCallBack);

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
                    loadMoreContent(likedSongsItem);
                }
            }
        };
    }
    private void initializeSpotifyResultCallBacks(){
        fetchChildrenCallback = new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                for(ListItem item : listItems.items)
                    if(item.id.contains(SpotifyMusicListActivity.SPOTIFY_COLLECTION_LINK)) {
                        likedSongsItem = item;
                        ((TextView)findViewById(R.id.PageTitle)).setText(likedSongsItem.title);
                        imagesApi.getImage(likedSongsItem.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                            @Override
                            public void onResult(Bitmap bitmap) {
                                image.setImageBitmap(bitmap);
                            }
                        });
                        getElementChildren(item, displayListItemsCallBack);
                    }else if (item.id.equals(SpotifyMusicListActivity.SPOTIFY_ID_LIBRARY) || item.id.equals(SpotifyMusicListActivity.SPOTIFY_ID_PLAYLIST))
                        getElementChildren(item, fetchChildrenCallback);
            }
        };
        displayListItemsCallBack = new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                ArrayList<SpotifyNavigationItem> navigationItems = new ArrayList<>();
                for (ListItem item : listItems.items){
                    navigationItems.add(new SpotifyNavigationItem(item));
                }
                SpotifyMusicListActivity.navigationList = new SpotifyNavigationList(navigationItems);
                displayListItems(navigationItems);
            }
        };
    }
    public void sendPlayableToMusicPlayer(int position) {
        Intent intent = new Intent(this, MediaActivity.class);
        binder.getService().PlayNow(SpotifyMusicListActivity.navigationList.musics,position);
        startActivity(intent);
    }
    private void initializeListViewContent(){
        noMoreDataToFetch = false;
        startIndexOfDataFetch = 0;
        userScrolled = false;
        contenu = LierSpotifyActivity.appRemote.getContentApi();
        Log.i(TAG, "In initializeListViewContent");
        contenu.getRecommendedContentItems(LierSpotifyActivity.CONTENT_API_RECOMMENDED_CALL).setResultCallback(fetchChildrenCallback);
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
        SpotifyMusicListActivity.navigationList.transformToXML(listView,this,binder);
        SpotifyMusicListActivity.navigationList.setListOnClickListener(playableListener,listView);
        SpotifyMusicListActivity.navigationList.setListOnScrollListener(onScrollListener,listView);
    }
    private void getElementChildren(ListItem item, CallResult.ResultCallback<ListItems> callback){
        contenu.getChildrenOfItem(item,SpotifyMusicListActivity.NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(callback);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}