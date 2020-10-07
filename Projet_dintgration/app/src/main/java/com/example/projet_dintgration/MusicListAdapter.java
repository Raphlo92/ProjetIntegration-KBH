package com.example.projet_dintgration;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projet_dintgration.DBHelpers.Classes.Music;

import java.util.ArrayList;

public class MusicListAdapter extends ArrayAdapter<Music> {
    private static final String TAG = "MusicListAdapter";
    private Context mContext;
    private int mRessource;
    private int lastPosition = -1;

    static class ViewHolder {
        TextView title;
        TextView artist;
        TextView length;
    }

    public MusicListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> objects) {
        super(context, resource, objects);
        mContext = context;
        mRessource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        int id = getItem(position).getId();
        String title = getItem(position).getTitle();
        int length = getItem(position).getLength(); // in seconds
        String type = getItem(position).getType();
        String path = getItem(position).getPath();
        String category = getItem(position).getCategory();
        String artist = getItem(position).getArtist();
        String album = getItem(position).getAlbum();
        boolean favorite = getItem(position).isFavorite();

        Music music = new Music(id, title, length, type, path, category, artist, album, favorite);

        final View result;
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mRessource, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.artist = (TextView) convertView.findViewById(R.id.artist);
            holder.length = (TextView) convertView.findViewById(R.id.length);
            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }



        /*Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition)? R.anim.load_down_anim : R.anim.load_up_anim);

        result.startAnimation(animation);*/

        lastPosition = position;

        holder.title.setText(music.getTitle());
        holder.artist.setText(music.getArtist());
        holder.length.setText(Music.TimeToString(music.getLength()));

        return convertView;
    }
}
