package com.example.projetdintegration;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.util.ArrayList;

public class CategorieListAdapter extends ArrayAdapter<Category> {

    private static final String Tag = " FileListAdapter";
    private int lastPosition = -1;
    private Context mContext;
    int mResource;

    static class ViewHolder {
        TextView title;
        HorizontalScrollView album;
    }

    public CategorieListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Category> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        int id = getItem(position).getId();
        String title = getItem(position).getName();


        Category category = new Category(id, title);
        FileListAdapter.ViewHolder holder = new FileListAdapter.ViewHolder();
        final View result;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new FileListAdapter.ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.TextView);

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (FileListAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }


        lastPosition = position;
        holder.title.setText(category.getName());

        return convertView;
    }
}
