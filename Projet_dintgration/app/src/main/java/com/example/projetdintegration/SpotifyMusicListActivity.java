package com.example.projetdintegration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;

public class SpotifyMusicListActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Menu menu;
    ContentApi contenu;
    ListView listView;
    AdapterView.OnItemClickListener navigationListener;
    AdapterView.OnItemClickListener playableListener;
    public static final String EXTRA_LIST_ITEM_SELECTED = "EXTRA_LIST_ITEM_SELECTED";
    static public ListItem lastSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotify_bibliotheque_start);
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
        initializeOnClickListItemListeners();

        listView = (ListView) findViewById(R.id.list_spotify_bibliotheque_start);
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
                Log.i("SpotifyMusicList",selectedItem.hasChildren() + " " + selectedItem.isPlayable());
                if(!selectedItem.hasChildren())
                    sendPlayableToMusicPlayer(selectedItem.getURI());
                else{
                    lastSelected = selectedItem.getBaseListItem();
                    reloadActivity(true);
                }
            }
        };
    }

    private void sendPlayableToMusicPlayer(String uri) {
        Intent intent = new Intent(this,SpotifyMusicPlayer.class);
        intent.putExtra(SpotifyMusicPlayer.EXTRA_SPOTIFY_MUSIC_PLAYER_URI,uri);
        startActivity(intent);
    }

    private void getElementChildren(ListItem item){
        contenu.getChildrenOfItem(item,10,0).setResultCallback(new CallResult.ResultCallback<ListItems>() {
            @Override
            public void onResult(ListItems listItems) {
                ArrayList<SpotifyNavigationItem> navigationItems = new ArrayList<>();
                for (ListItem item : listItems.items){
                    navigationItems.add(new SpotifyNavigationItem(item));
                }
                displayListItems(navigationItems);
            }
        });
    }
    private void displayListItems(ArrayList<SpotifyNavigationItem> navigationItems){
        SpotifyNavigationList navigationList = new SpotifyNavigationList(navigationItems);
        navigationList.transformToXML(listView,this);
        if(!navigationList.navigationItems.get(0).isPlayable())
            navigationList.setListOnClickListener(navigationListener,listView);
        else
            navigationList.setListOnClickListener(playableListener,listView);
    }
    private void reloadActivity(Boolean extraLastSelected){
        Intent intent = new Intent(this,SpotifyMusicListActivity.class);
        intent.putExtra(EXTRA_LIST_ITEM_SELECTED,extraLastSelected);
        startActivity(intent);
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
    ArrayList<SpotifyNavigationItem> navigationItems;
    public SpotifyNavigationList(ArrayList<SpotifyNavigationItem> items){
        navigationItems = items;
    }

    public void transformToXML(ListView listView,Context context){
        ArrayAdapter<SpotifyNavigationItem> arrayAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,navigationItems);
        listView.setAdapter(arrayAdapter);
    }
    public void setListOnClickListener(AdapterView.OnItemClickListener clickListener, ListView listView){
        listView.setOnItemClickListener(clickListener);
    }
    //TODO Faire un principe de loading progressif avec le scroll down pour afficher plus de 10 résultats.
    // TODO Permettre le visionnement du contenu jouable mais ayant des enfants
    // TODO actualiser l'affichage pour prendre le même que la bibliothèque locale.
}