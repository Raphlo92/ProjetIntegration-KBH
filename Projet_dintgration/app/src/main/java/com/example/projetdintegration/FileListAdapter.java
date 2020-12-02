package com.example.projetdintegration;

import android.app.Person;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.text.Transliterator;
import android.media.MediaMetadataRetriever;
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
    MediaPlaybackService.LocalBinder binder;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView album;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            album = (ImageView) itemView.findViewById(R.id.mainFileImage);
            title = (TextView) itemView.findViewById(R.id.mainFileName);
        }
    }

    public FileListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Music> musics, MediaPlaybackService.LocalBinder binder) {
        super();
        mContext = context;
        mResource = resource;
        this.musics = musics;
        this.binder = binder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mainactivity_imagebutton_adapter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = musics.get(position).getName();
        String path = musics.get(position).getPath();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] artByte = mmr.getEmbeddedPicture();
        if(artByte != null){
            Bitmap bm = BitmapFactory.decodeByteArray(artByte, 0, artByte.length);
            holder.album.setImageBitmap(bm);
        }
        else{
            holder.album.setImageResource(R.drawable.ic_file);
        }

        holder.album.setOnClickListener(view -> {
            binder.getService().PlayNow(musics, position);
        });

        holder.title.setText(title);
    }
    @Override
    public int getItemCount() {
        return musics.size();
    }
}
