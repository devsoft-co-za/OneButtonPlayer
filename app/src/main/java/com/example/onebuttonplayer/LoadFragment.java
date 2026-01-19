package com.example.onebuttonplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;

public class LoadFragment extends Fragment {

    private ShowManager showManager;
    private RecyclerView recyclerView;
    private ShowAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_load, container, false);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            showManager = activity.getShowManager();
        }

        recyclerView = view.findViewById(R.id.showsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ShowAdapter(showManager.getShows());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setShows(showManager.getShows());
        }
    }

    private class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ShowViewHolder> {

        private List<Show> shows;

        public ShowAdapter(List<Show> shows) {
            this.shows = shows;
        }

        @Override
        public ShowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
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

        public void setShows(List<Show> shows) {
            this.shows = shows;
            notifyDataSetChanged();
        }

        class ShowViewHolder extends RecyclerView.ViewHolder {
            private TextView showName;
            private TextView eventCount;
            private TextView createdDate;

            public ShowViewHolder(View itemView) {
                super(itemView);
                showName = itemView.findViewById(R.id.showName);
                eventCount = itemView.findViewById(R.id.eventCount);
                createdDate = itemView.findViewById(R.id.createdDate);

                itemView.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Show show = shows.get(position);
                        loadShow(show);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Show show = shows.get(position);
                        showDeleteConfirmation(show, position);
                        return true;
                    }
                    return false;
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
        android.util.Log.i("LoadFragment", "Loading show: " + show.getName());
        showManager.setCurrentShow(show);
        Toast.makeText(getContext(), "Loaded show: " + show.getName(), Toast.LENGTH_SHORT).show();

        // Reset play state by explicitly calling stopShow()
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            android.util.Log.i("LoadFragment", "Activity found, getting PlayFragment");
            PlayFragment playFragment = activity.getCurrentPlayFragment();
            if (playFragment != null) {
                android.util.Log.i("LoadFragment", "PlayFragment found, calling stopShow()");
                playFragment.stopShow(); // Explicitly call the stop button function
            } else {
                android.util.Log.w("LoadFragment", "PlayFragment is null!");
            }
            
            android.util.Log.i("LoadFragment", "Switching to play tab");
            activity.getViewPager().setCurrentItem(0); // Switch to play tab
        } else {
            android.util.Log.w("LoadFragment", "Activity is null!");
        }
    }

    private void showDeleteConfirmation(Show show, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Delete Show")
            .setMessage("Are you sure you want to delete \"" + show.getName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                showManager.deleteShow(show.getId());
                adapter.setShows(showManager.getShows());
                Toast.makeText(getContext(), "Show deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
