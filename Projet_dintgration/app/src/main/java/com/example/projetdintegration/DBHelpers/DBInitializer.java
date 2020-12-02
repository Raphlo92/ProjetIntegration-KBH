package com.example.projetdintegration.DBHelpers;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Classes.Album;
import com.example.projetdintegration.DBHelpers.Classes.Artist;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.Utilities.MusicFileExplorer;
import com.example.projetdintegration.Utilities.NumberUtil;
import com.example.projetdintegration.Utilities.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import static com.example.projetdintegration.DBHelpers.Musics.getCategoryNameById;


public class DBInitializer {
    private static final String TAG = "DBInitializer";
    //private static final int ARTIST_NAME_COUNT = 5;
    //private static final int ALBUM_NAME_COUNT = 6;
    //private Date lastModified;
    Context mContext;
    DBHelper dbHelper;
    SQLiteDatabase DBWriter;
    SQLiteDatabase DBReader;
    public DBInitializer(Context context) {
        mContext = context;
        dbHelper = new DBHelper(context);
        DBWriter = dbHelper.getWritableDatabase();
        DBReader = dbHelper.getReadableDatabase();
    }

    public static class DBInitialisingService extends IntentService{

        //Date lastInit;
        public DBInitialisingService() {
            super("DBInitialisingService");
            //this.lastInit = Date.from(Instant.now());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Log.d(TAG, "onHandleIntent: Started");
            new DBInitializer(this).Init(getAllMusicsInMediaStore(this));
            //lastInit = Date.from(Instant.now());
        }
    }

    public void Init(ArrayList<Music> musics){
        //lastModified = Date.from(Instant.now());
        Log.d(TAG, "Init: Started");
        new Musics(DBWriter, mContext).Insert(musics);
    }

    public static ArrayList<Music> getAllMusicsInMediaStore(Context context){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<Music> musics = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context);
        Playlists playlistsDBHelper = new Playlists(dbHelper.getReadableDatabase(), context);

        Cursor cursorA = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC");

        Cursor cursorV = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
        if(cursorA != null){
            while(cursorA.moveToNext()){
                if (cursorA.getInt(cursorA.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) != 0) {
                    String path = cursorA.getString(cursorA.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    mmr.setDataSource(path);
                    int id = cursorA.getInt(cursorA.getColumnIndex(MediaStore.Audio.Media._ID));
                    int isMusic = cursorA.getInt(cursorA.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    String title = cursorA.getString(cursorA.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

                    Log.d(TAG, "getAllMusicsInMediaStore: "+ title + ": IsMusic = " + isMusic);

                    String type = cursorA.getString(cursorA.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    String artist =  cursorA.getString(cursorA.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album =  cursorA.getString(cursorA.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    String duration =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                    if (genre != null){
                        genre = StringUtil.Strip(genre.trim());
                        genre = StringUtil.ReplaceAbbreviations(genre);

                        if (NumberUtil.tryParseInt(genre)){
                            genre = getCategoryNameById(context, Integer.parseInt(genre));
                        }
                    }
                    else{
                        genre = "";
                    }

                    if (duration != null){
                        duration = duration.trim();
                    }
                    else
                        duration = "0";

                    Log.d(TAG, "MediaStore: Music: " +
                            "\nid = " + + id +
                            "\ntitle = " + title +
                            "\nduration = " + duration +
                            "\ntype = " + type +
                            "\npath = " + path +
                            "\nartist = " + artist +
                            "\nalbum = " + album +
                            "\ngenre = " + genre);
                    musics.add(new Music(id, title, Double.parseDouble(duration) / 1000, type, path, genre, artist, album, playlistsDBHelper.isInFavorites(id)));
                }
            }
            cursorA.close();
        }

        if(cursorV != null)
        {
            while(cursorV.moveToNext()){
                String path = cursorV.getString(cursorV.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                mmr.setDataSource(path);
                int id = cursorV.getInt(cursorV.getColumnIndex(MediaStore.Video.Media._ID));
                Log.d(TAG, "getAllMusicsInMediaStore: id = " + id);
                String title = cursorV.getString(cursorV.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String type = cursorV.getString(cursorV.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                String artist =  cursorV.getString(cursorV.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                String album =  cursorV.getString(cursorV.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                String genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                String duration =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                if (genre != null){
                    genre = StringUtil.Strip(genre.trim());
                    genre = StringUtil.ReplaceAbbreviations(genre);

                    if (NumberUtil.tryParseInt(genre)){
                        genre = getCategoryNameById(context, Integer.parseInt(genre));
                    }
                }
                else{
                    genre = "";
                }

                if (duration != null){
                    duration = duration.trim();
                }
                else
                    duration = "0";

                Log.d(TAG, "MediaStore: Music: " +
                        "\nid = " + + id +
                        "\ntitle = " + title +
                        "\nduration = " + duration +
                        "\ntype = " + type +
                        "\npath = " + path +
                        "\nartist = " + artist +
                        "\nalbum = " + album +
                        "\ngenre = " + genre);
                musics.add(new Music(id, title, Double.parseDouble(duration) / 1000, type, path, genre, artist, album, playlistsDBHelper.isInFavorites(id)));
            }
            cursorV.close();
        }
        return musics;
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
}
