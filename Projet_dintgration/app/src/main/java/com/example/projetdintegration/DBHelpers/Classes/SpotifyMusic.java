package com.example.projetdintegration.DBHelpers.Classes;

import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;

public class SpotifyMusic extends Music {
    ImageUri imageUri;
    String spotifyId;
    String subtitle;
    boolean playable;
    boolean hasChildren;
    public SpotifyMusic(String name, String path, boolean favorite, boolean playable, boolean hasChildren, String spotifyId, ImageUri image, String subtitle,String itemType) {
        super(-1, name, 0, "Spotify", path, "",itemType , "", favorite);
        imageUri = image;
        this.spotifyId = spotifyId;
        this.playable = playable;
        this.hasChildren = hasChildren;
        this.subtitle = subtitle;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public boolean isPlayable() {
        return playable;
    }

    public ImageUri getImageUri() {
        return imageUri;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public ListItem transformToListItem(){
        return new ListItem(spotifyId,getPath(),imageUri,getName(),subtitle,playable,hasChildren);
    }
}
