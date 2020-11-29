package com.example.projetdintegration.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.example.projetdintegration.DBHelpers.Classes.Category;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
//import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableMusic;
import com.example.projetdintegration.Utilities.NumberUtil;
import com.example.projetdintegration.Utilities.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Musics {
    private static final String TAG = "Musics";
    //region BD values
    //public static final String TABLE_NAME = TableMusic.TABLE_NAME;
    //public long createdRowId;
    //public int deletedRows;
    //public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> musics;
    Context mContext;
    //endregion

    /*public static ContentValues getTestValues1(){
        ContentValues values = new ContentValues();

        values.put(TableMusic._ID, 0);
        values.put(TableMusic.COLUMN_NAME_TITLE, "Music1");
        values.put(TableMusic.COLUMN_NAME_LENGTH, 120);
        values.put(TableMusic.COLUMN_NAME_TYPE, "audio");
        values.put(TableMusic.COLUMN_NAME_FILE, "path");
        values.put(TableMusic.COLUMN_NAME_ID_ALBUM, 0);
        values.put(TableMusic.COLUMN_NAME_ID_ARTIST, 0);
        values.put(TableMusic.COLUMN_NAME_ID_CATEGORY, 1);

        return values;
    }
    public static ContentValues getTestValues2(){
        ContentValues values = new ContentValues();

        values.put(TableMusic._ID, 1);
        values.put(TableMusic.COLUMN_NAME_TITLE, "Music2");
        values.put(TableMusic.COLUMN_NAME_LENGTH, 120);
        values.put(TableMusic.COLUMN_NAME_TYPE, "audio");
        values.put(TableMusic.COLUMN_NAME_FILE, "path");
        values.put(TableMusic.COLUMN_NAME_ID_ALBUM, 1);
        values.put(TableMusic.COLUMN_NAME_ID_ARTIST, 1);
        values.put(TableMusic.COLUMN_NAME_ID_CATEGORY, 0);

        return values;
    }
    public static ContentValues getTestValues3(){
        ContentValues values = new ContentValues();

        values.put(TableMusic._ID, 2);
        values.put(TableMusic.COLUMN_NAME_TITLE, "Music3");
        values.put(TableMusic.COLUMN_NAME_LENGTH, 120);
        values.put(TableMusic.COLUMN_NAME_TYPE, "audio");
        values.put(TableMusic.COLUMN_NAME_FILE, "path");
        values.put(TableMusic.COLUMN_NAME_ID_ALBUM, 0);
        values.put(TableMusic.COLUMN_NAME_ID_ARTIST, 0);
        values.put(TableMusic.COLUMN_NAME_ID_CATEGORY, 1);

        return values;
    }
    public static ContentValues getTestValues4(){
        ContentValues values = new ContentValues();

        values.put(TableMusic._ID, 3);
        values.put(TableMusic.COLUMN_NAME_TITLE, "Music4");
        values.put(TableMusic.COLUMN_NAME_LENGTH, 120);
        values.put(TableMusic.COLUMN_NAME_TYPE, "audio");
        values.put(TableMusic.COLUMN_NAME_FILE, "path");
        values.put(TableMusic.COLUMN_NAME_ID_ALBUM, 1);
        values.put(TableMusic.COLUMN_NAME_ID_ARTIST, 1);
        values.put(TableMusic.COLUMN_NAME_ID_CATEGORY, 0);

        return values;
    }*/

    public Musics(Context context){
        mContext = context;
    }

    public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<IDBClass> musics = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(mContext);
        Playlists playlistsDBHelper = new Playlists(dbHelper.getReadableDatabase(), mContext);

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, whereClause, whereArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor == null){
            return null;
        }

        while(cursor.moveToNext()){
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            mmr.setDataSource(path);
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)).trim();
            String type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
            String artist =  cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)).trim();
            String album =  cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)).trim();
            /*String genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            String duration =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            if (genre != null){
                genre = StringUtil.Strip(genre.trim());
                genre = StringUtil.ReplaceAbbreviations(genre);

                if (NumberUtil.tryParseInt(genre)){
                    genre = getCategoryNameById(mContext, Integer.parseInt(genre));
                }
            }

            if (duration != null){
                duration = duration.trim();
            }
            else
                duration = "0";*/

            musics.add(new Music(id, title, 0, type, path, null, artist, album, playlistsDBHelper.isInFavorites(id)));
        }
        cursor.close();
        return musics;
    }

    public ArrayList<Integer> getAllUsedCategoriesIds(){
        ArrayList<Integer> ids = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(mContext);
        Playlists playlistsDBHelper = new Playlists(dbHelper.getReadableDatabase(), mContext);

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        if(cursor == null){
            return null;
        }

        while(cursor.moveToNext()){
            int id = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
            Cursor cursor2 = mContext.getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", id), null, null, null, null);
            if (cursor2 != null){
                if(cursor2.moveToNext()){
                    ids.add(id);
                    cursor2.close();
                }
            }

        }

        cursor.close();
        return ids;
    }

    public ArrayList<IDBClass> getAllUsedCategories(){
        ArrayList<IDBClass> categories = new ArrayList<>();
        String CSIds = StringUtil.toCommaSeparatedString(getAllUsedCategoriesIds());
        String whereClause = MediaStore.Audio.Genres._ID + " IN (" + CSIds + ")";
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, whereClause, null,
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        if(cursor == null){
            return null;
        }

        while(cursor.moveToNext()){
            int id = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)).trim();

            categories.add(new Category(id, name));
        }

        cursor.close();
        return categories;
    }

    static public String getCategoryNameById(Context context, int id) {
        String name = null;
        String selection = MediaStore.Audio.Genres._ID + " = ?";
        String[] selectionArgs = { id + "" };
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, selection, selectionArgs,
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));
        }

        cursor.close();
        return name;
    }
}