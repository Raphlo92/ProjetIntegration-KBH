package com.example.projetdintegration.Utilities;

import android.util.Log;

import com.spotify.android.appremote.api.UserApi;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.LibraryState;

public class SpotifyLibraryManager{
    private static final String TAG = "SPOTIFY_LIBRARY_MANAGER";
    UserApi userApi;
    public SpotifyLibraryManager(UserApi api){
        userApi = api;
    }

    public void addToLibrary(String uri, CallResult.ResultCallback<Empty> callResult){
        userApi.addToLibrary(uri).setResultCallback(callResult);
    }
    public void removeFromLibrary(String uri,CallResult.ResultCallback<Empty> callback){
        userApi.removeFromLibrary(uri).setResultCallback(callback);
    }
    public void getLibraryState(String uri, CallResult.ResultCallback<LibraryState> callback){
        userApi.getLibraryState(uri).setResultCallback(callback);
    }

    public static CallResult.ResultCallback<LibraryState> getBaseLibraryStateResult(){ //Peut être l'enlever ou le modifier complètement le onResult
        return new CallResult.ResultCallback<LibraryState>() {
            @Override
            public void onResult(LibraryState libraryState) {
                Log.i(TAG, libraryState.canAdd + " " + libraryState.isAdded + " for uri: " + libraryState.uri);
            }
        };
    }
}
