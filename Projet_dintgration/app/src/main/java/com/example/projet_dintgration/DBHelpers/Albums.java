package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.Album;
import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TableAlbum;

import java.util.ArrayList;

public class Albums extends AbstractDBHelper{
    //region BD values
    public static final String TABLE_NAME = TableAlbum.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> albums;
    //endregion

    public static ContentValues getTestValues1(){
        ContentValues values = new ContentValues();

        values.put(TableAlbum._ID, 0);
        values.put(TableAlbum.COLUMN_NAME_TITLE, "Album1");
        values.put(TableAlbum.COLUMN_NAME_ID_ARTIST, 0);
        values.put(TableAlbum.COLUMN_NAME_ID_CATEGORY, 1);

        return values;
    }
    public static ContentValues getTestValues2(){
        ContentValues values = new ContentValues();

        values.put(TableAlbum._ID, 1);
        values.put(TableAlbum.COLUMN_NAME_TITLE, "Album2");
        values.put(TableAlbum.COLUMN_NAME_ID_ARTIST, 1);
        values.put(TableAlbum.COLUMN_NAME_ID_CATEGORY, 0);

        return values;
    }

    public Albums(SQLiteDatabase db){
        super(db);
    }

    @Override
    public void Insert(ContentValues values) {
        //TODO check integrity of values
        createdRowId = DB.insert(TABLE_NAME, null, values);
    }

    @Override
    public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy) {
        //TODO check integrity of parameters
        Cursor cursor = DB.query(TABLE_NAME, columns, whereClause, whereArgs, groupBy, having, orderBy);
        ArrayList<IDBClass> newAlbums = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableAlbum._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TableAlbum.COLUMN_NAME_TITLE));
            String artist = Artists.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(TableAlbum.COLUMN_NAME_ID_ARTIST)));
            String category = Categories.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(TableAlbum.COLUMN_NAME_ID_CATEGORY)));
            //endregion
            newAlbums.add(new Album(id, title, artist, category));
        }
        albums = newAlbums;
        return newAlbums;
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
        String selection = TableAlbum._ID + " = ?";
        String[] selectionArgs = { id + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(TableAlbum.COLUMN_NAME_TITLE));
        }

        return name;
    }
}
