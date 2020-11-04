package com.example.projetdintegration;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Categories;
import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;

import java.util.ArrayList;
import java.util.Random;

public class CategorieListAdapter extends ArrayAdapter<Category> {

    private static final String Tag = " FileListAdapter";
    private int lastPosition = -1;
    private Context mContext;
    int mResource;

    static class ViewHolder {
        TextView title;
        ListView musics;
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

        View result;

        Category category = new Category(id, title);
        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.TextView);
            holder.musics = (ListView)  convertView.findViewById(R.id.slideshow);

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        lastPosition = position;
        holder.title.setText(category.getName());

        String whereClause = DBHelper.Contract.TableMusic.COLUMN_NAME_ID_CATEGORY + " = ?";
        String[] whereArgs = {id + ""};

        DBHelper dbHelper = new DBHelper(mContext);
        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase());
        ArrayList<IDBClass> dbMusics = DBMusicsReader.Select(null, whereClause, whereArgs, null, null, null);
        ArrayList<Music> musics = new ArrayList<>();

        Random rand = new Random();
        for (IDBClass music : dbMusics) {
            music = dbMusics.get(rand.nextInt(dbMusics.size()));
            musics.add((Music) music);
            dbMusics.remove(music);
        }

        FileListAdapter adapter = new FileListAdapter(mContext, R.layout.mainactivity_imagebutton_adapter, musics);
        //holder.musics.setAdapter(adapter);

        return convertView;
    }
}
