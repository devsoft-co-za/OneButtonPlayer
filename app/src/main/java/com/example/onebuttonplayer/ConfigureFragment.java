package com.example.onebuttonplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;

public class ConfigureFragment extends Fragment {
    private ShowManager showManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure, container, false);

        // Get managers from MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
        }

        // TODO: Implement configure screen UI and logic

        return view;
    }
}
