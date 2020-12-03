package com.example.projetdintegration;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.Classes.SpotifyMusic;
import com.spotify.android.appremote.api.UserApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.LibraryState;

import java.util.ArrayList;

public class SpotifyNavigationList{
    public static String STRING_ARTISTE_ARRAY = "Artiste";
    public static String STRING_ALBUM_ARRAY = "Album";
    public static String STRING_PLAYLIST_ARRAY = "Liste de lecture";
    public static String STRING_PODCAST_ARRAY = "Podcast";
    public static String STRING_COLLECTION_ARRAY = "Collection";
    public static String STRING_AUTRE_ARRAY = "Autre";
    public static String STRING_SONG_ARRAY = "Chanson";

    //ArrayAdapter<SpotifyNavigationItem> arrayAdapter;
    MusicListAdapter adapter;
    public ArrayList<Music> musics;
    ArrayList<SpotifyNavigationItem> navigationItems;
    public SpotifyNavigationList(ArrayList<SpotifyNavigationItem> items){
        navigationItems = items;
        musics = new ArrayList<Music>();
    }
    public void addItem(SpotifyNavigationItem item){
        navigationItems.add(item);
    }
    public void transformToXML(ListView listView, Context context, MediaPlaybackService.LocalBinder binder){
        //arrayAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,navigationItems);
        adapter = new MusicListAdapter(context,R.layout.music_listitem_layout, musics,-1,binder);
        musics.addAll(transformToMusicsListArray(navigationItems,adapter));
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    public void updateContentInList(ArrayList<SpotifyNavigationItem> items, RelativeLayout progressBar){
        //  new Handler().postDelayed(new Runnable() {
        //  @Override
        //   public void run() {
        navigationItems.addAll(items);
        musics.addAll(transformToMusicsListArray(items,adapter));
        adapter.notifyDataSetChanged();
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
    static public ArrayList<Music> transformToMusicsListArray(ArrayList<SpotifyNavigationItem> items, MusicListAdapter adapter){
        ArrayList<Music> musics = new ArrayList<>();
        for (SpotifyNavigationItem item : items) {
            musics.add(new SpotifyMusic(item.getTitle(),item.getURI(), false,item.isPlayable(),item.hasChildren(),item.getID(),item.getImageURI(),item.baseNavigationItem.subtitle,determineItemType(item.getID())));
        }
        UserApi userApi = LierSpotifyActivity.appRemote.getUserApi();
        for(Music music : musics){
            userApi.getLibraryState(music.getPath()).setResultCallback(new CallResult.ResultCallback<LibraryState>() {
                @Override
                public void onResult(LibraryState libraryState) {
                    if(libraryState.canAdd)
                        ((SpotifyMusic)music).setFavorite(libraryState.isAdded);
                    adapter.notifyDataSetChanged();
                }
            });
        }
        return musics;
    }


    private static String determineItemType(String id) {
        Log.i("DetermineTypeTest",id);
        if(id.contains(SpotifyMusicListActivity.SPOTIFY_ALBUM_LINK) && !id.equals(SpotifyMusicListActivity.SPOTIFY_ID_ALBUMS))
            return STRING_ALBUM_ARRAY;
        else if(id.contains(SpotifyMusicListActivity.SPOTIFY_TRACK_LINK))
            return STRING_SONG_ARRAY;
        else if(id.contains(SpotifyMusicListActivity.SPOTIFY_PLAYLIST_LINK)&& !id.equals(SpotifyMusicListActivity.SPOTIFY_ID_PLAYLIST))
            return STRING_PLAYLIST_ARRAY;
        else if(id.contains(SpotifyMusicListActivity.SPOTIFY_ID_PODCASTS)&& !id.equals(SpotifyMusicListActivity.SPOTIFY_ID_PODCASTS))
            return STRING_PODCAST_ARRAY;
        else if(id.contains(SpotifyMusicListActivity.SPOTIFY_ARTIST_LINK)&& !id.equals(SpotifyMusicListActivity.SPOTIFY_ID_ARTISTS))
            return STRING_ARTISTE_ARRAY;
        else if(id.contains(SpotifyMusicListActivity.SPOTIFY_COLLECTION_LINK))
            return STRING_COLLECTION_ARRAY;
        else
            return STRING_AUTRE_ARRAY;
    }
    // TODO Actualiser affichage pour l'artiste
    // TODO actualiser l'affichage pour prendre le même que la bibliothèque locale.
}
