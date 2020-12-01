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
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableMusic;
import com.example.projetdintegration.Utilities.NumberUtil;
import com.example.projetdintegration.Utilities.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Musics extends AbstractDBHelper {
    private static final String TAG = "Musics";
    //region BD values
    public static final String TABLE_NAME = TableMusic.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> musics;
    Context mContext;
    //endregion

    public Musics(SQLiteDatabase db, Context context){
        super(db);
        mContext = context;
    }

    @Override
    public void Insert(ContentValues values) {
        //TODO check integrity of values
        createdRowId = DB.insert(TABLE_NAME, null, values);
    }

    public void Insert(Music music){
        ContentValues values = new ContentValues();

        values.put(TableMusic._ID, music.getId());
        values.put(TableMusic.COLUMN_NAME_TITLE, music.getName());
        values.put(TableMusic.COLUMN_NAME_LENGTH, music.getLength());
        values.put(TableMusic.COLUMN_NAME_TYPE, music.getType());
        values.put(TableMusic.COLUMN_NAME_FILE, music.getPath());
        values.put(TableMusic.COLUMN_NAME_ALBUM, music.getAlbum());
        values.put(TableMusic.COLUMN_NAME_ARTIST, music.getArtist());
        values.put(TableMusic.COLUMN_NAME_CATEGORY, music.getCategory());

        Insert(values);
    }

    public void Insert(List<Music> musics){
        for (Music music : musics) {
            Insert(music);
        }
    }

    @Override
    public void Delete(String whereClause, String[] whereArgs) {
        deletedRows = DB.delete(TABLE_NAME, whereClause, whereArgs);
    }

    @Override
    public void Update(ContentValues values, String whereClause, String[] whereArgs) {
        nbUpdatedRows = DB.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    @Override
    public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy) {
        //TODO check integrity of parameters
        Cursor cursor = DB.query(TABLE_NAME, columns, whereClause, whereArgs, groupBy, having, orderBy);
        ArrayList<IDBClass> newMusics = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_TITLE));
            int length = cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_LENGTH));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_TYPE));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_FILE));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ARTIST));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_CATEGORY));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ALBUM));
            boolean favorite = new Playlists(DB, mContext).isInFavorites(id);
            //endregion

            Log.d(TAG, "Select: "+ title +" category = " + category);
            newMusics.add(new Music(id, title, length, type, path, category, artist, album, favorite));
        }
        musics = newMusics;
        cursor.close();
        return newMusics;
    }


    /*public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs) {
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
            String genre =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            String duration =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            if (genre != null){
                genre = StringUtil.Strip(genre.trim());
                genre = StringUtil.ReplaceAbbreviations(genre);

                if (NumberUtil.tryParseInt(genre)){
                    genre = getCategoryNameById(mContext, Integer.parseInt(genre));
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

            Log.d(TAG, "Select: genre = " + genre);
            musics.add(new Music(id, title, Long.parseLong(duration), type, path, genre, artist, album, playlistsDBHelper.isInFavorites(id)));
        }
        cursor.close();
        return musics;
    }*/

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
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Genres._ID));
            Cursor cursor2 = mContext.getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", id), null, null, null, null);
            Log.d(TAG, "getAllUsedCategoriesIds: " + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)) + ": " + cursor2);
            if (cursor2 != null){
                Log.d(TAG, "getAllUsedCategoriesIds: count = " + cursor2.getCount());
                if(cursor2.getCount() > 0){
                    cursor2.moveToFirst();
                    Log.d(TAG, "getAllUsedCategoriesIds: count = " + cursor2.getCount());
                    Log.d(TAG, "getAllUsedCategoriesIds: " + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)) + " firstFile = " + cursor2.getString(cursor2.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.TITLE)));
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
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Genres._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)).trim();
            Log.d(TAG, "getAllUsedCategories: name = " + name);
            categories.add(new Category(id, name));
        }

        cursor.close();
        return categories;
    }

    public ArrayList<Music> getAllMusicInCategory(int catId){
        ArrayList<Music> catMusics = new ArrayList<>();

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", catId), null, null, null, null);

        if(cursor == null){
            return null;
        }

        while(cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.TITLE));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.MIME_TYPE));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.DATA));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.ALBUM));
            boolean favorite = new Playlists(DB, mContext).isInFavorites(id);
            //endregion

            catMusics.add(new Music(id, title, 0, type, path, null, artist, album, favorite));
        }

        return catMusics;
    }

    static public String getCategoryNameById(Context context, int id) {
        String name = "unknown";
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