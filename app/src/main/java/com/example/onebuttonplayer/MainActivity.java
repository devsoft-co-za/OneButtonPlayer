package com.example.onebuttonplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
    
    public ViewPager2 getViewPager() {
        return viewPager;
    }
    
    // Media button handling for hardware controls
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle media buttons
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE ||
            keyCode == KeyEvent.KEYCODE_MEDIA_STOP ||
            keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            
            Log.i("MainActivity", "Media button pressed: " + keyCode);
            
            // Forward to current PlayFragment
            PlayFragment playFragment = getCurrentPlayFragment();
            if (playFragment != null) {
                playFragment.handleButtonPress();
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private PlayFragment getCurrentPlayFragment() {
        // Get the current fragment from ViewPager
        if (viewPager.getCurrentItem() == 0) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (fragment instanceof PlayFragment) {
                return (PlayFragment) fragment;
            }
        }
        return null;
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
    private TextView noShowsMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_load, container, false);

        // Get managers from MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
        }

        // Setup RecyclerView
        setupRecyclerView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadShowsList();
    }

    private void setupRecyclerView(View view) {
        showsRecyclerView = view.findViewById(R.id.showsRecyclerView);
        noShowsMessage = view.findViewById(R.id.noShowsMessage);

        // Setup RecyclerView with LinearLayoutManager
        androidx.recyclerview.widget.LinearLayoutManager layoutManager = new androidx.recyclerview.widget.LinearLayoutManager(getContext());
        showsRecyclerView.setLayoutManager(layoutManager);

        // Create and set adapter
        ShowAdapter adapter = new ShowAdapter();
        showsRecyclerView.setAdapter(adapter);
    }

    private void loadShowsList() {
        java.util.List<Show> shows = showManager.getShows();
        ShowAdapter adapter = (ShowAdapter) showsRecyclerView.getAdapter();
        
        if (adapter != null) {
            adapter.setShows(shows);
            
            if (shows.isEmpty()) {
                noShowsMessage.setVisibility(android.view.View.VISIBLE);
                showsRecyclerView.setVisibility(android.view.View.GONE);
            } else {
                noShowsMessage.setVisibility(android.view.View.GONE);
                showsRecyclerView.setVisibility(android.view.View.VISIBLE);
            }
        }
    }

    private class ShowAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ShowAdapter.ShowViewHolder> {
        private java.util.List<Show> shows = new java.util.ArrayList<>();

        public void setShows(java.util.List<Show> shows) {
            this.shows = shows;
            notifyDataSetChanged();
        }

        @Override
        public ShowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
            return new ShowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShowViewHolder holder, int position) {
            Show show = shows.get(position);
            holder.bind(show);
        }

        @Override
        public int getItemCount() {
            return shows.size();
        }

        class ShowViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private TextView showName;
            private TextView eventCount;
            private TextView createdDate;

            public ShowViewHolder(View itemView) {
                super(itemView);
                showName = itemView.findViewById(R.id.showName);
                eventCount = itemView.findViewById(R.id.eventCount);
                createdDate = itemView.findViewById(R.id.createdDate);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        Show show = shows.get(position);
                        loadShow(show);
                    }
                });
            }

            public void bind(Show show) {
                showName.setText(show.getName());
                eventCount.setText("Events: " + show.getEvents().size());
                createdDate.setText("Created: " + show.getCreated());
            }
        }
    }

    private void loadShow(Show show) {
        showManager.setCurrentShow(show);
        Toast.makeText(getContext(), "Loaded show: " + show.getName(), Toast.LENGTH_SHORT).show();
        
        // Switch to play screen
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getViewPager().setCurrentItem(0); // Switch to play tab
        }
    }
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
    }
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