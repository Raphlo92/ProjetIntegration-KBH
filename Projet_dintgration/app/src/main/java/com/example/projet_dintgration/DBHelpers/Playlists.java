package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.Classes.Playlist;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TablePlaylist;

import java.util.ArrayList;

public class Playlists extends AbstractDBHelper {
    //region BD values
    public static final String TABLE_NAME = TablePlaylist.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> playlists;
    //endregion

    public static ContentValues getTestValues1(){
        ContentValues values = new ContentValues();

        values.put(TablePlaylist._ID, 0);
        values.put(TablePlaylist.COLUMN_NAME_NAME, "Playlist1");
        values.put(TablePlaylist.COLUMN_NAME_TYPE, "normal");

        return values;
    }
    public static ContentValues getTestValues2(){
        ContentValues values = new ContentValues();

        values.put(TablePlaylist._ID, 1);
        values.put(TablePlaylist.COLUMN_NAME_NAME, "Playlist2");
        values.put(TablePlaylist.COLUMN_NAME_TYPE, "normal");

        return values;
    }

    public Playlists(SQLiteDatabase db){
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
        ArrayList<IDBClass> newPlaylists = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TablePlaylist._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(TablePlaylist.COLUMN_NAME_NAME));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(TablePlaylist.COLUMN_NAME_TYPE));
            //endregion
            newPlaylists.add(new Playlist(id, name, type));
        }
        playlists = newPlaylists;
        return newPlaylists;
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
}
