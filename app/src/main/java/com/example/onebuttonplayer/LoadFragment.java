package com.example.onebuttonplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class LoadFragment extends Fragment {
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