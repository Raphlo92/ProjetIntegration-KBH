package com.example.projetdintegration;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;

import java.util.ArrayList;
import java.util.Random;

public class CategorieListAdapter extends RecyclerView.Adapter<CategorieListAdapter.ViewHolder> {

    private static final String Tag = " FileListAdapter";
    private int lastPosition = -1;
    private Context mContext;
    int mResource;
    ArrayList<Category> categories;
    MediaPlaybackService.LocalBinder binder;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mainactivity_adapter_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int id = categories.get(position).getId();
        String title = categories.get(position).getName();

        Category category = new Category(id, title);

        lastPosition = position;
        holder.title.setText(category.getName());

        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase(),  mContext);
        ArrayList<Music> musics = DBMusicsReader.getAllMusicInCategory(category.getId());

        FileListAdapter adapter = new FileListAdapter(mContext, R.layout.mainactivity_imagebutton_adapter, musics, binder);
        holder.musics.setAdapter(adapter);


        LinearLayoutManager layout = new LinearLayoutManager(mContext);
        layout.setOrientation(RecyclerView.HORIZONTAL);
        holder.musics.setLayoutManager(layout);

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RecyclerView musics;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.TextView);
            musics = itemView.findViewById(R.id.slideshow);
        }
    }

    public CategorieListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Category> categories, MediaPlaybackService.LocalBinder binder) {
        super();
        mContext = context;
        mResource = resource;
        this.categories = categories;
        this.binder = binder;
    }
}
