package com.example.projetdintegration;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.Classes.*;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.Utilities.PopupHelper;
import com.example.projetdintegration.Utilities.SpotifyLibraryManager;
import com.spotify.android.appremote.api.UserApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.UserStatus;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MusicListAdapter extends ArrayAdapter<Music> {
    private static final String TAG = "MusicListAdapter";
    private Context mContext;
    private int mRessource;
    private int lastPosition = -1;
    private PopupHelper popupHelper;
    private int playlistId;
    Playlists playlistsWriter;
    ArrayList<Music> musics;
    MediaPlaybackService.LocalBinder binder;


    static class ViewHolder {
        LinearLayout item;
        ImageView favorite;
        TextView title;
        TextView artist;
        TextView length;
        ImageView options;
    }

    public MusicListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> musics, int playlistId, MediaPlaybackService.LocalBinder binder) {
        super(context, resource, musics);
        mContext = context;
        mRessource = resource;
        popupHelper = new PopupHelper(context);
        this.playlistId = playlistId;
        this.musics = musics;
        this.binder = binder;
        playlistsWriter = new Playlists(new DBHelper(mContext).getWritableDatabase());
    }

    public void initHolderValues(ViewHolder holder, Music music){
        if(music.getType().equals("Spotify")){
            initHolderValuesSpotify(holder,(SpotifyMusic) music);
        }else{
            initHolderValuesLocal(holder,music);
        }
    }

    private void initHolderValuesLocal(ViewHolder holder, Music music) {
        holder.title.setText(music.getName());
        holder.artist.setText(music.getArtist());
        holder.length.setText(Music.TimeToString(music.getLength()));
        holder.item.setOnClickListener(view -> {
            Log.d(TAG, "onItemClick: Started");
            binder.getService().updateMusicList(musics, lastPosition);
        });
        holder.item.setOnLongClickListener(view -> {
            popupHelper.showMusicOptions(view, music);
            return true;
        });
        if (music.isFavorite()){
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
        }else{
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        holder.favorite.setOnClickListener(view -> {
            if (music.isFavorite()){
                playlistsWriter.RemoveFromFavorites(music.getId());
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else{
                playlistsWriter.AddToFavorites(music.getId());
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);

            }
            this.refresh(playlistId);
        });

        holder.options.setOnClickListener(view -> {
            popupHelper.showMusicOptions(view, music);
        });
    }

    private void initHolderValuesSpotify(ViewHolder holder, SpotifyMusic music) {
        SpotifyLibraryManager libraryManager = new SpotifyLibraryManager(LierSpotifyActivity.appRemote.getUserApi());
        UserApi user = LierSpotifyActivity.appRemote.getUserApi();
        CallResult.ResultCallback<Empty> callbackAdd = new CallResult.ResultCallback<Empty>() {
            @Override
            public void onResult(Empty empty) {
                music.setFavorite(true);
                notifyDataSetChanged();
            }
        };
        CallResult.ResultCallback<Empty> callbackRemove = new CallResult.ResultCallback<Empty>() {
            @Override
            public void onResult(Empty empty) {
                music.setFavorite(false);
                notifyDataSetChanged();
            }
        };
        holder.title.setText(music.getName());
        holder.artist.setText(music.getArtist());
        holder.length.setVisibility(View.GONE);
        if (/*music.getSpotifyId().contains(SpotifyMusicListActivity.SPOTIFY_TRACK_LINK)*/ music.getArtist().equals(SpotifyNavigationList.STRING_SONG_ARRAY)) {
            holder.favorite.setVisibility(View.VISIBLE);
            if (music.isFavorite()) {
                Log.i(TAG, "Valeur du uri -- id " + music.getPath() + " -- " + music.getSpotifyId() + " -- " + music.getName());
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }else
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        } else {
            holder.favorite.setVisibility(View.GONE);
        }
        holder.favorite.setOnClickListener(view -> {
            Log.i(TAG, music.isFavorite() + " " + music.getPath());
            if (music.isFavorite()) {
                libraryManager.removeFromLibrary(music.getPath(), callbackRemove);
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            } else {
                libraryManager.addToLibrary(music.getPath(), callbackAdd);
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            }
            //TODO modifier pour Spotify
            this.notifyDataSetChanged();
        });
        holder.item.setOnLongClickListener(view -> {
            if (music.getPath().contains(SpotifyMusicListActivity.SPOTIFY_ALBUM_LINK))
                popupHelper.showMusicOptions(view, music,callbackRemove,callbackAdd,music.isFavorite());
            else
                popupHelper.showMusicOptions(view, music,callbackRemove,callbackAdd);

            return true;
        });
        holder.options.setOnClickListener(view -> {
            if (music.getPath().contains(SpotifyMusicListActivity.SPOTIFY_ALBUM_LINK))
                popupHelper.showMusicOptions(view, music,callbackRemove,callbackAdd, music.isFavorite());
            else
                popupHelper.showMusicOptions(view, music,callbackRemove,callbackAdd);
        });
    }

    //public void refreshSpotify() {
    //    this.clear();
    //    this.addAll(SpotifyNavigationList.transformToMusicsListArray(SpotifyMusicListActivity.navigationList.navigationItems));
    //    this.notifyDataSetChanged();
    //}

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Music music = (Music) getItem(position);
        //final View result;
        ViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mRessource, parent, false);
            holder = new ViewHolder();
            holder.item = (LinearLayout) convertView.findViewById(R.id.musicItem) ;
            holder.favorite = (ImageView) convertView.findViewById(R.id.favitesButton);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.artist = (TextView) convertView.findViewById(R.id.artist);
            holder.length = (TextView) convertView.findViewById(R.id.length);
            holder.options = (ImageView) convertView.findViewById(R.id.moreOptions);
            //result = convertView;
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            //result = convertView;
        }
        /*Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition)? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);*/
        lastPosition = position;
        initHolderValues(holder,music);

        return convertView;
    }

    public void refresh(int playlistId){
        DBHelper dbHelper = new DBHelper(mContext);
        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        ArrayList<IDBClass> dbMusics = new ArrayList<>();
        ArrayList<Music> musics = new ArrayList<>();
        if(playlistId > -1){
            Playlists DBPlaylistsReader = new Playlists(dbHelper.getReadableDatabase());
            dbMusics = DBPlaylistsReader.getAllMusicsInPlaylist(playlistId);
        }
        else{
            dbMusics = DBMusicsReader.Select(null, null, null, null, null, null);
        }

        for (IDBClass music : dbMusics) {
            musics.add((Music) music);
        }
        this.clear();
        this.addAll(musics);
        this.notifyDataSetChanged();
    }
}
