package com.example.projetdintegration.Utilities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;


import com.example.projetdintegration.DBHelpers.Albums;
import com.example.projetdintegration.DBHelpers.Artists;
import com.example.projetdintegration.DBHelpers.Classes.IDBClass;
import com.example.projetdintegration.DBHelpers.Classes.Playlist;
import com.example.projetdintegration.DBHelpers.DBHelper;
import com.example.projetdintegration.DBHelpers.Musics;
import com.example.projetdintegration.DBHelpers.Playlists;
import com.example.projetdintegration.MainActivity;
import com.example.projetdintegration.MusicListActivity;
import com.example.projetdintegration.NavigationManager;
import com.example.projetdintegration.PlaylistListActivity;
import com.example.projetdintegration.R;
import com.example.projetdintegration.DBHelpers.DBHelper.Contract.TablePlaylist;
import com.example.projetdintegration.DBHelpers.Classes.Music;

import java.util.ArrayList;

public class PopupHelper {
    private Context mContext;
    private DBHelper dbHelper;
    private Playlists playlistsWriter;
    private Playlists playlistsReader;
    private Musics researchmusic;
    private String researchContainer;

    public PopupHelper(Context context){
        mContext = context;
        dbHelper = new DBHelper(context);
        playlistsWriter = new Playlists(dbHelper.getWritableDatabase());
        playlistsReader = new Playlists(dbHelper.getReadableDatabase());
    }

    public void showOptionsPopup(View v, int menuRes){
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(menuRes, popup.getMenu());
        popup.show();
    }

    public void showCreateForm(){
        String title = "Créer une liste de lectures";
        String buttonTitle = "Create";

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editPlaylistName = formElementsView.findViewById(R.id.playlistInputName);

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String playlistName = editPlaylistName.getText().toString();
            Playlist playlist = new Playlist(0, playlistName, "normal");
            Playlists playlistsWriter = new Playlists(dbHelper.getWritableDatabase());
            playlistsWriter.Insert(playlist);
            dialog.dismiss();
            PlaylistListActivity.RefreshView(mContext);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
        builder.show();
    }

    public void showEditForm(Playlist playlist){
        String title = "Modifier la liste de lectures";
        String buttonTitle = "Modifier";

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        builder.show();
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
    public void showMusicOptions(View v, Music music){

        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.music_menu, popup.getMenu());



        popup.setOnMenuItemClickListener(item -> {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.playNow:
                    //TODO override the queue and play this music now
                    return true;
                case R.id.playNext:
                    //TODO add to queue as the next music
                    return true;
                case R.id.addToQueue:
                    //TODO add to queue as the last music
                    return true;
                case R.id.addToPlaylist:
                    //TODO find a way to get the playlist ID and had the music to it
                    showAddToPlaylists(v, music);

                    return true;
                case R.id.musicOfArtist:
                    //TODO go to a view of all the music of the same artist (find artist ID)
                    ShowMusicOfArtiste(music);
                    return true;
                case R.id.musicInAlbum:
                    //TODO go to a view of all the music in the same album (find playlist ID)
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

        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase());

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String MusicsName = editSearchMusic.getText().toString();
            researchContainer = MusicsName;

            String[] whereArgs = {"%" + MusicsName + "%" };
            ArrayList<IDBClass> DBMusics = DBMusicsReader.Select(null, DBHelper.Contract.TableMusic.COLUMN_NAME_TITLE + " LIKE ?", whereArgs, null, null, null);

            ArrayList<Music> musics = new ArrayList<>();
            for(IDBClass music:DBMusics) { musics.add((Music)music); }
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

    public void ShowMusicOfArtiste(Music music)
    {
        String title = "rechercher de vos Artiste";
        String buttonTitle = "Rechercher";


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editSearchMusic = formElementsView.findViewById(R.id.playlistInputName);
        editSearchMusic.setText(music.getArtist());

        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase());

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String ArtistsName = editSearchMusic.getText().toString();

            String[] whereArgs = {"%" + ArtistsName + "%" };
            ArrayList<IDBClass> DBMusics = DBMusicsReader.Select(null, DBHelper.Contract.TableMusic.COLUMN_NAME_ID_ARTIST + " LIKE ?", whereArgs, null, null, null);

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

        Musics DBMusicsReader = new Musics(new DBHelper(mContext).getReadableDatabase());

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            String AlbumsName = editSearchMusic.getText().toString();

            String[] whereArgs = {"%" + AlbumsName + "%" };
            ArrayList<IDBClass> DBMusics = DBMusicsReader.Select(null, DBHelper.Contract.TableMusic.COLUMN_NAME_ID_ALBUM + " LIKE ?", whereArgs, null, null, null);

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
