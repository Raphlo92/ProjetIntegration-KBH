package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.Classes.Music;

import java.util.ArrayList;

public class Musics extends AbstractDBHelper {
    //region BD values
    public static final String TABLE_NAME = DBHelper.Contract.TableMusic.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> musics;
    //endregion


    public Musics(SQLiteDatabase db){
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
        ArrayList<IDBClass> newMusics = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_TITLE));
            int length = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_LENGTH));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_TYPE));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_FILE));
            String artist = Artists.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_ID_ARTIST)));
            String category = Categories.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_ID_CATEGORY)));
            String album = Albums.getNameById(DB, cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_ID_ALBUM)));
            //endregion

            //TODO find if favorite or not
            newMusics.add(new Music(id, title, length, type, path, category,artist, album, false));
        }
        musics = newMusics;
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
}