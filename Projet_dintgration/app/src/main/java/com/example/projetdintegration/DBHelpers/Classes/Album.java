package com.example.projetdintegration.DBHelpers.Classes;

import android.graphics.drawable.Drawable;

public class Album implements IDBClass {
    private int id;
    private String name;
    private String imagePath;
    private Drawable image;
    private String artist;
    private String category;

    public Album(int id, String name, String imagePath, String artist, String category) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
        image = Drawable.createFromPath(imagePath);
        this.artist = artist;
        this.category = category;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        image = Drawable.createFromPath(imagePath);
        this.imagePath = imagePath;
    }

    public Drawable getImage() {
        return image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
