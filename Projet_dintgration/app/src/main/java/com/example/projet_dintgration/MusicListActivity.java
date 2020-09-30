package com.example.projet_dintgration;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_dintgration.DBHelpers.Classes.Music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class MusicListActivity extends AppCompatActivity {
    private static final String TAG = "MusicListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        Log.d(TAG, "onCreate: Started.");

        final ListView listView = (ListView) findViewById(R.id.musicListView);

        Music song1 = new Music(0, "Song1", 125, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song2 = new Music(0, "Song2", 180, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song3 = new Music(0, "Song3", 220, "audio", "./nowhere", "Rock", "Bob", "Yeeters", false);
        Music song41 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song42 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song43 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song44 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song45 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song46 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song47 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song4 = new Music(0, "Song4", 270, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song5 = new Music(0, "Song5", 195, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song6 = new Music(0, "Song6", 145, "audio", "./nowhere", "Rock", "John", "Others", false);
        Music song7 = new Music(0, "Song7", 105, "audio", "./nowhere", "Rock", "John", "Others", false);

        ArrayList<Music> musics = new ArrayList<Music>();

        musics.add(song1);
        musics.add(song2);
        musics.add(song3);
        musics.add(song4);

        musics.add(song41);
        musics.add(song42);
        musics.add(song43);
        musics.add(song44);
        musics.add(song45);
        musics.add(song46);
        musics.add(song47);

        musics.add(song5);
        musics.add(song6);
        musics.add(song7);

        MusicListAdapter adapter = new MusicListAdapter(this, R.layout.listitem_layout, musics);
        listView.setAdapter(adapter);

    }
}
