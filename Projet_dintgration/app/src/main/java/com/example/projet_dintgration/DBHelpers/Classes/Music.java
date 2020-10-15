package com.example.projet_dintgration.DBHelpers.Classes;

public class Music implements IDBClass{
    private int id;
    private String name;
    private double length; // in seconds
    private String type;
    private String path;
    private String category;
    private String artist;
    private String album;
    private boolean favorite;

    public Music(int id, String name, double length, String type, String path, String category, String artist, String album, boolean favorite) {
        this.id = id;
        this.name = name;
        this.length = length;
        this.type = type;
        this.path = path;
        this.category = category;
        this.artist = artist;
        this.album = album;
        this.favorite = favorite;
    }

    public static String TimeToString(double length){
        if(length == 0)
            return "unknown";

        int hours;
        int minutes;
        int seconds;

        hours = (int)Math.floor(length / 3600);
        minutes = (int)Math.floor((length % 3600)/60);
        seconds = (int)length % 60;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
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

