package com.example.projetdintegration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projetdintegration.DBHelpers.Classes.Artist;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableArtist;

import java.util.ArrayList;
import java.util.List;

public class Artists extends AbstractDBHelper {
    //region BD values
    public static final String TABLE_NAME = TableArtist.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> artists;
    //endregion

    public static ContentValues getTestValues1(){
        ContentValues values = new ContentValues();

        values.put(TableArtist._ID, 0);
        values.put(TableArtist.COLUMN_NAME_NAME, "Artist1");

        return values;
    }
    public static ContentValues getTestValues2(){
        ContentValues values = new ContentValues();

        values.put(TableArtist._ID, 1);
        values.put(TableArtist.COLUMN_NAME_NAME, "Artist2");

        return values;
    }

    public Artists(SQLiteDatabase db){
        super(db);
    }

    @Override
    public void Insert(ContentValues values) {
        //TODO check integrity of values
        createdRowId = DB.insert(TABLE_NAME, null, values);
    }

    public void Insert(Artist artist){
        ContentValues values = new ContentValues();

        values.put(TableArtist.COLUMN_NAME_NAME, artist.getName());

        Insert(values);
    }

    public void Insert(List<Artist> artists){
        for (Artist artist : artists) {
            Insert(artist);
        }
    }

    @Override
    public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy) {
        //TODO check integrity of parameters
        Cursor cursor = DB.query(TABLE_NAME, columns, whereClause, whereArgs, groupBy, having, orderBy);
        ArrayList<IDBClass> newArtists = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableArtist._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(TableArtist.COLUMN_NAME_NAME));
            //endregion
            newArtists.add(new Artist(id, name));
        }
        artists = newArtists;
        return newArtists;
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

    public static String getNameById(SQLiteDatabase db, int id){
        String name = null;
        String selection = TableArtist._ID + " = ?";
        String[] selectionArgs = { id + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(TableArtist.COLUMN_NAME_NAME));;
        }

        cursor.close();
        return name;
    }

    public static int getIdByName(SQLiteDatabase db, String name){
        int id = -1;
        String selection = TableArtist.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { name };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndexOrThrow(TableArtist._ID));
        }

        cursor.close();
        return id;
    }
}
