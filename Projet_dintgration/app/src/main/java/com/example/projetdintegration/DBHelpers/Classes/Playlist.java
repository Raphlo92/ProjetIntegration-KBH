package com.example.projetdintegration.DBHelpers.Classes;

import android.util.Log;

import androidx.annotation.Nullable;

public class Playlist implements IDBClass {
    private static final String TAG = "Playlist";
    private int id;
    private String name;
    private String type;

    public Playlist(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null){
            return false;
        }
        Log.d(TAG, "equals: obj.class = " + obj.getClass().getName());
        if (obj.getClass().getName().equals(this.getClass().getName())){
            Playlist comparedPlaylist = (Playlist)obj;
            return comparedPlaylist.getName().equals(getName());
        }
        return false;
    }
}
