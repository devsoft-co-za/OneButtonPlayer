package com.example.onebuttonplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

public class ConfigureFragment extends Fragment {
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