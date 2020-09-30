package com.example.projet_dintgration.DBHelpers.Classes;

public class Music {
    private int id;
    private String title;
    private int length; // in seconds
    private String type;
    private String path;
    private String category;
    private String artist;
    private String album;
    private boolean favorite;

    public Music(int id, String title, int length, String type, String path, String category, String artist, String album, boolean favorite) {
        this.id = id;
        this.title = title;
        this.length = length;
        this.type = type;
        this.path = path;
        this.category = category;
        this.artist = artist;
        this.album = album;
        this.favorite = favorite;
    }

    public static String TimeToString(int length){
        int hours;
        int minutes;
        int seconds;

        hours = (int)Math.floor(length / 3600);
        minutes = (int)Math.floor((length % 3600)/60);
        seconds = length % 60;

        String result;

        result = (hours == 0)? "" : hours + "h";
        result += (minutes == 0)? "" : minutes + "m";
        result += (seconds == 0)? "" : seconds + "s";

        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}

