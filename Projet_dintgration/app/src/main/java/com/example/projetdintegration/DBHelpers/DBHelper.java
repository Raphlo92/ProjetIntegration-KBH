package com.example.projetdintegration.DBHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    Context mContext;
    private static final String TAG = "DBHelper";
    public static final class Contract{
        private Contract(){}

        public static class TableMusic implements BaseColumns {
            public static final String TABLE_NAME = "music";
            public static final String COLUMN_NAME_TITLE = "title";
            public static final String COLUMN_NAME_LENGTH = "length";
            public static final String COLUMN_NAME_TYPE = "type";
            public static final String COLUMN_NAME_FILE = "file";
            public static final String COLUMN_NAME_ARTIST = "artist";
            public static final String COLUMN_NAME_ALBUM = "album";
            public static final String COLUMN_NAME_CATEGORY = "category";
        }

        /*public static class TableArtist implements BaseColumns {
            public static final String TABLE_NAME = "artist";
            public static final String COLUMN_NAME_NAME = "name";
        }

        public static class TableAlbum implements BaseColumns {
            public static final String TABLE_NAME = "album";
            public static final String COLUMN_NAME_TITLE = "title";
            public static final String COLUMN_NAME_IMAGE = "image";
            public static final String COLUMN_NAME_ID_ARTIST = "idartist";
            public static final String COLUMN_NAME_ID_CATEGORY = "idcategory";
        }

        public static class TableCategory implements BaseColumns {
            public static final String TABLE_NAME = "category";
            public static final String COLUMN_NAME_NAME = "name";
        }*/

        public static class TablePlaylist implements BaseColumns {
            public static final String TABLE_NAME = "playlist";
            public static final String COLUMN_NAME_NAME = "name";
            public static final String COLUMN_NAME_TYPE = "type";
            public static final String NORMAL_TYPE = "normal";
            public static final String RELATIVE_TYPE = "relative";
            public static final String FAVORITES = "favorites";
        }

        public static class TableMusicPlaylist implements BaseColumns {
            public static final String TABLE_NAME = "music_playlist";
            public static final String COLUMN_NAME_ID_PLAYLIST = "idplaylist";
            public static final String COLUMN_NAME_ID_MUSIC = "idmusic";
        }
    }

    //region SQL
    public static final String SQL_CREATE_TABLE_MUSIC =
            "CREATE TABLE IF NOT EXISTS " + Contract.TableMusic.TABLE_NAME + " (" +
                    Contract.TableMusic._ID + " INTEGER PRIMARY KEY UNIQUE," +
                    Contract.TableMusic.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_LENGTH + " INTEGER NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_TYPE + " TEXT NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_FILE + " TEXT NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_ARTIST + " TEXT NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_ALBUM + " TEXT NOT NULL," +
                    Contract.TableMusic.COLUMN_NAME_CATEGORY + " TEXT)" ;

    /*public static final String SQL_CREATE_TABLE_ARTIST =
            "CREATE TABLE IF NOT EXISTS " + Contract.TableArtist.TABLE_NAME + " (" +
                    Contract.TableArtist._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.TableArtist.COLUMN_NAME_NAME + " TEXT NOT NULL)";

    public static final String SQL_CREATE_TABLE_ALBUM =
            "CREATE TABLE IF NOT EXISTS " + Contract.TableAlbum.TABLE_NAME + " (" +
                    Contract.TableAlbum._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.TableAlbum.COLUMN_NAME_ID_ARTIST + " INTEGER NOT NULL," +
                    Contract.TableAlbum.COLUMN_NAME_ID_CATEGORY + " INTEGER NOT NULL," +
                    Contract.TableAlbum.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    Contract.TableAlbum.COLUMN_NAME_IMAGE + " TEXT NOT NULL," +
                    "FOREIGN KEY (" + Contract.TableAlbum.COLUMN_NAME_ID_ARTIST + ")" +
                    "REFERENCES " + Contract.TableArtist.TABLE_NAME + "(" + Contract.TableArtist._ID + ")" +
                    "ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + Contract.TableAlbum.COLUMN_NAME_ID_CATEGORY + ")" +
                    "REFERENCES " + Contract.TableCategory.TABLE_NAME + "(" + Contract.TableCategory._ID + ")" +
                    "ON DELETE CASCADE)";

    public static final String SQL_CREATE_TABLE_CATEGORY =
            "CREATE TABLE IF NOT EXISTS " + Contract.TableCategory.TABLE_NAME + " (" +
                    Contract.TableCategory._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.TableCategory.COLUMN_NAME_NAME + " TEXT NOT NULL)";*/

    public static final String SQL_CREATE_TABLE_PLAYLIST =
            "CREATE TABLE IF NOT EXISTS " + Contract.TablePlaylist.TABLE_NAME + " (" +
                    Contract.TablePlaylist._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.TablePlaylist.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                    Contract.TablePlaylist.COLUMN_NAME_TYPE + " TEXT NOT NULL)";

    public static final String SQL_CREATE_TABLE_MUSIC_PLAYLIST =
            "CREATE TABLE IF NOT EXISTS " + Contract.TableMusicPlaylist.TABLE_NAME + " (" +
                    Contract.TableMusicPlaylist.COLUMN_NAME_ID_MUSIC + " INTEGER," +
                    Contract.TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST + " INTEGER," +
                    "PRIMARY KEY (" + Contract.TableMusicPlaylist.COLUMN_NAME_ID_MUSIC + ", " + Contract.TableMusicPlaylist.COLUMN_NAME_ID_PLAYLIST + "))";

    public static final String SQL_DELETE_TABLE_MUSIC =
            "DROP TABLE IF EXISTS " + Contract.TableMusic.TABLE_NAME;

    /*public static final String SQL_DELETE_TABLE_ARTIST =
            "DROP TABLE IF EXISTS " + Contract.TableArtist.TABLE_NAME;

    public static final String SQL_DELETE_TABLE_ALBUM =
            "DROP TABLE IF EXISTS " + Contract.TableAlbum.TABLE_NAME;

    public static final String SQL_DELETE_TABLE_CATEGORY =
            "DROP TABLE IF EXISTS " + Contract.TableCategory.TABLE_NAME;*/

    public static final String SQL_DELETE_TABLE_PLAYLIST =
            "DROP TABLE IF EXISTS " + Contract.TablePlaylist.TABLE_NAME;

    public static final String SQL_DELETE_TABLE_MUSIC_PLAYLIST =
            "DROP TABLE IF EXISTS " + Contract.TableMusicPlaylist.TABLE_NAME;
    //endregion

    public static final int DB_VERSION = 2;
    public static final String DB_NAME = "AndroidMusique.db";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_MUSIC);
        /*db.execSQL(SQL_CREATE_TABLE_ARTIST);
        db.execSQL(SQL_CREATE_TABLE_ALBUM);
        db.execSQL(SQL_CREATE_TABLE_CATEGORY);*/
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST);
        db.execSQL(SQL_CREATE_TABLE_MUSIC_PLAYLIST);
        Log.d(TAG, "onCreate: Started");
        Playlists playlistsDBHelper = new Playlists(db, mContext);
        playlistsDBHelper.initFavorites();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion){
            db.execSQL(SQL_DELETE_TABLE_MUSIC_PLAYLIST);
            /*db.execSQL(SQL_DELETE_TABLE_MUSIC);
            db.execSQL(SQL_DELETE_TABLE_ALBUM);
            db.execSQL(SQL_DELETE_TABLE_ARTIST);
            db.execSQL(SQL_DELETE_TABLE_CATEGORY);*/
            db.execSQL(SQL_DELETE_TABLE_PLAYLIST);
            onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /*public void enterTestValues(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(Contract.TableArtist.TABLE_NAME,null,  Artists.getTestValues1());
        db.insert(Contract.TableArtist.TABLE_NAME,null,  Artists.getTestValues2());

        db.insert(Contract.TableCategory.TABLE_NAME,null,  Categories.getTestValues1());
        db.insert(Contract.TableCategory.TABLE_NAME,null,  Categories.getTestValues2());

        db.insert(Contract.TablePlaylist.TABLE_NAME,null,  Playlists.getTestValues1());
        db.insert(Contract.TablePlaylist.TABLE_NAME,null,  Playlists.getTestValues2());

        db.insert(Contract.TableAlbum.TABLE_NAME,null,  Albums.getTestValues1());
        db.insert(Contract.TableAlbum.TABLE_NAME,null,  Albums.getTestValues2());

        db.insert(Contract.TableMusic.TABLE_NAME,null,  Musics.getTestValues1());
        db.insert(Contract.TableMusic.TABLE_NAME,null,  Musics.getTestValues2());
        db.insert(Contract.TableMusic.TABLE_NAME,null,  Musics.getTestValues3());
        db.insert(Contract.TableMusic.TABLE_NAME,null,  Musics.getTestValues4());

        Playlists playlists = new Playlists(db);
        playlists.AddToPlaylist(0, 0);
        playlists.AddToPlaylist(1, 0);
        playlists.AddToPlaylist(0, 1);
        playlists.AddToPlaylist(3, 1);
        playlists.AddToPlaylist(2, 1);
    }*/
}
