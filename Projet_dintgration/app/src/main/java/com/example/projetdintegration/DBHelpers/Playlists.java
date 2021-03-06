package com.example.projetdintegration.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TablePlaylist;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableMusicPlaylist;
//import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TableMusic;
import com.example.projetdintegration.Utilities.NumberUtil;
import com.example.projetdintegration.Utilities.StringUtil;

import java.io.File;
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

    Context mContext;

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

    public Playlists(SQLiteDatabase db, Context context){
        super(db);
        mContext = context;
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

    public void initFavorites(){
        Insert(new Playlist(0, "Favoris", TablePlaylist.FAVORITES));
    }

    public void AddToPlaylist(int musicId, int playlistId){
        if(playlistId != -1){
            ContentValues values = new ContentValues();

            values.put(TableMusicPlaylist.COLUMN_NAME_ID_MUSIC, musicId);
            values.put(TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST, playlistId);

            DB.insert(TableMusicPlaylist.TABLE_NAME, null, values);
        }
    }

    public void RemoveFromPlaylist(int musicId, int playlistId){
        String whereClause = TableMusicPlaylist.COLUMN_NAME_ID_MUSIC + " = ? AND " + TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST + " = ?";
        String[] whereArgs = {musicId + "", playlistId + ""};
        DB.delete(TableMusicPlaylist.TABLE_NAME, whereClause, whereArgs);
    }

    public void RemoveFromFavorites(int musicId){
        RemoveFromPlaylist(musicId, getFavoritesId());
    }

    public void AddToFavorites(int musicId){
        Log.d(TAG, "AddToFavorites: musicLikedId = " + musicId);
        AddToPlaylist(musicId, getFavoritesId());
    }

    public boolean isInFavorites(int musicId){
        return isInPlaylist(musicId, getFavoritesId());
    }

    public boolean isInPlaylist(int musicId, int playlistId){
        String[] columns = { TableMusicPlaylist.COLUMN_NAME_ID_MUSIC };
        String where = TableMusicPlaylist.COLUMN_NAME_ID_MUSIC + " = ? AND " + TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST + " = ?";
        String[] whereArgs = {musicId + "", playlistId + ""};
        Cursor cursor = DB.query(TableMusicPlaylist.TABLE_NAME, columns , where, whereArgs, null, null, null);
        boolean inPlaylist = cursor.moveToNext();
        cursor.close();
        return inPlaylist;
    }

    public int getFavoritesId(){
        String[] columns = { TablePlaylist._ID };
        int favoritesId = -1;
        String where = TablePlaylist.COLUMN_NAME_TYPE + " = ?";
        String[] whereArgs = {TablePlaylist.FAVORITES};
        Cursor cursor = DB.query(TablePlaylist.TABLE_NAME, columns , where, whereArgs, null, null, null);
        while (cursor.moveToNext()){
            favoritesId =  cursor.getInt(cursor.getColumnIndexOrThrow(TablePlaylist._ID));
        }

        Log.d(TAG, "getFavoritesId: Id = " + favoritesId);
        cursor.close();
        return favoritesId;
    }

    public ArrayList<Integer> getAllMusicsIdsInPlaylist(int playlistId){
        //fixed by using playlist Id
        String[] columns = { TableMusicPlaylist.COLUMN_NAME_ID_MUSIC };
        String where = TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST + " = ?";
        String[] whereArgs = { playlistId + ""};
        Cursor cursor = DB.query(TableMusicPlaylist.TABLE_NAME, columns , where, whereArgs, null, null, null);

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

    public ArrayList<IDBClass> getAllMusicsInPlaylist(int playlistId){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<IDBClass> musics = new ArrayList<>();
        String CSIds = StringUtil.toCommaSeparatedString(getAllMusicsIdsInPlaylist(playlistId)) ;
        String whereClause = DBHelper.Contract.TableMusic._ID + " IN (" + CSIds + ")";

        Musics DBMusicReader = new Musics(new DBHelper(mContext).getReadableDatabase(), mContext);

        musics = DBMusicReader.SavedSelect(null, whereClause, null, null, null, null);

        return musics;
    }

    public ArrayList<String> getAllArtistsInPlaylist(int playlistId){
        ArrayList<String> artists = new ArrayList<>();
        String[] columns = {DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST};
        String CSIds = StringUtil.toCommaSeparatedString(getAllMusicsIdsInPlaylist(playlistId)) ;
        String whereClause = DBHelper.Contract.TableMusic._ID + " IN (" + CSIds + ")";

        Cursor cursor = DB.query(DBHelper.Contract.TableMusic.TABLE_NAME, columns, whereClause, null, DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST, null, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST));
            artists.add("\""+name+"\"");
        }
        cursor.close();
        return artists;
    }

    public ArrayList<String> getAllAlbumsInPlaylist(int playlistId){
        ArrayList<String> albums = new ArrayList<>();
        String[] columns = {DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM};
        String CSIds = StringUtil.toCommaSeparatedString(getAllMusicsIdsInPlaylist(playlistId)) ;
        String whereClause = DBHelper.Contract.TableMusic._ID + " IN (" + CSIds + ")";

        Cursor cursor = DB.query(DBHelper.Contract.TableMusic.TABLE_NAME, columns, whereClause, null, DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM, null, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM));
            albums.add("\""+name+"\"");
        }
        cursor.close();
        return albums;
    }

    public ArrayList<String> getAllCategoriesInPlaylist(int playlistId){
        ArrayList<String> categories = new ArrayList<>();
        String[] columns = {DBHelper.Contract.TableMusic.COLUMN_NAME_CATEGORY};
        String CSIds = StringUtil.toCommaSeparatedString(getAllMusicsIdsInPlaylist(playlistId)) ;
        String whereClause = DBHelper.Contract.TableMusic._ID + " IN (" + CSIds + ")";

        Cursor cursor = DB.query(DBHelper.Contract.TableMusic.TABLE_NAME, columns, whereClause, null, DBHelper.Contract.TableMusic.COLUMN_NAME_CATEGORY, null, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.Contract.TableMusic.COLUMN_NAME_CATEGORY));
            categories.add("\""+name+"\"");
        }
        cursor.close();
        return categories;
    }

    public static String getPlaylistName(SQLiteDatabase db, int playlistId){
        String name = null;
        String selection = TablePlaylist._ID + " = ?";
        String[] selectionArgs = { playlistId + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(TablePlaylist.COLUMN_NAME_NAME));
        }

        cursor.close();
        return name;
    }

    public static Playlist getPlaylistById(SQLiteDatabase db, int playlistId){
        Playlist playlist = new Playlist(playlistId, "", "");
        String selection = TablePlaylist._ID + " = ?";
        String[] selectionArgs = { playlistId + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            playlist.setName(cursor.getString(cursor.getColumnIndexOrThrow(TablePlaylist.COLUMN_NAME_NAME)));
            playlist.setType(cursor.getString(cursor.getColumnIndexOrThrow(TablePlaylist.COLUMN_NAME_TYPE)));
        }

        cursor.close();
        return playlist;
    }
}
