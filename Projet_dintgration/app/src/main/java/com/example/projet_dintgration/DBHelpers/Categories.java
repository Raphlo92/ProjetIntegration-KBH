package com.example.projet_dintgration.DBHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projet_dintgration.DBHelpers.Classes.Category;
import com.example.projet_dintgration.DBHelpers.Classes.IDBClass;
import com.example.projet_dintgration.DBHelpers.DBHelper.Contract.TableCategory;

import java.util.ArrayList;
import java.util.List;

public class Categories extends AbstractDBHelper {

    //region BD values
    public static final String TABLE_NAME = TableCategory.TABLE_NAME;
    public long createdRowId;
    public int deletedRows;
    public int nbUpdatedRows;
    //endregion

    //region loaded values
    public ArrayList<IDBClass> categories;
    //endregion

    public static ContentValues getTestValues1(){
        ContentValues values = new ContentValues();

        values.put(TableCategory._ID, 0);
        values.put(TableCategory.COLUMN_NAME_NAME, "Category1");

        return values;
    }

    public void init() {
        ContentValues values;
        String[] music = {"Blues", "Classic rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other ", "Pop", "Rhythm and Blues", "Rap", "Reggae", "Rock", "Techno"
                , "Industrial", "Alternative", "Ska", "Death metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz & Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound clip", "Gospel", "Noise"
                , "Alternative Rock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy"
                , "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native US", "Cabaret", "New Wave", "Psychedelic", "Rave", "Show tunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock ’n’ Roll", "Hard rock", "Folk", "Folk-Rock"
                , "National Folk", "Swing", "Fast Fusion", "Bebop", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber music", "Sonata"
                , "Symphony", "Booty bass", "Primus", "Porn groove", "Satire", "Slow jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum solo", "A cappella", "Euro-House", "Dancehall", "Goa", "Drum & Bass", "Club-House", "Hardcore Techno"
                , "Terror", "Indie", "BritPop", "Negerpunk ", "Polsk Punk ", "Beat", "Christian Gangsta Rap ", "Heavy Metal ", "Black Metal ", "Crossover ", "Contemporary Christian", "Christian rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "Jpop", "Synthpop", "Abstract ", "Art Rock", "Baroque"
                , "Bhangra ", "Big beat", "Breakbeat", "Chillout ", "Downtempo", "Dub", "EBM ", "Eclectic", "Electro", "Electroclash ", "Emo", "Experimental", "Garage", "Global", "IDM", "Illbient ", "Industro-Goth", "Jam Band", "Krautrock ", "Leftfield", "Lounge"
                , "Math Rock", "New Romantic", "Nu-Breakz", "Post-Punk ", "Post-Rock", "Psytrance", "Shoegaze ", "Space Rock", "Trop Rock", "World Music", "Neoclassical", "Audiobook", "Audio theatre", "Neue Deutsche Welle", "Podcast", "Indie-Rock ", "G-Funk", "Dubstep", "Garage Rock", "Psybient"};;
        for (int i = 0; i < music.length; i++) {
            values = new ContentValues();
            values.put(TableCategory.COLUMN_NAME_NAME, music[i]);
            Insert(values);
        }

    }

    public static ContentValues getTestValues2(){
        ContentValues values = new ContentValues();

        values.put(TableCategory._ID, 1);
        values.put(TableCategory.COLUMN_NAME_NAME, "Category2");

        return values;
    }

    public Categories(SQLiteDatabase db){
        super(db);
    }

    @Override
    public void Insert(ContentValues values) {
        //TODO check integrity of values
        createdRowId = DB.insert(TABLE_NAME, null, values);
    }

    public void Insert(Category category){
        ContentValues values = new ContentValues();

        values.put(TableCategory.COLUMN_NAME_NAME, category.getName());

        Insert(values);
    }

    public void Insert(List<Category> categories){
        for (Category category : categories) {
            Insert(category);
        }
    }

    @Override
    public ArrayList<IDBClass> Select(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy) {
        //TODO check integrity of parameters
        Cursor cursor = DB.query(TABLE_NAME, columns, whereClause, whereArgs, groupBy, having, orderBy);
        ArrayList<IDBClass> newCategories = new ArrayList<>();
        while (cursor.moveToNext()){
            //region set values
            int id =  cursor.getInt(cursor.getColumnIndexOrThrow(TableCategory._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(TableCategory.COLUMN_NAME_NAME));
            //endregion
            newCategories.add(new Category(id, name));
        }
        categories = newCategories;
        return newCategories;
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
        String selection = TableCategory._ID + " = ?";
        String[] selectionArgs = { id + "" };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            name = cursor.getString(cursor.getColumnIndexOrThrow(TableCategory.COLUMN_NAME_NAME));;
        }

        return name;
    }

    public static int getIdByName(SQLiteDatabase db, String name){
        int id = -1;
        String selection = TableCategory.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { name };
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndexOrThrow(TableCategory._ID));
        }

        return id;
    }

}
