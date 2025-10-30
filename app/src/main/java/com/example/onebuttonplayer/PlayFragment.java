package com.example.onebuttonplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;
import com.example.onebuttonplayer.AudioPlayerManager;
import com.example.onebuttonplayer.Show;
import java.util.List;

public class PlayFragment extends Fragment {
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

        // Reset play state when fragment is created
        isPlaying = false;
        currentEventIndex = 0;
        if (audioPlayerManager != null) {
            audioPlayerManager.setCurrentEventIndex(0);
        }

        // Setup UI components
        setupPlayControls(view);
        updatePlayScreen();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the screen whenever the fragment becomes visible
        updatePlayScreen();
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
        View view = getView();
        if (view != null) {
            android.widget.LinearLayout statusIndicator = view.findViewById(R.id.statusIndicator);
            statusIndicator.setBackgroundColor(android.graphics.Color.GREEN);
            new android.os.Handler().postDelayed(() -> 
                statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, null)), 300);
        }
    }

    private void pauseShow() {
        isPlaying = false;
        audioPlayerManager.pauseAll();
        updatePlayScreen();
    }

    public void stopShow() {
        android.util.Log.i("PlayFragment", "stopShow() called - resetting play state");
        isPlaying = false;
        currentEventIndex = 0;
        audioPlayerManager.stopAll();
        android.util.Log.i("PlayFragment", "stopShow() - isPlaying=" + isPlaying + ", currentEventIndex=" + currentEventIndex);
        updatePlayScreen();
    }

    private void updatePlayScreen() {
        View view = getView();
        if (view == null) return;
        
        TextView showName = view.findViewById(R.id.showName);
        TextView eventCount = view.findViewById(R.id.eventCount);
        TextView playStatus = view.findViewById(R.id.playStatus);
        TextView statusText = view.findViewById(R.id.statusText);
        TextView statusIcon = view.findViewById(R.id.statusIcon);
        android.widget.LinearLayout statusIndicator = view.findViewById(R.id.statusIndicator);

        Show currentShow = showManager.getCurrentShow();
        
        android.util.Log.i("PlayFragment", "updatePlayScreen() - currentShow=" + currentShow + ", isPlaying=" + isPlaying);
        
        // Always try to load the last show if none is currently set
        if (currentShow == null) {
            showManager.loadLastShow();
            currentShow = showManager.getCurrentShow();
        }
        
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
            // Check if there are any saved shows
            List<Show> savedShows = showManager.getShows();
            if (savedShows.isEmpty()) {
                showName.setText("No Shows");
                statusText.setText("No Shows Configured");
                statusIcon.setText("❌");
            } else {
                // If there are shows but none is loaded, load the first one
                Show firstShow = savedShows.get(0);
                showManager.setCurrentShow(firstShow);
                showName.setText(firstShow.getName());
                eventCount.setText("Events: " + firstShow.getEvents().size());
                playStatus.setText("Stopped");
                statusText.setText("Ready to Play");
                statusIcon.setText("🎵");
                statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, null));
            }
        }
    }

    public void handleButtonPress() {
        if (!isPlaying || showManager.getCurrentShow() == null) return;

        Show currentShow = showManager.getCurrentShow();
        
        if (currentEventIndex >= currentShow.getEvents().size()) {
            // Show completed
            isPlaying = false;
            currentEventIndex = 0;
            audioPlayerManager.setCurrentEventIndex(0);
            updatePlayScreen();
            Toast.makeText(getContext(), "Show Complete!", Toast.LENGTH_SHORT).show();
            return;
        }

        ShowEvent event = currentShow.getEvents().get(currentEventIndex);
        executeEvent(event);
        currentEventIndex++;
        audioPlayerManager.setCurrentEventIndex(currentEventIndex);
        
        // Update status
        View view = getView();
        if (view != null) {
            TextView statusText = view.findViewById(R.id.statusText);
            TextView statusIcon = view.findViewById(R.id.statusIcon);
            statusText.setText("Event " + currentEventIndex + "/" + currentShow.getEvents().size());
            statusIcon.setText(getEventIcon(event.getType()));
            
            // Visual feedback
            android.widget.LinearLayout statusIndicator = view.findViewById(R.id.statusIndicator);
            statusIndicator.setBackgroundColor(android.graphics.Color.YELLOW);
            new android.os.Handler().postDelayed(() -> 
                statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, null)), 300);
        }
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

    public void resetPlayState() {
        isPlaying = false;
        currentEventIndex = 0;
        audioPlayerManager.setCurrentEventIndex(0);
        updatePlayScreen();
    }
}