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

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.android.appremote.internal.ImagesApiImpl;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SpotifyMusicListActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    ContentApi contenu;
    ListView listView;
    AdapterView.OnItemClickListener navigationListener;
    AdapterView.OnItemClickListener playableListener;
    SpotifyNavigationList navigationList;
    RelativeLayout progressBar;
    Boolean noMoreDataToFetch;
    int startIndexOfDataFetch;
    static final String SPOTIFY_ARTIST_LINK = "artist";
    static final String SPOTIFY_ALBUM_LINK = "album";
    static final String SPOTIFY_PLAYLIST_LINK = "playlist";
    boolean userScrolled;
    public static final int NUMBER_OF_ELEMENTS_TO_LOAD = 15;
    public static final String EXTRA_LIST_ITEM_SELECTED = "EXTRA_LIST_ITEM_SELECTED";
    static public ListItem lastSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        determineContentViewToSet();
        progressBar = findViewById(R.id.loadItemsListView);
        initializeNavigationElements();
        initializeOnClickListItemListeners();
        listView = (ListView) findViewById(R.id.list_spotify_bibliotheque_start);
        initializeListViewContent();
    }
    private void determineContentViewToSet(){
        if(lastSelected != null) {
            if (lastSelected.id.contains(SPOTIFY_ALBUM_LINK))
                manageAlbumView();
            else
                setContentView(R.layout.spotify_bibliotheque_start);
            TextView categorieName = findViewById(R.id.textView_categorie_name);
            categorieName.setText(lastSelected.title);
        }else
            setContentView(R.layout.spotify_bibliotheque_start);
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
        if(getIntent().getBooleanExtra(EXTRA_LIST_ITEM_SELECTED,false)) {
            getElementChildren(lastSelected);
        }else{
            contenu.getRecommendedContentItems(LierSpotifyActivity.CONTENT_API_RECOMMENDED_CALL).setResultCallback(new CallResult.ResultCallback<ListItems>() {
                @Override
                public void onResult(ListItems listItems) {
                    for (ListItem item : listItems.items
                    ) {
                        if (item.id.equals("com.spotify.your-library")) {
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
                SpotifyNavigationItem selectedItem = (SpotifyNavigationItem) parent.getItemAtPosition(position);
                Log.i("SpotifyMusicList",selectedItem.hasChildren() + " " + selectedItem.isPlayable());
                lastSelected = selectedItem.getBaseListItem();
                reloadActivity(true);
            }
        };
        playableListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyNavigationItem selectedItem = (SpotifyNavigationItem) parent.getItemAtPosition(position);
                Log.i("SpotifyMusicList",selectedItem.baseNavigationItem.toString());
                Log.i("SpotifyMusicList", Boolean.toString(determineIfHasChildren(selectedItem)));
                if(determineIfHasChildren(selectedItem))
                    sendPlayableToMusicPlayer(selectedItem.getURI());
                else{
                    lastSelected = selectedItem.getBaseListItem();
                    reloadActivity(true);
                }
            }
        };
    }

    private boolean determineIfHasChildren(SpotifyNavigationItem selectedItem){
        return !selectedItem.hasChildren() && (!selectedItem.getID().contains(SPOTIFY_ALBUM_LINK) || selectedItem.getID().equals(lastSelected.id)) &&
                !selectedItem.getID().contains(SPOTIFY_ARTIST_LINK) && !selectedItem.getID().contains(SPOTIFY_PLAYLIST_LINK);
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
        navigationList.transformToXML(listView,this);
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
        baseNavigationItem = baseItemWithChildren;
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
    @Override
    public String toString(){
        return baseNavigationItem.title;
    }
}
class SpotifyNavigationList{
    ArrayAdapter<SpotifyNavigationItem> arrayAdapter;
    ArrayList<SpotifyNavigationItem> navigationItems;
    public SpotifyNavigationList(ArrayList<SpotifyNavigationItem> items){
        navigationItems = items;
    }
    public void addItem(SpotifyNavigationItem item){
        navigationItems.add(item);
    }
    public void transformToXML(ListView listView,Context context){
        arrayAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,navigationItems);
        listView.setAdapter(arrayAdapter);
    }
    public void updateContentInList(ArrayList<SpotifyNavigationItem> items, RelativeLayout progressBar){
      //  new Handler().postDelayed(new Runnable() {
          //  @Override
         //   public void run() {
                navigationItems.addAll(items);
                arrayAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
         //   }
        //},5000);
    }
    public void setListOnClickListener(AdapterView.OnItemClickListener clickListener, ListView listView){
        listView.setOnItemClickListener(clickListener);
    }
    public void setListOnScrollListener(AbsListView.OnScrollListener scrollListener, ListView listView){
        listView.setOnScrollListener(scrollListener);
    }
    // TODO Permettre le visionnement du contenu jouable mais ayant des enfants
    // TODO actualiser l'affichage pour prendre le même que la bibliothèque locale.
}