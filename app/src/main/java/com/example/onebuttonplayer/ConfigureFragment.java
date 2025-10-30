package com.example.onebuttonplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;
import com.example.onebuttonplayer.ShowFile;

public class ConfigureFragment extends Fragment {
    private ShowManager showManager;
    private LinearLayout selectedFilesContainer;
    private LinearLayout eventsListContainer;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    
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

        // Setup UI components
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
        // TODO: Implement event dialog
        Toast.makeText(getContext(), "Event dialog not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void saveShow() {
        // TODO: Implement save show
        Toast.makeText(getContext(), "Save show not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void clearShow() {
        // TODO: Implement clear show
        Toast.makeText(getContext(), "Clear show not implemented yet", Toast.LENGTH_SHORT).show();
    }
}
