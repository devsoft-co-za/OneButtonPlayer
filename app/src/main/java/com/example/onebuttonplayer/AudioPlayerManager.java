package com.example.onebuttonplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class AudioPlayerManager {
    private final Context context;
    private final Map<Integer, MediaPlayer> players;
    private boolean isPlaying = false;
    private int currentEventIndex = 0;

    public AudioPlayerManager(Context context) {
        this.context = context;
        this.players = new HashMap<>();
    }

    public void playTrack(int trackIndex, String uri) {
        try {
            // Stop any currently playing track
            stopAll();

            MediaPlayer player = new MediaPlayer();
            
            // Check if we have persistent permission, if not try to take it
            try {
                context.getContentResolver().takePersistableUriPermission(
                    Uri.parse(uri), 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (SecurityException e) {
                Log.w("AudioPlayerManager", "No persistent permission for URI: " + uri);
                // Continue anyway - the file might still be accessible
            }
            
            player.setDataSource(context, Uri.parse(uri));
            player.prepare();
            player.start();

            players.put(trackIndex, player);
            isPlaying = true;

        } catch (Exception e) {
            Log.e("AudioPlayerManager", "Error playing track: " + uri, e);
        }
    }

    public void pauseTrack(int trackIndex) {
        MediaPlayer player = players.get(trackIndex);
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void resumeTrack(int trackIndex) {
        MediaPlayer player = players.get(trackIndex);
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    public void stopTrack(int trackIndex) {
        MediaPlayer player = players.get(trackIndex);
        if (player != null) {
            try {
                player.stop();
            } catch (IllegalStateException e) {
                Log.w("AudioPlayerManager", "Player already stopped: " + e.getMessage());
            }
            player.release();
            players.remove(trackIndex);
        }
    }

    public void pauseAll() {
        for (MediaPlayer player : players.values()) {
            if (player != null && player.isPlaying()) {
                player.pause();
            }
        }
        isPlaying = false;
    }

    public void stopAll() {
        for (MediaPlayer player : players.values()) {
            try {
                player.stop();
            } catch (IllegalStateException e) {
                Log.w("AudioPlayerManager", "Player already stopped: " + e.getMessage());
            }
            player.release();
        }
        players.clear();
        isPlaying = false;
        currentEventIndex = 0;
    }

    public void cleanup() {
        stopAll();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentEventIndex() {
        return currentEventIndex;
    }

    public void setCurrentEventIndex(int index) {
        currentEventIndex = index;
    }
}
