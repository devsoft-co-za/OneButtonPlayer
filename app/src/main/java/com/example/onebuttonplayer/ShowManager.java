package com.example.onebuttonplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShowManager {
    private static final String SHARED_PREFS_NAME = "OneButtonPlayerPrefs";
    private static final String KEY_SHOWS = "saved_shows";
    private static final String KEY_LAST_SHOW = "last_show_id";

    private final SharedPreferences prefs;
    private final List<Show> shows;
    private Show currentShow;

    public ShowManager(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        shows = new ArrayList<>();
        loadShows();
    }

    public void saveShow(Show show) {
        // Remove existing show with same name
        shows.removeIf(s -> s.getName().equals(show.getName()));
        shows.add(show);
        saveShows();
    }

    public void deleteShow(String showId) {
        shows.removeIf(show -> show.getId().equals(showId));
        saveShows();
    }

    public List<Show> getShows() {
        return new ArrayList<>(shows);
    }

    public Show getCurrentShow() {
        return currentShow;
    }

    public void setCurrentShow(Show show) {
        currentShow = show;
        if (show != null) {
            prefs.edit().putString(KEY_LAST_SHOW, show.getId()).apply();
        }
    }

    public void loadLastShow() {
        String lastShowId = prefs.getString(KEY_LAST_SHOW, null);
        if (lastShowId != null) {
            for (Show show : shows) {
                if (show.getId().equals(lastShowId)) {
                    currentShow = show;
                    break;
                }
            }
        }
    }

    private void loadShows() {
        String showsJson = prefs.getString(KEY_SHOWS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(showsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonShow = jsonArray.getJSONObject(i);
                Show show = new Show(
                    jsonShow.getString("id"),
                    jsonShow.getString("name")
                );

                // Load files
                if (jsonShow.has("files")) {
                    JSONArray filesArray = jsonShow.getJSONArray("files");
                    for (int j = 0; j < filesArray.length(); j++) {
                        JSONObject jsonFile = filesArray.getJSONObject(j);
                        ShowFile file = new ShowFile(
                            jsonFile.getString("name"),
                            jsonFile.getString("uri"),
                            jsonFile.getInt("index")
                        );
                        show.getFiles().add(file);
                    }
                }

                // Load events
                if (jsonShow.has("events")) {
                    JSONArray eventsArray = jsonShow.getJSONArray("events");
                    for (int j = 0; j < eventsArray.length(); j++) {
                        JSONObject jsonEvent = eventsArray.getJSONObject(j);
                        ShowEvent event = new ShowEvent(
                            jsonEvent.getString("type"),
                            jsonEvent.getInt("trackIndex"),
                            jsonEvent.getString("trackName")
                        );
                        show.getEvents().add(event);
                    }
                }

                shows.add(show);
            }
        } catch (Exception e) {
            Log.e("ShowManager", "Error loading shows", e);
        }
    }

    private void saveShows() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Show show : shows) {
                JSONObject jsonShow = new JSONObject();
                jsonShow.put("id", show.getId());
                jsonShow.put("name", show.getName());
                jsonShow.put("created", show.getCreated());

                // Save files
                JSONArray filesArray = new JSONArray();
                for (ShowFile file : show.getFiles()) {
                    JSONObject jsonFile = new JSONObject();
                    jsonFile.put("name", file.getName());
                    jsonFile.put("uri", file.getUri());
                    jsonFile.put("index", file.getIndex());
                    filesArray.put(jsonFile);
                }
                jsonShow.put("files", filesArray);

                // Save events
                JSONArray eventsArray = new JSONArray();
                for (ShowEvent event : show.getEvents()) {
                    JSONObject jsonEvent = new JSONObject();
                    jsonEvent.put("type", event.getType());
                    jsonEvent.put("trackIndex", event.getTrackIndex());
                    jsonEvent.put("trackName", event.getTrackName());
                    eventsArray.put(jsonEvent);
                }
                jsonShow.put("events", eventsArray);

                jsonArray.put(jsonShow);
            }
            prefs.edit().putString(KEY_SHOWS, jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e("ShowManager", "Error saving shows", e);
        }
    }
}
