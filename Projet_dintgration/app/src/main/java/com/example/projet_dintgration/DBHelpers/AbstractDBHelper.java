package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;

import java.util.ArrayList;

abstract public class AbstractDBHelper {
    public SQLiteDatabase DB;
    public AbstractDBHelper(SQLiteDatabase db){
        DB = db;
    }

    abstract public void Insert(ContentValues values);
    abstract public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy);
    abstract public void Delete(String whereClause, String[] whereArgs);
    abstract public void Update(ContentValues values, String selection, String[] selectionArgs);
}
