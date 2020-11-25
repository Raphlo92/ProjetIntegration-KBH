package com.example.projetdintegration;

import android.app.Person;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private static final String Tag = " FileListAdapter";
    private int lastPosition = -1;
    private Context mContext;
    int mResource;
    ArrayList<Music> musics;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton album;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            album = (ImageButton) itemView.findViewById(R.id.mediaActivityButton1);
        }
    }

    public FileListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> musics) {
        super();
        mContext = context;
        mResource = resource;
        this.musics = musics;
    }

    /*public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.album = (ImageButton) convertView.findViewById(R.id.mediaActivityButton1);
            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
    }


        lastPosition = position;
        holder.album.setImageDrawable(Drawable.createFromPath("@drawable/ic_file"));


        return convertView;
    }*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mainactivity_imagebutton_adapter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int id = musics.get(position).getId();
        String title = musics.get(position).getName();
        double length = musics.get(position).getLength(); // in seconds
        String type = musics.get(position).getType();
        String path = musics.get(position).getPath();
        String category = musics.get(position).getCategory();
        String artist = musics.get(position).getArtist();
        String album = musics.get(position).getAlbum();
        boolean favorite = musics.get(position).isFavorite();

        Music music = new Music(id, title, length, type, path, category, artist, album, favorite);

        //if(path == null)
        holder.album.setImageResource(R.drawable.ic_file);
        //else {
        //    holder.album.setImageBitmap(BitmapFactory.decodeFile(path));
        //}
        lastPosition = position;
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }
}
