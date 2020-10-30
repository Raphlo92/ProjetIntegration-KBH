package com.example.projetdintegration.DBHelpers;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.contentcapture.ContentCaptureCondition;

import androidx.annotation.Nullable;

import com.example.projetdintegration.DBHelpers.Classes.Album;
import com.example.projetdintegration.DBHelpers.Classes.Artist;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.MusicFileExplorer;
import com.example.projetdintegration.Utilities.NumberUtil;
import com.example.projetdintegration.Utilities.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;



public class DBInitializer {
    private static final String TAG = "DBInitializer";
    private static final int ARTIST_NAME_COUNT = 5;
    private static final int ALBUM_NAME_COUNT = 6;
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

        public DBInitialisingService() {
            super("DBInitialisingService");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Log.d(TAG, "onHandleIntent: Started");
            ArrayList<File> files = new ArrayList<>();
            MusicFileExplorer.getAllChildren(MusicFileExplorer.DIRECTORY_MUSIC, files);
            new DBInitializer(this).Init(files);
        }
    }

    public void Init(ArrayList<File> files){
        Log.d(TAG, "Init: Started");
        String[] metadata;

        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), DBHelper.DB_VERSION, DBHelper.DB_VERSION + 1);

        Categories categoriesDBHelper = new Categories(DBWriter);
        categoriesDBHelper.init();

        Playlists playlistsDBHelper = new Playlists(DBWriter);
        playlistsDBHelper.initFavorites();

        Log.d(TAG, "Init: FilesSize() = " + files.size());
        for (File file : files) {
            Log.d(TAG, "Init: File reading");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path path = Paths.get(file.toURI());

                Log.d(TAG, "Init: " + path.getFileName() + ": type = " + MusicFileExplorer.getMimeType(file));

                String mimeType = MusicFileExplorer.getMimeType(file);

                if(mimeType != null){
                    mimeType = mimeType.split("/")[0];
                    if(mimeType.equals("video") || mimeType.equals("audio")){
                        metadata = getMetadata(path.toAbsolutePath().toString());
                        Log.d(TAG, "Init: \nmusicName = " + path.getFileName() +
                                "\nmusicArtist = " + metadata[0] +
                                "\nmusicAlbum = " + metadata[1] +
                                "\nmusicGenre = " + metadata[2] +
                                "\nmusicDuration = " + metadata[3]);
                        Music music = new Music(0, path.getFileName().toString(), Double.parseDouble(metadata[3]) / 1000, "audio",  path.toAbsolutePath().toString(), metadata[2], metadata[0], metadata[1], false);
                        Musics musicsDBHelper = new Musics(DBWriter);
                        musicsDBHelper.Insert(music);
                    }
                    else if(mimeType.equals("image")){
                        //TODO update image in Album DB Table

                        //get Album Id by Name => name being the name of folder where the image is (ALBUM_NAME_COUNT = 6)
                        //  PATH TO ALBUM PROPS : /storage/emulated/0/Music/{Artist}/{Album}/*/{PropFile}
                        //                            1        2    3   4       5       6
                        String albumName = path.getName(ALBUM_NAME_COUNT).toString();
                        //update Album using know data (ID)
                        //  update image = "imagePath" where Id = (ID)
                        Albums albumsDBHelper = new Albums(DBWriter);

                        ContentValues values = new ContentValues();
                        values.put(DBHelper.Contract.TableAlbum.COLUMN_NAME_IMAGE, path.toAbsolutePath().toString());
                        String whereClause = DBHelper.Contract.TableAlbum.COLUMN_NAME_TITLE + " = ?";
                        String[] whereArgs = { albumName };

                        albumsDBHelper.Update(values, whereClause, whereArgs);
                    }
                }
                else if(path.getNameCount() == ARTIST_NAME_COUNT){
                    Log.d(TAG, "Init: artistName = " + path.getFileName());
                    Artist artist = new Artist(0, path.getFileName().toString());
                    Artists artistsDBHelper = new Artists(DBWriter);
                    artistsDBHelper.Insert(artist);
                }
                else if(path.getNameCount() == ALBUM_NAME_COUNT){
                    Log.d(TAG, "Init: albumName = " + path.getFileName());

                    String firstMusic = file.list()[0];

                    Path musicPath = Paths.get(path.toAbsolutePath() + "/" + firstMusic);
                    metadata = getMetadata(musicPath.toAbsolutePath().toString());

                    Album album = new Album(0, path.getFileName().toString(), metadata[4], path.getName(ARTIST_NAME_COUNT).toString(), metadata[2]);
                    Albums albumsDBHelper = new Albums(DBWriter);
                    albumsDBHelper.Insert(album);
                }



                /*
                switch (path.getNameCount()){
                    case ARTIST_NAME_COUNT:
                        Log.d(TAG, "Init: artistName = " + path.getFileName());
                        Artist artist = new Artist(0, path.getFileName().toString());
                        Artists artistsDBHelper = new Artists(DBWriter);
                        artistsDBHelper.Insert(artist);
                        break;
                    case ALBUM_NAME_COUNT:
                        Log.d(TAG, "Init: albumName = " + path.getFileName());

                        String firstMusic = file.list()[0];

                        Path musicPath = Paths.get(path.toAbsolutePath() + "/" + firstMusic);
                        metadata = getMetadata(musicPath.toAbsolutePath().toString());

                        Album album = new Album(0, path.getFileName().toString(), path.getName(ARTIST_NAME_COUNT).toString(), metadata[2]);
                        Albums albumsDBHelper = new Albums(DBWriter);
                        albumsDBHelper.Insert(album);
                        break;
                    case MUSIC_NAME_COUNT:
                        metadata = getMetadata(path.toAbsolutePath().toString());
                        Log.d(TAG, "Init: \nmusicName = " + path.getFileName() +
                                "\nmusicArtist = " + metadata[0] +
                                "\nmusicAlbum = " + metadata[1] +
                                "\nmusicGenre = " + metadata[2] +
                                "\nmusicDuration = " + metadata[3]);
                        Music music = new Music(0, path.getFileName().toString(), Double.parseDouble(metadata[3]) / 1000, "audio",  path.toAbsolutePath().toString(), metadata[2], metadata[0], metadata[1], false);
                        Musics musicsDBHelper = new Musics(DBWriter);
                        musicsDBHelper.Insert(music);
                        break;
                }*/
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

    public String[] getMetadata(String path){
        //TODO Strip genre and make sure they are in the DB
        //TODO - Extra: find the closes resembling genre
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String[] res = {"unknown", "unknown", "unknown", "0", "unknown"};
        try {
            mmr.setDataSource(path);

            String artist =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            String duration =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String albumImagePath = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                albumImagePath = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE);
            }


            Log.d(TAG, "metadata: artist = " + artist);
            Log.d(TAG, "metadata: album = " + album);
            Log.d(TAG, "metadata: genre = " + genre);
            Log.d(TAG, "metadata: duration = " + duration);

            if (artist != null){
                res[0] = artist.trim();
            }


            if (album != null){
                res[1] = album.trim();
            }

            if (genre != null){
                genre = StringUtil.Strip(genre.trim());
                genre = StringUtil.ReplaceAbbreviations(genre);

                if (NumberUtil.tryParseInt(genre)){
                    genre = Categories.getNameById(DBReader, Integer.parseInt(genre));
                }

                res[2] = genre;
            }

            if (duration != null){
                res[3] = duration.trim();
            }

            if (albumImagePath != null){
                res[4] = albumImagePath;
            }
        }
        catch(Exception e){}

        return res;
    }
}
