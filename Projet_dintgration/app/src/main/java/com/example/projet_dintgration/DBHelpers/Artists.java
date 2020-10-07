package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.Artist;
import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;

import java.util.ArrayList;

public class Artists extends AbstractDBHelper {
    //region BD values
    public static final String TABLE_NAME = DBHelper.Contract.TableArtist.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> artists;
    //endregion


    public Artists(SQLiteDatabase db){
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
        ArrayList<IDBClass> newArtists = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableArtist._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableArtist.COLUMN_NAME_NAME));
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
        String selection = DBHelper.Contract.TableArtist._ID + " = ?";
        String[] selectionArgs = { id + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableArtist.COLUMN_NAME_NAME));;
        }

        return name;
    }
}
