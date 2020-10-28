package com.example.projetdintegration.Utilities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.example.projetdintegration.R;

public class PopupHelper {
    private Context mContext;

    public PopupHelper(Context context){
        mContext = context;
    }

    public void showPopup(View v, int menuRes){
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
            //TODO Create a playlist

            dialog.dismiss();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
    }

    public void showEditForm(){
        String title = "Modifier la liste de lectures";
        String buttonTitle = "Edit";

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.playlist_input_form_layout, null, false);
        final EditText editPlaylistName = formElementsView.findViewById(R.id.playlistInputName);

        Builder builder = new Builder(mContext);
        builder.setView(formElementsView);
        builder.setTitle(title);
        builder.setPositiveButton(buttonTitle, (dialog, i) -> {
            //TODO Edit a playlist

            dialog.dismiss();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(dialog -> {
                //TODO save data somewhere
            });
        }
    }

    //TODO: show confirmation of action
    public void showDeleteForm(){}

    //TODO
    public void showMusicOptions(){}
}
