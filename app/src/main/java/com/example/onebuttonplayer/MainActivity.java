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
    }
}
// Data model classes
class Show {
    private String id;
    private String name;
    private List<ShowFile> files;
    private List<ShowEvent> events;
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