package com.example.projetdintegration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

public class FullScreenMediaController extends MediaController {

    private ImageButton fullScreenButton;
    private String isFullScreen;

    public FullScreenMediaController(Context context){
        super(context);
    }

    @Override
    public void setAnchorView(View view){
        super.setAnchorView(view);

        fullScreenButton = new ImageButton(super.getContext());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        params.rightMargin = 80;
        addView(fullScreenButton, params);

        if("y".equals(isFullScreen)){
            fullScreenButton.setImageResource(R.drawable.ic_baseline_fullscreen_exit_24);
        }
        else{
            fullScreenButton.setImageResource(R.drawable.ic_baseline_fullscreen_24);
        }

        fullScreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MediaActivity.class);

                if("y".equals(isFullScreen)){
                    intent.putExtra("fullScreenInd", "");
                }
                else
                {
                    intent.putExtra("fullScreenInd", "y");
                }
                ((Activity)getContext()).startActivity(intent);
            }
        });
    }



}
