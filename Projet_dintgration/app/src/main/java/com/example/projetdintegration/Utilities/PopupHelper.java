package com.example.projetdintegration.Utilities;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.DBInitializer;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.MediaPlaybackService;
import com.example.projetdintegration.LierSpotifyActivity;
import com.example.projetdintegration.MainActivity;
import com.example.projetdintegration.MusicListActivity;
import com.example.projetdintegration.PlaylistListActivity;
import com.example.projetdintegration.R;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TablePlaylist;
import com.example.projetdintegration.DBHelpers.Classes.Music;
import com.example.projetdintegration.SpotifyMusicListActivity;
import com.example.projetdintegration.Utilities.SpotifyLibraryManager;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.LibraryState;

import java.util.ArrayList;

public class PopupHelper {
    private static final String TAG = "PopupHelper";
    private Context mContext;
    private DBHelper dbHelper;
    private Playlists playlistsWriter;
    private Playlists playlistsReader;
    private String researchContainer;

    public PopupHelper(Context context) {
        mContext = context;
        dbHelper = new DBHelper(context);
        playlistsWriter = new Playlists(dbHelper.getWritableDatabase(), context);
        playlistsReader = new Playlists(dbHelper.getReadableDatabase(), context);
    }

    public void showOptionsPopup(View v, int menuRes) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(menuRes, popup.getMenu());
        popup.show();
    }

    public static class PlaylistMakeRelativeDialog extends DialogFragment{
        Context mContext;
        Playlist playlist;
        ArrayList<Playlist> playlists;
        Playlists playlistsWriter;
        Playlists playlistsReader;
        public PlaylistMakeRelativeDialog(Context context, Playlist playlist){
            super();
            mContext = context;
            DBHelper dbHelper = new DBHelper(mContext);
            playlistsWriter = new Playlists(dbHelper.getWritableDatabase(), mContext);
            playlistsReader = new Playlists(dbHelper.getReadableDatabase(), mContext);

            ArrayList<IDBClass> dbPlaylists = playlistsReader.Select(null, null, null, null, null, null);
            playlists = new ArrayList<>();
            for(IDBClass pl : dbPlaylists){
                playlists.add((Playlist)pl);
            }

            this.playlist = playlist;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String title = "Rendre relative sur: ";
            String buttonTitle = "OK";

            // inflate the custom dialog layout
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.make_relative_form_layout, null);

            final CheckBox artistCB = view.findViewById(R.id.relativeToArtist);
            final CheckBox albumCB = view.findViewById(R.id.relativeToAlbum);
            final CheckBox categoryCB = view.findViewById(R.id.relativeToCategory);

            artistCB.setOnClickListener(view1 -> {
                AlertDialog dialog = (AlertDialog) getDialog();
                if(dialog != null)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(artistCB.isChecked() || albumCB.isChecked() || categoryCB.isChecked());
            });

            albumCB.setOnClickListener(view1 -> {
                AlertDialog dialog = (AlertDialog) getDialog();
                if(dialog != null)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(artistCB.isChecked() || albumCB.isChecked() || categoryCB.isChecked());
            });

            categoryCB.setOnClickListener(view1 -> {
                AlertDialog dialog = (AlertDialog) getDialog();
                if(dialog != null)
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(artistCB.isChecked() || albumCB.isChecked() || categoryCB.isChecked());
            });


            // build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);
            if (playlist != null){
                builder.setPositiveButton(buttonTitle, (dialog, i) -> {
                    ContentValues values = new ContentValues();
                    String relativeToArtist = (artistCB.isChecked())? "/" + DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST : "";
                    String relativeToAlbum = (albumCB.isChecked())? "/" + DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM : "";
                    String relativeToCategory = (categoryCB.isChecked())? "/" + DBHelper.Contract.TableMusic.COLUMN_NAME_CATEGORY : "";
                    String relativeType = TablePlaylist.RELATIVE_TYPE + relativeToArtist + relativeToAlbum + relativeToCategory;
                    values.put(TablePlaylist.COLUMN_NAME_TYPE, relativeType);
                    String whereClause = TablePlaylist._ID + " = ?";
                    String[] whereArgs = { playlist.getId() + "" };
                    playlistsWriter.Update(values, whereClause, whereArgs);
                    playlist.setType(relativeType);

                    new DBInitializer(mContext).FillRelativePlaylist(playlist.getId());

                    PlaylistListActivity.RefreshView(mContext);
                    ArrayList<Music> musics = new ArrayList<>();
                    for (IDBClass music : playlistsReader.getAllMusicsInPlaylist(playlist.getId())){
                        musics.add((Music)music);
                    }
                    MusicListActivity.RefreshViewFromList(mContext, musics);
                    dialog.dismiss();
                });



            }
            builder.setTitle(title);
            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();

            // disable positive button by default
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public static class PlaylistInputDialog extends DialogFragment{
        Context mContext;
        Playlist playlist;
        ArrayList<Playlist> playlists;
        Playlists playlistsWriter;
        Playlists playlistsReader;
        public PlaylistInputDialog(Context context, Playlist playlist){
            super();
            mContext = context;
            DBHelper dbHelper = new DBHelper(mContext);
            playlistsWriter = new Playlists(dbHelper.getWritableDatabase(), mContext);
            playlistsReader = new Playlists(dbHelper.getReadableDatabase(), mContext);

            ArrayList<IDBClass> dbPlaylists = playlistsReader.Select(null, null, null, null, null, null);
            playlists = new ArrayList<>();
            for(IDBClass pl : dbPlaylists){
                playlists.add((Playlist)pl);
            }

            this.playlist = playlist;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String title = "Créer une liste de lectures";
            String buttonTitle = "Créer";

            // inflate the custom dialog layout
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.playlist_input_form_layout, null);

            final EditText editPlaylistName = view.findViewById(R.id.playlistInputName);


            editPlaylistName.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if(dialog != null){
                        String playlistName = editPlaylistName.getText().toString();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!playlistName.isEmpty() && !playlists.contains(new Playlist(0, playlistName, "")));
                    }
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if(dialog != null){
                        String playlistName = editPlaylistName.getText().toString();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!playlistName.isEmpty() && !playlists.contains(new Playlist(0, playlistName, "")));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if(dialog != null){
                        String playlistName = editPlaylistName.getText().toString();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!playlistName.isEmpty() && !playlists.contains(new Playlist(0, playlistName, "")));
                    }
                }
            });

            // build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);
            if (playlist != null){
                title = "Modifier la liste de lectures";
                buttonTitle = "Modifier";
                editPlaylistName.setText(playlist.getName());
                builder.setPositiveButton(buttonTitle, (dialog, i) -> {
                    String playlistName = editPlaylistName.getText().toString();
                    ContentValues values = new ContentValues();
                    values.put(TablePlaylist.COLUMN_NAME_NAME, playlistName);
                    String whereClause = TablePlaylist._ID + " = ?";
                    String[] whereArgs = { playlist.getId() + "" };
                    playlistsWriter.Update(values, whereClause, whereArgs);

                    ArrayList<IDBClass> dbPlaylists = playlistsReader.Select(null, null, null, null, null, null);
                    playlists = new ArrayList<>();
                    for(IDBClass pl : dbPlaylists){
                        playlists.add((Playlist)pl);
                    }

                    dialog.dismiss();
                    if (mContext.getClass() == MusicListActivity.class){
                        final TextView pageTitle = (TextView) ((MusicListActivity) mContext).findViewById(R.id.PageTitle);
                        pageTitle.setText(playlistName);
                    }});
                PlaylistListActivity.RefreshView(mContext);

            }
            else{
                builder.setPositiveButton(buttonTitle, (dialog, id) -> {
                    String playlistName = editPlaylistName.getText().toString();
                    Playlist playlist = new Playlist(0, playlistName, "normal");

                    playlistsWriter.Insert(playlist);
                    PlaylistListActivity.RefreshView(mContext);
                    playlists.add(playlist);

                    dialog.dismiss();
                });
            }
            builder.setTitle(title);
            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();

            // disable positive button by default
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public void showCreateForm(){
        FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();

        PlaylistInputDialog dialog = new PlaylistInputDialog(mContext, null);
        dialog.show(fm, "PlaylistCreateDialog");
    }

    public void showEditForm(Playlist playlist){
        FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();

        PlaylistInputDialog dialog = new PlaylistInputDialog(mContext, playlist);
        dialog.show(fm, "PlaylistEditDialog");
    }

    public void showDeleteForm(int playlistId) {
        String title = "Êtes-vous sure de vouloir supprimer la liste de lectures";
        String posButtonTitle = "Supprimer";
        String negButtonTitle = "Annuler";

        Builder builder = new Builder(mContext);
        builder.setTitle(title);
        builder.setPositiveButton(posButtonTitle, (dialog, i) -> {
            String whereClause = TablePlaylist._ID + " = ?";
            String[] whereArgs = {playlistId + ""};
            playlistsWriter.Delete(whereClause, whereArgs);
            PlaylistListActivity.RefreshView(mContext);
            mContext.startActivity(new Intent(mContext, PlaylistListActivity.class));
            dialog.dismiss();
        });
        builder.setNegativeButton(negButtonTitle, (dialog, i) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    public void showMakeRelativeForm(Playlist playlist){
        FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();

        PlaylistMakeRelativeDialog dialog = new PlaylistMakeRelativeDialog(mContext, playlist);
        dialog.show(fm, "PlaylistMakeRelativeDialog");
    }

    public void removeRelativeType(Playlist playlist){
        ContentValues values = new ContentValues();
        values.put(TablePlaylist.COLUMN_NAME_TYPE, TablePlaylist.NORMAL_TYPE);
        playlist.setType(TablePlaylist.NORMAL_TYPE);
        String whereClause = TablePlaylist._ID + " = ?";
        String[] whereArgs = { playlist.getId() + "" };
        playlistsWriter.Update(values, whereClause, whereArgs);
        PlaylistListActivity.RefreshView(mContext);
        ArrayList<Music> musics = new ArrayList<>();
        for (IDBClass music : playlistsReader.getAllMusicsInPlaylist(playlist.getId())){
            musics.add((Music)music);
        }
        MusicListActivity.RefreshViewFromList(mContext, musics);
    }

    //TODO finish the back-end options
    public void showMusicOptions(View v, Music music, CallResult.ResultCallback<Empty> callbackRemove, CallResult.ResultCallback<Empty> callbackAdd, int position, MediaPlaybackService.LocalBinder binder, boolean... isAdded){
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.spotify_music_menu, popup.getMenu());
        Menu menu = popup.getMenu();
        menu.findItem(R.id.musicOfArtist).setVisible(false);
        menu.findItem(R.id.musicInAlbum).setVisible(false);
        if(isAdded != null && isAdded.length == 1){
            MenuItem item = menu.findItem(R.id.addToSpotifyPlaylist);
            item.setVisible(true);
            if(isAdded[0])
                item.setTitle(R.string.add_to_spotify_list);
            else
                item.setTitle(R.string.remove_from_spotify_list);
        }else
            menu.findItem(R.id.addToSpotifyPlaylist).setVisible(false);
        popup.setOnMenuItemClickListener(item -> {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.playNow:
                    binder.getService().PlayNow(SpotifyMusicListActivity.navigationList.musics, position);
                    //TODO override the queue and play this music now
                    return true;
                case R.id.playNext:
                    binder.getService().AddNext(music);
                    return true;
                case R.id.addToQueue:
                    binder.getService().Add(music);
                    return true;
                case R.id.addToPlaylist:
                    showAddToPlaylists(v, music);
                    return true;
                case R.id.musicOfArtist:
                    //ShowMusicOfArtiste(music);
                    return true;
                case R.id.musicInAlbum:
                    //ShowmusicInAlbum(music);
                    return true;
                case R.id.addToSpotifyPlaylist:
                    addToSpotifyLibrary(v,music,callbackRemove,callbackAdd);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }
    public void showMusicOptions(View v, Music music, int position, MediaPlaybackService.LocalBinder binder){

        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.music_menu, popup.getMenu());

        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase(), mContext);

        ArrayList<Music> musics = DBMusicsReader.LastSelect();
        Log.d(TAG, "showMusicOptions: size = " + musics.size());

        popup.setOnMenuItemClickListener(item -> {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.playNow:
                    binder.getService().PlayNow(musics, position);
                    //TODO override the queue and play this music now
                    return true;
                case R.id.playNext:
                    binder.getService().AddNext(music);
                    return true;
                case R.id.addToQueue:
                    binder.getService().Add(music);
                    return true;
                case R.id.addToPlaylist:
                    showAddToPlaylists(v, music);
                    return true;
                case R.id.musicOfArtist:
                    searchMusicsOfArtist(music);
                    return true;
                case R.id.musicInAlbum:
                    searchMusicsInAlbum(music);
                    return true;
                default:
                    return false;
            }

        });

        popup.show();
    }

    private void addToSpotifyLibrary(View v, Music music, CallResult.ResultCallback<Empty> callbackRemove, CallResult.ResultCallback<Empty> callbackAdd) {
        SpotifyLibraryManager libraryManager = new SpotifyLibraryManager(LierSpotifyActivity.appRemote.getUserApi());
        libraryManager.getLibraryState(music.getPath(), new CallResult.ResultCallback<LibraryState>() {
            @Override
            public void onResult(LibraryState libraryState) {
                if(libraryState.canAdd)
                    if(libraryState.isAdded)
                        libraryManager.removeFromLibrary(music.getPath(),callbackRemove);
                    else
                        libraryManager.addToLibrary(music.getPath(), callbackAdd);
            }
        });
    }

    public void showPlaylistOptions(View v, Playlist playlist) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, popup.getMenu());
        popup.getMenu().getItem(2).setChecked(playlist.getType().contains(TablePlaylist.RELATIVE_TYPE));

        Log.d(TAG, "showPlaylistOptions: " + playlist.getType());
        Log.d(TAG, "showPlaylistOptions: " +  popup.getMenu().getItem(2).isChecked());

        popup.setOnMenuItemClickListener(item -> {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.editPlaylistName:
                    //TODO edit the name of the playlist
                    showEditForm(playlist);
                    return true;
                case R.id.deletePlaylist:
                    //TODO override the queue and play this music now
                    showDeleteForm(playlist.getId());
                    return true;
                case R.id.makeRelative:
                    //TODO add to queue as the next music
                    if(!item.isChecked()){
                        showMakeRelativeForm(playlist);
                    }
                    else{
                        removeRelativeType(playlist);
                    }
                    return true;
                default:
                    return false;
            }

        });
        popup.show();
    }

    public void showAddToPlaylists(View v, Music musicToAdd){
        int orderCount = Menu.FIRST;
        PopupMenu popup = new PopupMenu(mContext, v);
        Menu menu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();

        /*TODO:
           - Get all playlists
           - Create a checkable menu option for each playlist
           - Set onClick of a Playlist to add or remove the music from selected Playlist
        *  */
        ArrayList<IDBClass> dbPlaylists = playlistsReader.Select(new String[]{TablePlaylist._ID, TablePlaylist.COLUMN_NAME_NAME, TablePlaylist.COLUMN_NAME_TYPE}, null, null, null, null, null);
        for (IDBClass playlist : dbPlaylists) {
            MenuItem item = menu.add(R.id.checkablePlaylistsGroup, playlist.getId(), orderCount++, playlist.getName());
            item.setCheckable(true);
            item.setChecked(playlistsReader.isInPlaylist(musicToAdd.getId(), playlist.getId()));
        }
        inflater.inflate(R.menu.add_to_playlist_menu, menu);

        popup.setOnMenuItemClickListener(item -> {
            Playlist playlist = Playlists.getPlaylistById(dbHelper.getReadableDatabase(), item.getItemId());
            if(item.isChecked())
                playlistsWriter.RemoveFromPlaylist(musicToAdd.getId(), item.getItemId());
            else{
                playlistsWriter.AddToPlaylist(musicToAdd.getId(), item.getItemId());
                if(playlist.getType().contains(TablePlaylist.RELATIVE_TYPE)){
                    new DBInitializer(mContext).FillRelativePlaylist(playlist.getId());
                }
            }
            item.setChecked(!item.isChecked());
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            item.setActionView(new View(mContext));
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return false;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return false;
                }
            });
            MusicListActivity.RefreshView(mContext);
            return false;
        });

        popup.show();
    }

    //TODO : Take the user input to after give it to the musiclistActivity
    public void showSearchForm() {
        String title = "rechercher de vos chanson";
        String buttonTitle = "Rechercher";


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editSearchMusic = formElementsView.findViewById(R.id.playlistInputName);
        editSearchMusic.setText(researchContainer);


        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase(), mContext);
        //Artists DBArtistesReader = new Artists(new DBHelper(mContext).getReadableDatabase());
        //Albums DBAlbumsReader = new Albums(new DBHelper(mContext).getReadableDatabase());

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String input = editSearchMusic.getText().toString();
            researchContainer = input;

            String[] whereArgs = {"%" + input + "%", "%" + input + "%", "%" + input + "%"};

            /*ArrayList<IDBClass> DBAlbums = DBAlbumsReader.Select(null, DBHelper.Contract.TableAlbum.COLUMN_NAME_TITLE + " LIKE ?", whereArgs, null, null, null);
            //ArrayList<IDBClass> DBArtists = DBArtistesReader.Select(null, DBHelper.Contract.TableArtist.COLUMN_NAME_NAME + " LIKE ?", whereArgs, null, null, null);
            ArrayList<Integer> IDS_Artist = new ArrayList<Integer>();
            for (IDBClass artist:DBArtists){
                IDS_Artist.add(artist.getId());
            }
            ArrayList<Integer> IDS_Album = new ArrayList<Integer>();
            for (IDBClass album : DBAlbums) {
                IDS_Album.add(album.getId());
            }*/

            String WhereClause = DBHelper.Contract.TableMusic.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST + " LIKE ? OR " +
                    DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM + " LIKE ?";

            ArrayList<IDBClass> DBMusics = DBMusicsReader.SavedSelect(null, WhereClause, whereArgs, null, null, null);


            ArrayList<Music> musics = new ArrayList<>();
            for (IDBClass music : DBMusics) {
                musics.add((Music) music);
            }
            MusicListActivity.RefreshViewFromList(mContext, musics);
            dialog.dismiss();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
        builder.show();

    }
    
    public void searchMusicsOfArtist(Music music)
    {
        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase(), mContext);
        String[] whereArgs = {"%" + music.getArtist() + "%" };
        String WhereClause = DBHelper.Contract.TableMusic.COLUMN_NAME_ARTIST + " LIKE ?";
        ArrayList<IDBClass> DBArtists = DBMusicsReader.SavedSelect(null, WhereClause, whereArgs, null, null, null);
        ArrayList<Music> musics = new ArrayList<>();
        for (IDBClass artist : DBArtists) {
            musics.add((Music) artist);
        }
        MusicListActivity.RefreshViewFromList(mContext, musics);
    }

    public void searchMusicsInAlbum(Music music)
    {
        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase(), mContext);
        String[] whereArgs = {"%" + music.getAlbum() + "%" };
        String WhereClause = DBHelper.Contract.TableMusic.COLUMN_NAME_ALBUM + " LIKE ?";
        ArrayList<IDBClass> DBAlbums = DBMusicsReader.SavedSelect(null, WhereClause, whereArgs, null, null, null);
        ArrayList<Music> musics = new ArrayList<>();
        for (IDBClass album : DBAlbums) {
            musics.add((Music) album);
        }
        MusicListActivity.RefreshViewFromList(mContext, musics);

    }


}
