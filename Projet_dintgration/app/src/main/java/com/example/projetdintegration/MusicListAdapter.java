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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetdintegration.DBHelpers.Classes.Music;
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
        Intent intent = new Intent(this.getContext(), MediaActivity.class);

        Playlists playlistsWriter = new Playlists(new DBHelper(mContext).getWritableDatabase(), mContext);
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

        holder.item.setOnClickListener(view -> {
            Log.d(TAG, "onItemClick: Started");
            binder.getService().updateMusicList(musics, position);
            this.mContext.startActivity(intent);
        });
        holder.item.setOnLongClickListener(view -> {
            popupHelper.showMusicOptions(view, music, position, binder);
            return true;
        });
        if (music.isFavorite()){
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
        }
        else{
            holder.favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }

        Log.d(TAG, "getView: musicId = " + music.getId());
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
            popupHelper.showMusicOptions(view, music, position, binder);
        });

        return convertView;
    }

    public void refresh(int playlistId){
        DBHelper dbHelper = new DBHelper(mContext);
        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase(), mContext);
        musics = DBMusicsReader.LastSelect();

        this.clear();
        this.addAll(musics);
        this.notifyDataSetChanged();
    }
}
