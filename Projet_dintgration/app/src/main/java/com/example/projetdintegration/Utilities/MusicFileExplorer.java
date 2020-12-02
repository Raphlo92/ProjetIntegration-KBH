package com.example.projetdintegration.Utilities;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class MusicFileExplorer {
    //PATH TO MUSICS : /storage/emulated/0/Music/{Artist}/{Album}/{Music}
    //PATH TO ALBUM PROPS : /storage/emulated/0/Music/{Artist}/{Album}/{PropFile}
    //FILES TO IGNORE : files starting with '.'

    //FileOutputStream
    //FileInputStream
    //File

    static ArrayList<File> files = new ArrayList<>();
    //public static final String DIRECTORY_MUSIC = "src/main/res/raw";//"C:/Users/Kevin/Music";//"/storage/emulated/0/Music";
    public static final String DIRECTORY_MUSIC = "/storage/emulated/0/Music";
    private static final String TAG = "MusicFileExplorer";
    private MusicFileExplorer() { }

    public static String[] getAllArtistsDirectory() {
        return new File(DIRECTORY_MUSIC).list();
    }

    public static String[] getAlbumsDirectory(String artist) {
        return new File(DIRECTORY_MUSIC + "/" + artist).list();
    }

    public static String[] getAlbumFiles(String albumPath) {
        return new File(DIRECTORY_MUSIC + "/" + albumPath).list();
    }

    public static ArrayList<File> getAllMusicFiles(){
        //PATH TO MUSICS : /storage/emulated/0/Music/{Artist}/{Album}/{Music}
        ArrayList<File> files = new ArrayList<>();

        String[] artists = getAllArtistsDirectory();
        if (artists != null){
            for (String artist: artists) {
                files.add(new File(DIRECTORY_MUSIC + "/" + artist));
                String[] artistAlbums = getAlbumsDirectory(artist);
                if (artistAlbums != null){
                    for (String album: artistAlbums) {
                        files.add(new File(DIRECTORY_MUSIC + "/" + artist + "/" + album));
                        String[] albumFiles = getAlbumFiles(artist + "/" + album);
                        if (albumFiles != null){
                            for (String file: albumFiles) {
                                files.add(new File(DIRECTORY_MUSIC + "/" + artist + "/" + album + "/" + file));
                            }
                        }
                    }
                }
            }
        }

        return files;
    }

    public static void getAllNewestChildren(String path, ArrayList<File> files, Date lastSearch){
        Log.d(TAG, "getAllChildren: Started");
        File file = new File(path);
        String[] children = file.list();
        //ArrayList<File> files = new ArrayList<>();

        if(children != null){
            for (String child: children) {
                File childFile = new File(file.getAbsolutePath() + "/" + child);
                Log.d(TAG, "getAllChildren: childPath = " + file.getAbsolutePath() + "/" + child);

//                if(lastSearch.before(new Date(file.lastModified()))){
//                    files.add(childFile);
//                    if(childFile.list() != null)
//                        getAllNewestChildren(file.getAbsolutePath() + "/" + child, files, lastSearch);
//                }
            }
        }

    }

    public static void getAllChildren(String path, ArrayList<File> files){
        Log.d(TAG, "getAllChildren: Started");
        File file = new File(path);

        String[] children = file.list(); //null
        //ArrayList<File> files = new ArrayList<>();

        if(children != null){
            for (String child: children) {
                File childFile = new File(file.getAbsolutePath() + "/" + child);
                Log.d(TAG, "getAllChildren: childPath = " + file.getAbsolutePath() + "/" + child);

                files.add(childFile);
                if(childFile.list() != null)
                    getAllChildren(file.getAbsolutePath() + "/" + child, files);
            }
        }

    }

    public static String getMimeType(File file){
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getName().replaceAll(" ", ""));

        if (extension.equals("") && file.getName().contains(new StringBuffer().append(".").subSequence(0, 1))){
            String[] splitFileName = file.getName().split(".");
            if (splitFileName.length > 0){
                extension = splitFileName[1];
            }

        }

        Log.d(TAG, "getMimeType: extension = "+ extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if(extension != null){
            if(mimeType != null){
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            return "unsupported";
        }
        return null;
    }

    public static File getFile(String path) {
        return new File(path);
    }
}
