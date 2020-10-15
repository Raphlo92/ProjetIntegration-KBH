package com.example.projet_dintgration;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projet_dintgration.DBHelpers.Classes.*;
import com.example.projet_dintgration.DBHelpers.DBHelper;

import java.util.ArrayList;

public class PlaylistListAdapter extends ArrayAdapter<Playlist> {
    private static final String TAG = "PlaylistListAdapter";
    private Context mContext;
    private int mRessource;
    private int lastPosition = -1;

    static class ViewHolder {
        LinearLayout item;
        ImageView image;
        TextView name;
    }

    public PlaylistListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Playlist> objects) {
        super(context, resource, objects);
        mContext = context;
        mRessource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        int id = getItem(position).getId();
        String name = getItem(position).getName();
        String type = getItem(position).getType();

        final Playlist playlist = new Playlist(id, name, type);

        final View result;
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mRessource, parent, false);

            holder = new ViewHolder();
            holder.item = (LinearLayout) convertView.findViewById(R.id.playlistItem);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.image = (ImageView) convertView.findViewById(R.id.playlistImage);
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

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to PlaylistActivity
                Intent intent = new Intent(mContext, MusicListActivity.class);
                intent.putExtra(DBHelper.Contract.TablePlaylist._ID, playlist.getId());
                mContext.startActivity(intent);
            }
        });
        holder.name.setText(playlist.getName());

        return convertView;
    }
}
