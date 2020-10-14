package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.Classes.Playlist;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TablePlaylist;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TableMusicPlaylist;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TableMusic;

import java.util.ArrayList;
import java.util.List;

public class Playlists extends AbstractDBHelper {
    private static final String TAG = "Playlists";
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
        values.put(TablePlaylist.COLUMN_NAME_TYPE, "favorites");

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

    public void Insert(Playlist playlist){
        ContentValues values = new ContentValues();

        values.put(TablePlaylist.COLUMN_NAME_NAME, playlist.getName());
        values.put(TablePlaylist.COLUMN_NAME_TYPE, playlist.getType());

        Insert(values);
    }

    public void Insert(List<Playlist> playlists){
        for (Playlist playlist : playlists) {
            Insert(playlist);
        }
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
        cursor.close();
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

    public void AddToPlaylist(int musicId, int playlistId){
        ContentValues values = new ContentValues();

        values.put(TableMusicPlaylist.COLUMN_NAME_ID_MUSIC, musicId);
        values.put(TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST, playlistId);

        DB.insert(TableMusicPlaylist.TABLE_NAME, null, values);
    }

    public ArrayList<Integer> getAllMusicsIdsInPlaylist(int playlistId){
        String[] columns = { TableMusicPlaylist.COLUMN_NAME_ID_MUSIC };
        Cursor cursor = DB.query(TableMusicPlaylist.TABLE_NAME, columns , null, null, null, null, null);

        ArrayList<Integer> ids = new ArrayList<>();

        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableMusicPlaylist.COLUMN_NAME_ID_MUSIC));
            //endregion
            ids.add(id);
        }


        cursor.close();
        return ids;
    }

    private String toCommaSeparatedString(ArrayList<Integer> list) {
        if (list.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();
            for (Integer item : list) {
                nameBuilder.append(item).append(", ");
            }
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            return nameBuilder.toString();
        } else {
            return "";
        }
    }

    public ArrayList<IDBClass> getAllMusicsInPlaylist(int playlistId){
        ArrayList<IDBClass> musics;
        String CSIds = toCommaSeparatedString(getAllMusicsIdsInPlaylist(playlistId));
        Log.d(TAG, "getAllMusicsInPlaylist: CSIds = " + CSIds);
        Musics musicsDBHelper = new Musics(DB);

        String[] columns = {
                TableMusic._ID,
                TableMusic.COLUMN_NAME_TITLE,
                TableMusic.COLUMN_NAME_LENGTH,
                TableMusic.COLUMN_NAME_FILE,
                TableMusic.COLUMN_NAME_TYPE,
                TableMusic.COLUMN_NAME_ID_ALBUM,
                TableMusic.COLUMN_NAME_ID_ARTIST,
                TableMusic.COLUMN_NAME_ID_CATEGORY
        };

        String whereClause = TableMusic._ID + " IN (" + CSIds + ")";

        musics = musicsDBHelper.Select(columns, whereClause, null, null, null, null);

        return musics;
    }
}
