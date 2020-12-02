package com.example.projetdintegration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableMusic;
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
    //endregion

    public static ContentValues getTestValues1(){
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
    }

    public Musics(SQLiteDatabase db){
        super(db);
    }

    @Override
    public void Insert(ContentValues values) {
        //TODO check integrity of values
        createdRowId = DB.insert(TABLE_NAME, null, values);
    }

    public void Insert(Music music){
        ContentValues values = new ContentValues();

        int albumId = Albums.getIdByName(DB, music.getAlbum());
        int artistId = Artists.getIdByName(DB, music.getArtist());
        int categoryId = Categories.getIdByName(DB, music.getCategory());

        values.put(TableMusic.COLUMN_NAME_TITLE, music.getName());
        values.put(TableMusic.COLUMN_NAME_LENGTH, music.getLength());
        values.put(TableMusic.COLUMN_NAME_TYPE, music.getType());
        values.put(TableMusic.COLUMN_NAME_FILE, music.getPath());
        values.put(TableMusic.COLUMN_NAME_ID_ALBUM, albumId);
        values.put(TableMusic.COLUMN_NAME_ID_ARTIST, artistId);
        values.put(TableMusic.COLUMN_NAME_ID_CATEGORY, categoryId);

        Insert(values);
    }

    public void Insert(List<Music> musics){
        for (Music music : musics) {
            Insert(music);
        }
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
            String artist = Artists.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ID_ARTIST)));
            String category = Categories.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ID_CATEGORY)));
            String album = Albums.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ID_ALBUM)));
            boolean favorite = new Playlists(DB).isInFavorites(id);
            //endregion

            newMusics.add(new Music(id, title, length, type, path, category, artist, album, favorite));
        }
        musics = newMusics;
        cursor.close();
        return newMusics;
    }

    @Override
    public void Delete(String whereClause, String[] whereArgs) {
        //TODO check integrity of parameters
        deletedRows = DB.delete(TABLE_NAME, whereClause, whereArgs);
    }

    @Override
    public void Update(ContentValues values, String whereClause, String[] whereArgs) {
        nbUpdatedRows = DB.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public ArrayList<Integer> getAllUsedCategoriesIds(){
        //fixed by using playlist Id
        String[] columns = {TableMusic.COLUMN_NAME_ID_CATEGORY };
        String groupBy = TableMusic.COLUMN_NAME_ID_CATEGORY;
        Cursor cursor = DB.query(TableMusic.TABLE_NAME, columns , null, null, groupBy, null, null);

        ArrayList<Integer> ids = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableMusic.COLUMN_NAME_ID_CATEGORY));
            //endregion
            ids.add(id);
        }
        cursor.close();
        return ids;
    }

    public ArrayList<IDBClass> getAllUsedCategories(){
        ArrayList<IDBClass> musics;
        String CSIds = StringUtil.toCommaSeparatedString(getAllUsedCategoriesIds());

        Log.d(TAG, "getAllUsedCategories: CSIds = " + CSIds);
        Categories categoriesDBHelper = new Categories(DB);
        String[] columns = {
                DBHelper.Contract.TableCategory._ID,
                DBHelper.Contract.TableCategory.COLUMN_NAME_NAME
        };

        String whereClause = DBHelper.Contract.TableCategory._ID + " IN (" + CSIds + ")";
        whereClause = DBHelper.Contract.TableCategory._ID + " IN (" + CSIds + ")";


        musics = categoriesDBHelper.Select(columns, whereClause, null, null, null, null);

        return musics;
    }
}