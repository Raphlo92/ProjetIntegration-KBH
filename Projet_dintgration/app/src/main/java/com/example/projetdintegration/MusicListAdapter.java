package com.example.projetdintegration;

import android.content.Context;
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

import java.util.ArrayList;

public class MusicListAdapter extends ArrayAdapter<Music> {
    private static final String TAG = "MusicListAdapter";
    private Context mContext;
    private int mRessource;
    private int lastPosition = -1;
    private PopupHelper popupHelper;
    private int playlistId;


    static class ViewHolder {
        LinearLayout item;
        ImageView favorite;
        TextView title;
        TextView artist;
        TextView length;
        ImageView options;
    }

    public MusicListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> objects, int playlistId) {
        super(context, resource, objects);
        mContext = context;
        mRessource = resource;
        popupHelper = new PopupHelper(context);
        this.playlistId = playlistId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        int id = getItem(position).getId();
        String title = getItem(position).getName();
        double length = getItem(position).getLength(); // in seconds
        String type = getItem(position).getType();
        String path = getItem(position).getPath();
        String category = getItem(position).getCategory();
        String artist = getItem(position).getArtist();
        String album = getItem(position).getAlbum();
        boolean favorite = getItem(position).isFavorite();

        Playlists playlistsWriter = new Playlists(new DBHelper(mContext).getWritableDatabase());
        Music music = new Music(id, title, length, type, path, category, artist, album, favorite);

        final View result;
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
            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }



        /*Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition)? R.anim.load_down_anim : R.anim.load_up_anim);

        result.startAnimation(animation);*/

        lastPosition = position;

        holder.title.setText(music.getName());
        holder.artist.setText(music.getArtist());
        holder.length.setText(Music.TimeToString(music.getLength()));

        holder.item.setOnLongClickListener(view -> {
            popupHelper.showMusicOptions(view, music);
            return true;
        });
        if (music.isFavorite()){
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
        }
        else{
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }

        holder.favorite.setOnClickListener(view -> {
            if (music.isFavorite()){
                playlistsWriter.RemoveFromFavorites(music.getId());
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            }
            else{
                playlistsWriter.AddToFavorites(music.getId());
                holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);

            }
            this.refresh(playlistId);
        });

        holder.options.setOnClickListener(view -> {
            popupHelper.showMusicOptions(view, music); 
        });

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
