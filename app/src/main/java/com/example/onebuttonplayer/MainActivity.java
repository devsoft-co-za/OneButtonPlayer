package com.example.onebuttonplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ShowManager showManager;
    private AudioPlayerManager audioPlayerManager;
    
    // Tab titles
    private final String[] tabTitles = {"Play", "Configure", "Load"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize managers
        showManager = new ShowManager(this);
        audioPlayerManager = new AudioPlayerManager(this);
        
        // Setup ViewPager and TabLayout
        setupViewPager();
        
        // Initialize the app
        initApp();
    }
    
    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Create adapter with fragments
        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
    
    private void initApp() {
        // Load last used show if available
        showManager.loadLastShow();
        
        // Update UI based on current state
        updateUI();
    }
    
    private void updateUI() {
        // This will be called from fragments to update the main UI
        // For now, just log the current state
        if (showManager.getCurrentShow() != null) {
            Log.i("MainActivity", "Current show: " + showManager.getCurrentShow().getName());
        }
    }
    
    // Getters for managers
    public ShowManager getShowManager() {
        return showManager;
    }
    
    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        audioPlayerManager.pauseAll();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayerManager.cleanup();
    }
}
// Fragment classes
class PlayFragment extends Fragment {
    private ShowManager showManager;
    private AudioPlayerManager audioPlayerManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        // Get managers from MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
            audioPlayerManager = activity.getAudioPlayerManager();
        }

        // Setup UI components
        setupPlayControls(view);
        updatePlayScreen();

        return view;
    }

    private void setupPlayControls(View view) {
        Button playBtn = view.findViewById(R.id.playBtn);
        Button pauseBtn = view.findViewById(R.id.pauseBtn);
        Button stopBtn = view.findViewById(R.id.stopBtn);

        playBtn.setOnClickListener(v -> startShow());
        pauseBtn.setOnClickListener(v -> pauseShow());
        stopBtn.setOnClickListener(v -> stopShow());
    }

    private void startShow() {
        // Implementation
    }

    private void pauseShow() {
        // Implementation
    }

    private void stopShow() {
        // Implementation
    }

    private void updatePlayScreen() {
        // Implementation
    }
}

class ConfigureFragment extends Fragment {
    // Implementation for configure screen
}

class LoadFragment extends Fragment {
    // Implementation for load screen
}

// Pager Adapter
class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlayFragment();
            case 1:
                return new ConfigureFragment();
            case 2:
                return new LoadFragment();
            default:
                return new PlayFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    private String created;

    public Show(String id, String name) {
        this.id = id;
        this.name = name;
        this.files = new ArrayList<>();
        this.events = new ArrayList<>();
        this.created = new Date().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<ShowFile> getFiles() { return files; }
    public List<ShowEvent> getEvents() { return events; }
    public String getCreated() { return created; }

    public void setName(String name) { this.name = name; }
    public void setFiles(List<ShowFile> files) { this.files = files; }
    public void setEvents(List<ShowEvent> events) { this.events = events; }
}

class ShowFile {
    private String name;
    private String uri;
    private int index;

    public ShowFile(String name, String uri, int index) {
        this.name = name;
        this.uri = uri;
        this.index = index;
    }

    // Getters
    public String getName() { return name; }
    public String getUri() { return uri; }
    public int getIndex() { return index; }
}

class ShowEvent {
    private String type; // "start", "stop", "pause", "resume"
    private int trackIndex;
    private String trackName;

    public ShowEvent(String type, int trackIndex, String trackName) {
        this.type = type;
        this.trackIndex = trackIndex;
        this.trackName = trackName;
    }

    // Getters
    public String getType() { return type; }
    public int getTrackIndex() { return trackIndex; }
    public String getTrackName() { return trackName; }
}
// Data model classes
class Show {
    private String id;
    private String name;
    private java.util.List<ShowFile> files;
    private java.util.List<ShowEvent> events;
    private String created;

    public Show(String id, String name) {
        this.id = id;
        this.name = name;
        this.files = new java.util.ArrayList<>();
        this.events = new java.util.ArrayList<>();
        this.created = new java.util.Date().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public java.util.List<ShowFile> getFiles() { return files; }
    public java.util.List<ShowEvent> getEvents() { return events; }
    public String getCreated() { return created; }

    public void setName(String name) { this.name = name; }
    public void setFiles(java.util.List<ShowFile> files) { this.files = files; }
    public void setEvents(java.util.List<ShowEvent> events) { this.events = events; }
}

class ShowFile {
    private String name;
    private String uri;
    private int index;

    public ShowFile(String name, String uri, int index) {
        this.name = name;
        this.uri = uri;
        this.index = index;
    }

    // Getters
    public String getName() { return name; }
    public String getUri() { return uri; }
    public int getIndex() { return index; }
}

class ShowEvent {
    private String type; // "start", "stop", "pause", "resume"
    private int trackIndex;
    private String trackName;

    public ShowEvent(String type, int trackIndex, String trackName) {
        this.type = type;
        this.trackIndex = trackIndex;
        this.trackName = trackName;
    }

    // Getters
    public String getType() { return type; }
    public int getTrackIndex() { return trackIndex; }
    public String getTrackName() { return trackName; }
}

// Show Manager for persistence and show management
// Audio Player Manager for native audio playback
class AudioPlayerManager {
    private final android.content.Context context;
    private final java.util.Map<Integer, android.media.MediaPlayer> players;
    private boolean isPlaying = false;
    private int currentEventIndex = 0;

    public AudioPlayerManager(android.content.Context context) {
        this.context = context;
        this.players = new java.util.HashMap<>();
    }

    public void playTrack(int trackIndex, String uri) {
        try {
            // Stop any currently playing track
            stopAll();

            android.media.MediaPlayer player = new android.media.MediaPlayer();
            player.setDataSource(context, android.net.Uri.parse(uri));
            player.prepare();
            player.start();

            players.put(trackIndex, player);
            isPlaying = true;

        } catch (Exception e) {
            android.util.Log.e("AudioPlayerManager", "Error playing track: " + uri, e);
        }
    }

    public void pauseTrack(int trackIndex) {
        android.media.MediaPlayer player = players.get(trackIndex);
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    public void resumeTrack(int trackIndex) {
        android.media.MediaPlayer player = players.get(trackIndex);
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    public void stopTrack(int trackIndex) {
        android.media.MediaPlayer player = players.get(trackIndex);
        if (player != null) {
            player.stop();
            player.release();
            players.remove(trackIndex);
        }
    }

    public void pauseAll() {
        for (android.media.MediaPlayer player : players.values()) {
            if (player.isPlaying()) {
                player.pause();
            }
        }
        isPlaying = false;
    }

    public void stopAll() {
        for (android.media.MediaPlayer player : players.values()) {
            player.stop();
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
class ShowManager {
    private static final String SHARED_PREFS_NAME = "OneButtonPlayerPrefs";
    private static final String KEY_SHOWS = "saved_shows";
    private static final String KEY_LAST_SHOW = "last_show_id";

    private final android.content.SharedPreferences prefs;
    private final java.util.List<Show> shows;
    private Show currentShow;

    public ShowManager(android.content.Context context) {
        prefs = context.getSharedPreferences(SHARED_PREFS_NAME, android.content.Context.MODE_PRIVATE);
        shows = new java.util.ArrayList<>();
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

    public java.util.List<Show> getShows() {
        return new java.util.ArrayList<>(shows);
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
            currentShow = shows.stream()
                .filter(show -> show.getId().equals(lastShowId))
                .findFirst()
                .orElse(null);
        }
    }

    private void loadShows() {
        String showsJson = prefs.getString(KEY_SHOWS, "[]");
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray(showsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject jsonShow = jsonArray.getJSONObject(i);
                Show show = new Show(
                    jsonShow.getString("id"),
                    jsonShow.getString("name")
                );

                // Load files
                org.json.JSONArray filesArray = jsonShow.getJSONArray("files");
                for (int j = 0; j < filesArray.length(); j++) {
                    org.json.JSONObject jsonFile = filesArray.getJSONObject(j);
                    ShowFile file = new ShowFile(
                        jsonFile.getString("name"),
                        jsonFile.getString("uri"),
                        jsonFile.getInt("index")
                    );
                    show.getFiles().add(file);
                }

                // Load events
                org.json.JSONArray eventsArray = jsonShow.getJSONArray("events");
                for (int j = 0; j < eventsArray.length(); j++) {
                    org.json.JSONObject jsonEvent = eventsArray.getJSONObject(j);
                    ShowEvent event = new ShowEvent(
                        jsonEvent.getString("type"),
                        jsonEvent.getInt("trackIndex"),
                        jsonEvent.getString("trackName")
                    );
                    show.getEvents().add(event);
                }

                shows.add(show);
            }
        } catch (Exception e) {
            android.util.Log.e("ShowManager", "Error loading shows", e);
        }
    }

    private void saveShows() {
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray();
            for (Show show : shows) {
                org.json.JSONObject jsonShow = new org.json.JSONObject();
                jsonShow.put("id", show.getId());
                jsonShow.put("name", show.getName());
                jsonShow.put("created", show.getCreated());

                // Save files
                org.json.JSONArray filesArray = new org.json.JSONArray();
                for (ShowFile file : show.getFiles()) {
                    org.json.JSONObject jsonFile = new org.json.JSONObject();
                    jsonFile.put("name", file.getName());
                    jsonFile.put("uri", file.getUri());
                    jsonFile.put("index", file.getIndex());
                    filesArray.put(jsonFile);
                }
                jsonShow.put("files", filesArray);

                // Save events
                org.json.JSONArray eventsArray = new org.json.JSONArray();
                for (ShowEvent event : show.getEvents()) {
                    org.json.JSONObject jsonEvent = new org.json.JSONObject();
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
            android.util.Log.e("ShowManager", "Error saving shows", e);
        }
    }
}