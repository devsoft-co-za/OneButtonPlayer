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
// Fragment classes
static class PlayFragment extends Fragment {
    private ShowManager showManager;
    private AudioPlayerManager audioPlayerManager;
    private boolean isPlaying = false;
    private int currentEventIndex = 0;

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

        // Setup status indicator click handler
        android.widget.LinearLayout statusIndicator = view.findViewById(R.id.statusIndicator);
        statusIndicator.setOnClickListener(v -> handleButtonPress());
    }

    private void startShow() {
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null || currentShow.getEvents().isEmpty()) {
            Toast.makeText(getContext(), "No show loaded or no events configured", Toast.LENGTH_SHORT).show();
            return;
        }

        isPlaying = true;
        currentEventIndex = 0;
        audioPlayerManager.setCurrentEventIndex(0);
        updatePlayScreen();

        // Visual feedback
        android.widget.LinearLayout statusIndicator = getView().findViewById(R.id.statusIndicator);
        statusIndicator.setBackgroundColor(android.graphics.Color.GREEN);
        new android.os.Handler().postDelayed(() -> 
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, null)), 300);
    }

    private void pauseShow() {
        isPlaying = false;
        audioPlayerManager.pauseAll();
        updatePlayScreen();
    }

    private void stopShow() {
        isPlaying = false;
        currentEventIndex = 0;
        audioPlayerManager.stopAll();
        updatePlayScreen();
    }

    private void updatePlayScreen() {
        TextView showName = getView().findViewById(R.id.showName);
        TextView eventCount = getView().findViewById(R.id.eventCount);
        TextView playStatus = getView().findViewById(R.id.playStatus);
        TextView statusText = getView().findViewById(R.id.statusText);
        TextView statusIcon = getView().findViewById(R.id.statusIcon);
        android.widget.LinearLayout statusIndicator = getView().findViewById(R.id.statusIndicator);

        Show currentShow = showManager.getCurrentShow();
        
        if (currentShow != null) {
            showName.setText(currentShow.getName());
            eventCount.setText("Events: " + currentShow.getEvents().size());
            playStatus.setText(isPlaying ? "Playing" : "Stopped");
            
            // Update status indicator based on play state
            if (isPlaying) {
                statusText.setText("Playing Show");
                statusIcon.setText("▶");
                statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, null));
            } else {
                statusText.setText("Ready to Play");
                statusIcon.setText("🎵");
                statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, null));
            }
        } else {
            showName.setText("None");
            eventCount.setText("0");
            playStatus.setText("Stopped");
            
            statusText.setText("No Show Loaded");
            statusIcon.setText("❌");
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    public void handleButtonPress() {
        if (!isPlaying || showManager.getCurrentShow() == null) return;

        Show currentShow = showManager.getCurrentShow();
        
        if (currentEventIndex >= currentShow.getEvents().size()) {
            // Show completed
            isPlaying = false;
            updatePlayScreen();
            Toast.makeText(getContext(), "Show Complete!", Toast.LENGTH_SHORT).show();
            return;
        }

        ShowEvent event = currentShow.getEvents().get(currentEventIndex);
        executeEvent(event);
        currentEventIndex++;
        audioPlayerManager.setCurrentEventIndex(currentEventIndex);
        
        // Update status
        TextView statusText = getView().findViewById(R.id.statusText);
        TextView statusIcon = getView().findViewById(R.id.statusIcon);
        statusText.setText("Event " + currentEventIndex + "/" + currentShow.getEvents().size());
        statusIcon.setText(getEventIcon(event.getType()));
        
        // Visual feedback
        android.widget.LinearLayout statusIndicator = getView().findViewById(R.id.statusIndicator);
        statusIndicator.setBackgroundColor(android.graphics.Color.YELLOW);
        new android.os.Handler().postDelayed(() -> 
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, null)), 300);
    }

    private void executeEvent(ShowEvent event) {
        int trackIndex = event.getTrackIndex();
        Show currentShow = showManager.getCurrentShow();
        
        if (currentShow.getFiles().size() <= trackIndex) {
            android.util.Log.e("PlayFragment", "Track index out of bounds: " + trackIndex);
            Toast.makeText(getContext(), "Error: Audio file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ShowFile track = currentShow.getFiles().get(trackIndex);
        
        switch (event.getType()) {
            case "start":
                audioPlayerManager.playTrack(trackIndex, track.getUri());
                break;
            case "stop":
                audioPlayerManager.stopTrack(trackIndex);
                break;
            case "pause":
                audioPlayerManager.pauseTrack(trackIndex);
                break;
            case "resume":
                audioPlayerManager.resumeTrack(trackIndex);
                break;
        }
    }

    private String getEventIcon(String eventType) {
        switch (eventType) {
            case "start": return "▶";
            case "stop": return "⏹";
            case "pause": return "⏸";
            case "resume": return "▶";
            default: return "🎵";
        }
    }
}

static class ConfigureFragment extends Fragment {
    private ShowManager showManager;
    private java.util.List<ShowFile> selectedFiles = new java.util.ArrayList<>();
    private java.util.List<ShowEvent> currentEvents = new java.util.ArrayList<>();
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure, container, false);

        // Get managers from MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
        }

        // Setup file picker launcher
        setupFilePickerLauncher();

        // Setup UI components
        setupConfigureControls(view);

        return view;
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    handleFileSelection(result.getData());
                }
            }
        );
    }

    private void setupConfigureControls(View view) {
        Button selectFilesBtn = view.findViewById(R.id.selectFilesBtn);
        Button addEventBtn = view.findViewById(R.id.addEventBtn);
        Button saveShowBtn = view.findViewById(R.id.saveShowBtn);
        Button clearShowBtn = view.findViewById(R.id.clearShowBtn);

        selectFilesBtn.setOnClickListener(v -> selectFiles());
        addEventBtn.setOnClickListener(v -> showEventDialog());
        saveShowBtn.setOnClickListener(v -> saveShow());
        clearShowBtn.setOnClickListener(v -> clearShow());
    }

    private void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        
        filePickerLauncher.launch(intent);
    }

    private void handleFileSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple files selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                android.net.Uri uri = data.getClipData().getItemAt(i).getUri();
                addFileFromUri(uri);
            }
        } else if (data.getData() != null) {
            // Single file selected
            android.net.Uri uri = data.getData();
            addFileFromUri(uri);
        }
        
        updateSelectedFilesList();
    }

    private void addFileFromUri(android.net.Uri uri) {
        try {
            // Take persistent URI permission
            getActivity().getContentResolver().takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Get file name from URI
            String fileName = getFileNameFromUri(uri);
            
            ShowFile file = new ShowFile(fileName, uri.toString(), selectedFiles.size());
            selectedFiles.add(file);
            
            android.util.Log.i("ConfigureFragment", "Added file: " + fileName + " URI: " + uri);
        } catch (Exception e) {
            android.util.Log.e("ConfigureFragment", "Error adding file from URI", e);
            Toast.makeText(getContext(), "Error adding file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(android.net.Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ConfigureFragment", "Error getting file name", e);
            }
        }
        
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        
        return result;
    }

    private void updateSelectedFilesList() {
        android.widget.LinearLayout container = getView().findViewById(R.id.selectedFilesContainer);
        android.widget.TextView noFilesMessage = getView().findViewById(R.id.noFilesMessage);
        
        container.removeAllViews();
        
        if (selectedFiles.isEmpty()) {
            noFilesMessage.setVisibility(android.view.View.VISIBLE);
        } else {
            noFilesMessage.setVisibility(android.view.View.GONE);
            
            for (ShowFile file : selectedFiles) {
                android.widget.TextView fileView = new android.widget.TextView(getContext());
                fileView.setText(file.getName());
                fileView.setPadding(8, 8, 8, 8);
                fileView.setTextSize(14);
                container.addView(fileView);
            }
        }
    }

    private void showEventDialog() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(getContext(), "Please select audio files first", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Add Event");

        android.view.View dialogView = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_event, null);
        builder.setView(dialogView);

        android.widget.Spinner eventTypeSpinner = dialogView.findViewById(R.id.eventTypeSpinner);
        android.widget.Spinner trackSpinner = dialogView.findViewById(R.id.trackSpinner);

        // Setup spinners
        setupEventTypeSpinner(eventTypeSpinner);
        setupTrackSpinner(trackSpinner);

        builder.setPositiveButton("Save", (dialog, which) -> saveEvent(eventTypeSpinner, trackSpinner));
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void setupEventTypeSpinner(android.widget.Spinner spinner) {
        String[] eventTypes = {"start", "stop", "pause", "resume"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, eventTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupTrackSpinner(android.widget.Spinner spinner) {
        java.util.List<String> trackNames = new java.util.ArrayList<>();
        for (ShowFile file : selectedFiles) {
            trackNames.add(file.getName());
        }
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, trackNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void saveEvent(android.widget.Spinner eventTypeSpinner, android.widget.Spinner trackSpinner) {
        String eventType = eventTypeSpinner.getSelectedItem().toString();
        int trackIndex = trackSpinner.getSelectedItemPosition();
        String trackName = selectedFiles.get(trackIndex).getName();

        ShowEvent event = new ShowEvent(eventType, trackIndex, trackName);
        currentEvents.add(event);
        updateEventsList();
    }

    private void updateEventsList() {
        android.widget.LinearLayout container = getView().findViewById(R.id.eventsListContainer);
        android.widget.TextView noEventsMessage = getView().findViewById(R.id.noEventsMessage);
        
        container.removeAllViews();
        
        if (currentEvents.isEmpty()) {
            noEventsMessage.setVisibility(android.view.View.VISIBLE);
        } else {
            noEventsMessage.setVisibility(android.view.View.GONE);
            
            for (int i = 0; i < currentEvents.size(); i++) {
                ShowEvent event = currentEvents.get(i);
                android.widget.LinearLayout eventLayout = new android.widget.LinearLayout(getContext());
                eventLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                eventLayout.setPadding(8, 8, 8, 8);

                android.widget.TextView eventText = new android.widget.TextView(getContext());
                eventText.setText(event.getType().toUpperCase() + " - " + event.getTrackName());
                eventText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                eventText.setTextSize(14);

                android.widget.Button removeBtn = new android.widget.Button(getContext());
                removeBtn.setText("×");
                removeBtn.setBackgroundColor(android.graphics.Color.RED);
                removeBtn.setTextColor(android.graphics.Color.WHITE);
                final int index = i;
                removeBtn.setOnClickListener(v -> removeEvent(index));

                eventLayout.addView(eventText);
                eventLayout.addView(removeBtn);
                container.addView(eventLayout);
            }
        }
    }

    private void removeEvent(int index) {
        currentEvents.remove(index);
        updateEventsList();
    }

    private void saveShow() {
        android.widget.EditText showNameInput = getView().findViewById(R.id.showNameInput);
        String showName = showNameInput.getText().toString().trim();

        if (showName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a show name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFiles.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentEvents.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one event", Toast.LENGTH_SHORT).show();
            return;
        }

        String showId = java.util.UUID.randomUUID().toString();
        Show show = new Show(showId, showName);
        show.setFiles(new java.util.ArrayList<>(selectedFiles));
        show.setEvents(new java.util.ArrayList<>(currentEvents));

        showManager.saveShow(show);
        Toast.makeText(getContext(), "Show saved successfully!", Toast.LENGTH_SHORT).show();
        clearShow();
    }

    private void clearShow() {
        android.widget.EditText showNameInput = getView().findViewById(R.id.showNameInput);
        showNameInput.setText("");
        selectedFiles.clear();
        currentEvents.clear();
        updateSelectedFilesList();
        updateEventsList();
    }
}

static class LoadFragment extends Fragment {
    private ShowManager showManager;
    private androidx.recyclerview.widget.RecyclerView showsRecyclerView;
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