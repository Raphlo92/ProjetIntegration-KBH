package com.example.projetdintegration;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.Classes.SpotifyMusic;
import com.example.projetdintegration.Utilities.SpotifyLibraryManager;
import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.android.appremote.api.UserApi;
import com.spotify.android.appremote.internal.ImagesApiImpl;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.ImageIdentifier;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.Item;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SpotifyMusicListActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    ArrayList<Music> musics;
    ContentApi contenu;
    ListView listView;
    AdapterView.OnItemClickListener navigationListener;
    AdapterView.OnItemClickListener playableListener;
    public static SpotifyNavigationList navigationList;
    RelativeLayout progressBar;
    Boolean noMoreDataToFetch;
    int startIndexOfDataFetch;
    MediaPlaybackService.LocalBinder binder;
    public static final String SPOTIFY_ID_LIBRARY = "com.spotify.your-library";
    public static final String SPOTIFY_COLLECTION_LINK = "collection";
    public static final String SPOTIFY_ID_PLAYLIST = "com.spotify.your-playlists";
    public static final String SPOTIFY_ID_ALBUMS = "com.spotify.your-albums";
    public static final String SPOTIFY_ID_PODCASTS = "com.spotify.your-podcasts";
    public static final String SPOTIFY_ID_ARTISTS = "com.spotify.your-artists";
    public static final String SPOTIFY_ID_LIKED_SONGS_PLAYLIST = "spotify:user:" + ":collection";
    public static final String SPOTIFY_ARTIST_LINK = "artist";
    public static final String SPOTIFY_ALBUM_LINK = "album";
    public static final String SPOTIFY_PLAYLIST_LINK = "playlist";
    public static final String SPOTIFY_TRACK_LINK = "track";
    public static final String SPOTIFY_SHUFFLE_IMAGE_NAME = "ic_eis_shuffle";
    public static final String EXTRA_SPOTIFY_URI = "EXTRA_SPOTIFY_URI";
    boolean userScrolled;
    public static final int NUMBER_OF_ELEMENTS_TO_LOAD = 15;
    public static final String EXTRA_LIST_ITEM_SELECTED = "EXTRA_LIST_ITEM_SELECTED";
    static public ListItem lastSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getIntent().getBooleanExtra(EXTRA_LIST_ITEM_SELECTED, false))
            lastSelected = null;
        determineContentViewToSet();
        progressBar = findViewById(R.id.loadItemsListView);
        initializeNavigationElements();
        initializeOnClickListItemListeners();
        listView = (ListView) findViewById(R.id.list_spotify_bibliotheque_start);
        //initializeAdapter();
        initializeListViewContent();
    }

//    private void initializeAdapter(){
//        adapter = new MusicListAdapter(this,R.layout.music_listitem_layout,musics,-1, binder);
//    }
    private void TestAddFonctionOfLibraryManager() {
        SpotifyLibraryManager libraryManager = new SpotifyLibraryManager(LierSpotifyActivity.appRemote.getUserApi());
        //libraryManager.addToLibrary("spotify:album:5i7MWkomxEzODJS6ZNJO2l");
        //libraryManager.getLibraryState("spotify:album:5i7MWkomxEzODJS6ZNJO2l",SpotifyLibraryManager.getBaseLibraryStateResult());
    }

    private void determineContentViewToSet(){
        if(lastSelected != null) {
            if ((lastSelected.id.contains(SPOTIFY_ALBUM_LINK) && !lastSelected.id.equals(SPOTIFY_ID_ALBUMS)) ||
                    lastSelected.id.contains(SPOTIFY_PLAYLIST_LINK) && !lastSelected.id.equals(SPOTIFY_ID_PLAYLIST))
                manageAlbumView();
            else
                setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.PageTitle);
            categorieName.setText(lastSelected.title);
        }else {
            setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.PageTitle);
            categorieName.setText(R.string.spotify_bibliotheque_default_categorie);
        }
    }
    private void manageAlbumView() {
        setContentView(R.layout.album_view_layout);
        ImageView image = findViewById(R.id.album_view_image);
        ImagesApi imagesApi = LierSpotifyActivity.appRemote.getImagesApi();
        imagesApi.getImage(lastSelected.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
            @Override
            public void onResult(Bitmap bitmap) {
                image.setImageBitmap(bitmap);
            }
        });
    }
    private void initializeListViewContent(){
        noMoreDataToFetch = false;
        startIndexOfDataFetch = 0;
        userScrolled = false;
        contenu = LierSpotifyActivity.appRemote.getContentApi();
        String extraUri = getIntent().getStringExtra(EXTRA_SPOTIFY_URI);
        if(extraUri != null && extraUri.isEmpty()){
            LierSpotifyActivity.appRemote.call(extraUri, null, ListItems.class).setResultCallback(new CallResult.ResultCallback<ListItems>() {
                @Override
                public void onResult(ListItems listItems) {
                    Log.i("SpotifyCallTest", listItems.toString());
                }
            });
        }else if(getIntent().getBooleanExtra(EXTRA_LIST_ITEM_SELECTED,false)) {
            getElementChildren(lastSelected);
        }else{
            contenu.getRecommendedContentItems(LierSpotifyActivity.CONTENT_API_RECOMMENDED_CALL).setResultCallback(new CallResult.ResultCallback<ListItems>() {
                @Override
                public void onResult(ListItems listItems) {
                    for (ListItem item : listItems.items
                    ) {
                        if (item.id.equals(SPOTIFY_ID_LIBRARY)) {
                            getElementChildren(item);
                        }
                    }
                }
            });
        }
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
            public void gotoBibliothequeSpotify() {
            }
        });
        navigationView.setCheckedItem(R.id.nav_spotify_bibliotheque);
        NavigationManager.determinerOptionsAfficher(navigationView.getMenu());
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    private void initializeOnClickListItemListeners() {
        navigationListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyMusic selectedItem = (SpotifyMusic) parent.getItemAtPosition(position);
                ListItem spotifyItem = selectedItem.transformToListItem();
                Log.i("SpotifyMusicList",spotifyItem.hasChildren + " " + spotifyItem.playable);
                lastSelected = spotifyItem;
                reloadActivity(true);
            }
        };
        playableListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyNavigationItem selectedItem = new SpotifyNavigationItem(((SpotifyMusic) parent.getItemAtPosition(position)).transformToListItem());
                if(determineIfHasChildren(selectedItem, new SpotifyNavigationItem(lastSelected)))
                    sendPlayableToMusicPlayer(selectedItem.getURI());
                else{
                    lastSelected = selectedItem.getBaseListItem();
                    reloadActivity(true);
                }
            }
        };
    }
    public static boolean determineIfHasChildren(SpotifyNavigationItem selectedItem, SpotifyNavigationItem lastSelected){
        //Returns true for playable
        return !selectedItem.hasChildren() && (!selectedItem.getID().contains(SPOTIFY_ALBUM_LINK) || selectedItem.getID().equals(lastSelected.getID())) &&
                (!selectedItem.getID().contains(SPOTIFY_ARTIST_LINK) ||
                 getSpotifyIdUniqueKeyPart(selectedItem.getID()).equals(getSpotifyIdUniqueKeyPart(lastSelected.getID()))) &&
                !selectedItem.getID().contains(SPOTIFY_PLAYLIST_LINK) &&
                (!selectedItem.getID().contains(SPOTIFY_COLLECTION_LINK) || selectedItem.getImageURI().toString().contains(SPOTIFY_SHUFFLE_IMAGE_NAME));
    }
    public static String getSpotifyIdUniqueKeyPart(String spotifyId){
        return spotifyId.substring(spotifyId.lastIndexOf(':') + 1);
    }
    private void sendPlayableToMusicPlayer(String uri) {
        Intent intent = new Intent(this,SpotifyMusicPlayer.class);
        intent.putExtra(SpotifyMusicPlayer.EXTRA_SPOTIFY_MUSIC_PLAYER_URI,uri);
        startActivity(intent);
    }
    private void getElementChildren(ListItem item){
        contenu.getChildrenOfItem(item,NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                ArrayList<SpotifyNavigationItem> navigationItems = new ArrayList<>();
                for (ListItem item : listItems.items){
                    navigationItems.add(new SpotifyNavigationItem(item));
                }
                navigationList = new SpotifyNavigationList(navigationItems);
                displayListItems(navigationItems);
            }
        });
    }
    private void addElementChildren(ListItem item){
        contenu.getChildrenOfItem(item,NUMBER_OF_ELEMENTS_TO_LOAD,startIndexOfDataFetch).setResultCallback(new CallResult.ResultCallback<ListItems>() {
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
        navigationList.transformToXML(listView,this,binder);
        if(!navigationList.navigationItems.get(0).isPlayable())
            navigationList.setListOnClickListener(navigationListener,listView);
        else
            navigationList.setListOnClickListener(playableListener,listView);
        navigationList.setListOnScrollListener(getLoadElementsAtEndOfListScrollListener(),listView);
    }
    private void reloadActivity(Boolean extraLastSelected){
        Intent intent = new Intent(this,SpotifyMusicListActivity.class);
        intent.putExtra(EXTRA_LIST_ITEM_SELECTED,extraLastSelected);
        startActivity(intent);
    }
    private void loadMoreContent(ListItem item){
        if(!noMoreDataToFetch){
            startIndexOfDataFetch += NUMBER_OF_ELEMENTS_TO_LOAD;
            progressBar.setVisibility(View.VISIBLE);
            addElementChildren(item);
        }
    }
    public AbsListView.OnScrollListener getLoadElementsAtEndOfListScrollListener(){
        return new AbsListView.OnScrollListener() {
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
                    loadMoreContent(lastSelected);
                }
            }
        };
    }
}
class SpotifyNavigationItem{
    ListItem baseNavigationItem;
    public SpotifyNavigationItem(ListItem baseItemWithChildren){
        if(!(baseItemWithChildren == null))
            baseNavigationItem = baseItemWithChildren;
        else
            baseNavigationItem = new ListItem("", "", null , "", "", false, false);
    }
    public String getURI(){
        return baseNavigationItem.uri;
    }
    public String getID(){
        return baseNavigationItem.id;
    }
    public boolean isPlayable(){
        return baseNavigationItem.playable;
    }
    public boolean hasChildren(){
        return baseNavigationItem.hasChildren;
    }
    public ImageUri getImageURI(){
        return baseNavigationItem.imageUri;
    }
    public ListItem getBaseListItem(){
        return baseNavigationItem;
    }
    public String getTitle(){return baseNavigationItem.title;}
    @Override
    public String toString(){
        return baseNavigationItem.title;
    }
}
