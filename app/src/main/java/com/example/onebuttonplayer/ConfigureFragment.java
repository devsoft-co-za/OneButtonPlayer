package com.example.onebuttonplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;
import com.example.onebuttonplayer.ShowFile;
import com.example.onebuttonplayer.ShowEvent;
import com.example.onebuttonplayer.Show;

import java.util.ArrayList;
import java.util.List;

public class ConfigureFragment extends Fragment {
    private ShowManager showManager;
    private LinearLayout selectedFilesContainer;
    private LinearLayout eventsListContainer;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private String lastEventType = "start";
    private int lastTrackIndex = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure, container, false);

        // Get managers from MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
        }

        // Initialize file picker launcher
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    handleFileSelection(result.getData());
                }
            }
        );

        // Clear current show and setup UI
        showManager.setCurrentShow(null);
        setupUI(view);

        return view;
    }

    private void setupUI(View view) {
        // Get references to containers
        selectedFilesContainer = view.findViewById(R.id.selectedFilesContainer);
        eventsListContainer = view.findViewById(R.id.eventsListContainer);

        // Setup file selection button
        Button selectFilesBtn = view.findViewById(R.id.selectFilesBtn);
        selectFilesBtn.setOnClickListener(v -> selectFiles());

        // Setup other buttons (to be implemented)
        Button addEventBtn = view.findViewById(R.id.addEventBtn);
        Button saveShowBtn = view.findViewById(R.id.saveShowBtn);
        Button clearShowBtn = view.findViewById(R.id.clearShowBtn);

        addEventBtn.setOnClickListener(v -> showEventDialog());
        saveShowBtn.setOnClickListener(v -> saveShow());
        clearShowBtn.setOnClickListener(v -> clearShow());
    }

    private void selectFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/mpeg", "audio/mp3", "audio/mp4", "audio/wav", "audio/ogg"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Audio Files"));
    }

    private void handleFileSelection(Intent data) {
        if (data.getClipData() != null) {
            // Multiple files selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                addFileToShow(uri);
            }
            Toast.makeText(getContext(), "Selected " + count + " files", Toast.LENGTH_SHORT).show();
        } else if (data.getData() != null) {
            // Single file selected
            Uri uri = data.getData();
            addFileToShow(uri);
            Toast.makeText(getContext(), "Selected 1 file", Toast.LENGTH_SHORT).show();
        }
        
        updateSelectedFilesList();
    }

    private void addFileToShow(Uri uri) {
        // Get the file name from URI
        String fileName = getFileNameFromUri(uri);
        
        // Create ShowFile and add to current show
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null) {
            // Create a new show if none exists
            currentShow = new Show(String.valueOf(System.currentTimeMillis()), "New Show");
            showManager.setCurrentShow(currentShow);
        }
        
        ShowFile file = new ShowFile(fileName, uri.toString(), currentShow.getFiles().size());
        currentShow.getFiles().add(file);
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = uri.getLastPathSegment();
        if (fileName == null) {
            fileName = "audio_file_" + System.currentTimeMillis();
        }
        return fileName;
    }

    private void updateSelectedFilesList() {
        selectedFilesContainer.removeAllViews();
        
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null || currentShow.getFiles().isEmpty()) {
            TextView noFilesMessage = new TextView(getContext());
            noFilesMessage.setText("No files selected");
            noFilesMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noFilesMessage.setTextAppearance(android.R.style.TextAppearance_Medium);
            noFilesMessage.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            selectedFilesContainer.addView(noFilesMessage);
            return;
        }

        for (ShowFile file : currentShow.getFiles()) {
            TextView fileView = new TextView(getContext());
            fileView.setText(file.getName());
            fileView.setPadding(8, 8, 8, 8);
            fileView.setBackgroundColor(getResources().getColor(android.R.color.white, null));
            fileView.setTextAppearance(android.R.style.TextAppearance_Small);
            selectedFilesContainer.addView(fileView);
        }
    }

    private void showEventDialog() {
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null || currentShow.getFiles().isEmpty()) {
            Toast.makeText(getContext(), "Please select audio files first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_event, null);
        
        // Setup event type spinner
        Spinner eventTypeSpinner = dialogView.findViewById(R.id.eventTypeSpinner);
        String[] eventTypes = {"start", "stop", "pause", "resume"};
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, eventTypes);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);
        
        // Set last selected event type
        for (int i = 0; i < eventTypes.length; i++) {
            if (eventTypes[i].equals(lastEventType)) {
                eventTypeSpinner.setSelection(i);
                break;
            }
        }

        // Setup track spinner
        Spinner trackSpinner = dialogView.findViewById(R.id.trackSpinner);
        List<String> trackNames = new ArrayList<>();
        for (ShowFile file : currentShow.getFiles()) {
            trackNames.add(file.getName());
        }
        ArrayAdapter<String> trackAdapter = new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_item, trackNames);
        trackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trackSpinner.setAdapter(trackAdapter);
        
        // Set last selected track
        if (lastTrackIndex < trackNames.size()) {
            trackSpinner.setSelection(lastTrackIndex);
        }

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
            .setTitle("Add Event")
            .setPositiveButton("Add", (dialog, which) -> {
                String eventType = eventTypeSpinner.getSelectedItem().toString();
                int trackIndex = trackSpinner.getSelectedItemPosition();
                String trackName = trackNames.get(trackIndex);
                
                // Persist selections for next time
                lastEventType = eventType;
                lastTrackIndex = trackIndex;
                
                addEventToShow(eventType, trackIndex, trackName);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addEventToShow(String eventType, int trackIndex, String trackName) {
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null) {
            currentShow = new Show(String.valueOf(System.currentTimeMillis()), "New Show");
            showManager.setCurrentShow(currentShow);
        }
        
        ShowEvent event = new ShowEvent(eventType, trackIndex, trackName);
        currentShow.getEvents().add(event);
        updateEventsList();
        Toast.makeText(getContext(), "Event added: " + eventType + " - " + trackName, Toast.LENGTH_SHORT).show();
    }

private void updateEventsList() {
    eventsListContainer.removeAllViews();

    Show currentShow = showManager.getCurrentShow();
    if (currentShow == null || currentShow.getEvents().isEmpty()) {
        TextView noEventsMessage = new TextView(getContext());
        noEventsMessage.setText("No events added");
        noEventsMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noEventsMessage.setTextAppearance(android.R.style.TextAppearance_Medium);
        noEventsMessage.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        eventsListContainer.addView(noEventsMessage);
        return;
    }

    for (ShowEvent event : currentShow.getEvents()) {
        TextView eventView = new TextView(getContext());
        eventView.setText(event.getType().toUpperCase() + " - " + event.getTrackName());
        eventView.setPadding(8, 8, 8, 8);
        eventView.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        eventView.setTextAppearance(android.R.style.TextAppearance_Small);
        eventsListContainer.addView(eventView);
    }
}
    private void saveShow() {
        View view = getView();
        if (view == null) return;
        
        android.widget.EditText showNameInput = view.findViewById(R.id.showNameInput);
        String showName = showNameInput.getText().toString().trim();
        
        if (showName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a show name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Show currentShow = showManager.getCurrentShow();
        if (currentShow == null || currentShow.getFiles().isEmpty()) {
            Toast.makeText(getContext(), "Please select audio files first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentShow.getEvents().isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one event", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Set the show name and save
        currentShow.setName(showName);
        showManager.saveShow(currentShow);
        
        Toast.makeText(getContext(), "Show saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Clear the form after saving
        clearShow();
    }

    private void clearShow() {
        // Clear the current show
        showManager.setCurrentShow(null);
        
        // Clear UI elements
        View view = getView();
        if (view != null) {
            android.widget.EditText showNameInput = view.findViewById(R.id.showNameInput);
            showNameInput.setText("");
        }
        
        // Clear the file and event lists
        selectedFilesContainer.removeAllViews();
        eventsListContainer.removeAllViews();
        
        // Show empty state messages
        TextView noFilesMessage = new TextView(getContext());
        noFilesMessage.setText("No files selected");
        noFilesMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noFilesMessage.setTextAppearance(android.R.style.TextAppearance_Medium);
        noFilesMessage.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        selectedFilesContainer.addView(noFilesMessage);
        
        TextView noEventsMessage = new TextView(getContext());
        noEventsMessage.setText("No events added");
        noEventsMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noEventsMessage.setTextAppearance(android.R.style.TextAppearance_Medium);
        noEventsMessage.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        eventsListContainer.addView(noEventsMessage);
        
        Toast.makeText(getContext(), "Show cleared", Toast.LENGTH_SHORT).show();
    }
}