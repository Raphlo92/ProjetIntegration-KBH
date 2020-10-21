package com.example.projetdintegration;

import android.app.Person;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<Music> {
    private static final String Tag = " FileListAdapter";
    private int lastPosition = -1;
    private Context mcontext;
    int mResource;

    static class ViewHolder {
        TextView title;
        ImageButton album;
    }

    public FileListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> objects) {
        super(context, resource, objects);
        this.mcontext = mcontext;
        mResource = resource;
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

        Music music = new Music(id, title, length, type, path, category, artist, album, favorite);
        ViewHolder holder = new ViewHolder();
        final View result;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mcontext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.TextView);
            holder.album = (ImageButton) convertView.findViewById(R.id.mediaActivityButton1);
            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
    }


        lastPosition = position;
        holder.title.setText(music.getCategory());
        holder.album.setImageDrawable(Drawable.createFromPath("@drawable/ic_file"));


        return convertView;
    }
}
