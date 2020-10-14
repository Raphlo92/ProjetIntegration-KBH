package com.example.projet_dintgration.DBHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.projet_dintgration.DBHelpers.Classes.Album;
import com.example.projet_dintgration.DBHelpers.Classes.Artist;
import com.example.projet_dintgration.DBHelpers.Classes.Music;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

public class DBInitializer {
    private static final String TAG = "DBInitializer";
    private static final int ARTIST_NAME_COUNT = 5;
    private static final int ALBUM_NAME_COUNT = 6;
    private static final int MUSIC_NAME_COUNT = 7;
    DBHelper dbHelper;
    SQLiteDatabase DBWriter;
    SQLiteDatabase DBReader;
    public DBInitializer(Context context) {
        dbHelper = new DBHelper(context);
        DBWriter = dbHelper.getWritableDatabase();
        DBReader = dbHelper.getReadableDatabase();
    }

    public void Init(ArrayList<File> files){
        Log.d(TAG, "Init: Started");

        for (File file : files) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path path = Paths.get(file.toURI());
                switch (path.getNameCount()){
                    case ARTIST_NAME_COUNT:
                        Log.d(TAG, "Init: artistName = " + path.getFileName());
                        Artist artist = new Artist(0, path.getFileName().toString());
                        break;
                    case ALBUM_NAME_COUNT:
                        Log.d(TAG, "Init: albumName = " + path.getFileName());

                        Album album = new Album(0, path.getFileName().toString(), path.getName(ARTIST_NAME_COUNT).toString(), "");
                        album.setCategory(getCategory(album));

                        break;
                    case MUSIC_NAME_COUNT:
                        Log.d(TAG, "Init: musicName = " + path.getFileName());
                        Music music = new Music(0, path.getFileName().toString(), new MediaPlayer().getDuration(), "", "", "", "", "", false);
                        album.setCategory(getCategory(Music));
                        break;
                }
            }


        }
    }

    public String getCategory(Album album){
        String category = "";
        try {
            StringBuilder StringifiedJSON = new StringBuilder();
            URL url = new URL("https://www.theaudiodb.com/api/v1/json/1/searchalbum.php?s="+ album.getArtist() + "&a="+ album.getName());
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == 200){
                Scanner sc = new Scanner(url.openStream());
                while(sc.hasNext()){
                    StringifiedJSON.append(sc.nextLine());
                }
                sc.close();
            }

            JSONObject jsonObject = (JSONObject) new JSONTokener(StringifiedJSON.toString()).nextValue();
            category = jsonObject.getString("strGenre");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return category;
    }

    public String getCategory(Music music){
        String category = "";

        int categoryId = Albums.getCategoryByName(DBReader, music.getAlbum());

        category = Categories.getNameById(DBReader, categoryId);

        return category;
    }

    public int getLength(URI uri){
        Media
    }
}
