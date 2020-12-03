package com.example.projetdintegration.Utilities;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.MediaPlaybackService;
import com.example.projetdintegration.MusicListActivity;
import com.example.projetdintegration.PlaylistListActivity;
import com.example.projetdintegration.R;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TablePlaylist;
import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.util.ArrayList;

public class PopupHelper {
    private static final String TAG = "PopupHelper";
    private Context mContext;
    private DBHelper dbHelper;
    private Playlists playlistsWriter;
    private Playlists playlistsReader;
    private Musics researchmusic;
    private String researchContainer;

    public PopupHelper(Context context){
        mContext = context;
        dbHelper = new DBHelper(context);
        playlistsWriter = new Playlists(dbHelper.getWritableDatabase(), context);
        playlistsReader = new Playlists(dbHelper.getReadableDatabase(), context);
    }

    public void showOptionsPopup(View v, int menuRes){
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(menuRes, popup.getMenu());
        popup.show();
    }

    public static class PlaylistDialog extends DialogFragment{
        Context mContext;
        Playlist playlist;
        ArrayList<Playlist> playlists;
        Playlists playlistsWriter;
        Playlists playlistsReader;
        public PlaylistDialog(Context context, Playlist playlist){
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

        PlaylistDialog dialog = new PlaylistDialog(mContext, null);
        dialog.show(fm, "PlaylistCreateDialog");

        /*Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String playlistName = editPlaylistName.getText().toString();
                if (!playlistName.isEmpty()) {
                    Playlist playlist = new Playlist(0, playlistName, "normal");
                    Playlists playlistsWriter = new Playlists(dbHelper.getWritableDatabase(), mContext);
                    playlistsWriter.Insert(playlist);
                    PlaylistListActivity.RefreshView(mContext);
                    dialogInterface.dismiss();
                }

            }});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
        builder.show();*/
    }

    public void showEditForm(Playlist playlist){
        FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();

        PlaylistDialog dialog = new PlaylistDialog(mContext, playlist);
        dialog.show(fm, "PlaylistEditDialog");

        /*LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editPlaylistName = formElementsView.findViewById(R.id.playlistInputName);
        editPlaylistName.setText(playlist.getName());

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String playlistName = editPlaylistName.getText().toString();
            ContentValues values = new ContentValues();
            values.put(TablePlaylist.COLUMN_NAME_NAME, playlistName);
            String whereClause = TablePlaylist._ID + " = ?";
            String[] whereArgs = { playlist.getId() + "" };
            playlistsWriter.Update(values, whereClause, whereArgs);
            dialog.dismiss();
            if (mContext.getClass() == MusicListActivity.class){
                final TextView pageTitle = (TextView) ((MusicListActivity) mContext).findViewById(R.id.PageTitle);
                pageTitle.setText(playlistName);
            }
            //PlaylistListActivity.RefreshView();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
        builder.show();*/
    }


    public void showDeleteForm(int playlistId){
        String title = "Êtes-vous sure de vouloir supprimer la liste de lectures";
        String posButtonTitle = "Supprimer";
        String negButtonTitle = "Annuler";

        Builder builder = new Builder(mContext);
        builder.setTitle(title);
        builder.setPositiveButton(posButtonTitle, (dialog, i) -> {
            String whereClause = TablePlaylist._ID + " = ?";
            String[] whereArgs = { playlistId + "" };
            playlistsWriter.Delete(whereClause, whereArgs);
            //PlaylistListActivity.RefreshView();
            mContext.startActivity(new Intent(mContext, PlaylistListActivity.class));
            dialog.dismiss();
        });
        builder.setNegativeButton(negButtonTitle, (dialog, i) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    //TODO finish the back-end options
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
                    binder.getService().UpdateMusicList(musics, position);
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
                    ShowMusicOfArtiste(music);
                    return true;
                case R.id.musicInAlbum:
                    ShowmusicInAlbum(music);
                    return true;
                /*case R.id.addToSpotifyFav:
                    return true;
                case R.id.addToSpotifyPlaylist:
                    return true;*/
                default:
                    return false;
            }

        });

        popup.show();
    }

    public void showPlaylistOptions(View v, Playlist playlist){

        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, popup.getMenu());

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
            if(item.isChecked())
                playlistsWriter.RemoveFromPlaylist(musicToAdd.getId(), item.getItemId());
            else
                playlistsWriter.AddToPlaylist(musicToAdd.getId(), item.getItemId());
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
    public void showSearchForm(){
        String title = "rechercher de vos chanson";
        String buttonTitle = "Rechercher";


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editSearchMusic = formElementsView.findViewById(R.id.playlistInputName);
        editSearchMusic.setText(researchContainer);

        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase(), mContext);

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String MusicsName = editSearchMusic.getText().toString();
            researchContainer = MusicsName;

            String[] whereArgs = {"%" + MusicsName + "%" };
            DBMusicsReader.Select(null, DBHelper.Contract.TableMusic.COLUMN_NAME_TITLE + " LIKE ?", whereArgs, null, null, null);
            MusicListActivity.RefreshView(mContext);
            dialog.dismiss();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
        builder.show();

    }

    public void ShowMusicOfArtiste(Music music)
    {
        String title = "rechercher de vos Artiste";
        String buttonTitle = "Rechercher";


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editSearchMusic = formElementsView.findViewById(R.id.playlistInputName);
        editSearchMusic.setText(music.getArtist());

        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase(),mContext);

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String ArtistsName = editSearchMusic.getText().toString();

            String[] whereArgs = {"%" + ArtistsName + "%" };
            ArrayList<IDBClass> DBMusics = DBMusicsReader.Select(null, MediaStore.Audio.Media.ARTIST + " LIKE ?", whereArgs, null, null, null);

            ArrayList<Music> musics = new ArrayList<>();
            for(IDBClass artist:DBMusics) { musics.add((Music)artist); }
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

    public void ShowmusicInAlbum(Music music)
    {
        String title = "rechercher de vos Album";
        String buttonTitle = "Rechercher";


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editSearchMusic = formElementsView.findViewById(R.id.playlistInputName);
        editSearchMusic.setText(music.getAlbum());

        Musics DBMusicsReader = new Musics(dbHelper.getReadableDatabase(),mContext);

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String AlbumsName = editSearchMusic.getText().toString();

            String[] whereArgs = {AlbumsName};
            ArrayList<IDBClass> DBMusics = DBMusicsReader.Select(null, MediaStore.Audio.Media.ALBUM + " LIKE ?", whereArgs, null, null, null);

            ArrayList<Music> musics = new ArrayList<>();
            for(IDBClass album:DBMusics) { musics.add((Music)album); }
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

}
