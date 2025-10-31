package com.example.onebuttonplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// Import custom classes
import com.example.onebuttonplayer.ShowManager;
import com.example.onebuttonplayer.AudioPlayerManager;
import com.example.onebuttonplayer.PlayFragment;
import com.example.onebuttonplayer.ConfigureFragment;
import com.example.onebuttonplayer.LoadFragment;
import com.example.onebuttonplayer.player.PlayerService;

public class MainActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ShowManager showManager;
    private AudioPlayerManager audioPlayerManager;
    private PlayerService playerService;
    private boolean isServiceBound = false;
    
    // Service connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            playerService = binder.getInstance();
            isServiceBound = true;
            Log.i("MainActivity", "PlayerService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
            isServiceBound = false;
            Log.i("MainActivity", "PlayerService disconnected");
        }
    };
    
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
        
        // Bind to PlayerService
        Intent serviceIntent = new Intent(this, PlayerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        
        // Get the current fragment from ViewPager
        if (viewPager.getCurrentItem() == 0) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (fragment instanceof PlayFragment) {
                return (PlayFragment) fragment;
            }
        }
        
        // If not found by tag, try to find any existing PlayFragment
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof PlayFragment) {
                return (PlayFragment) fragment;
            }
        }
        
        return null;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Don't pause audio when app goes to background - let service handle it
        // audioPlayerManager.pauseAll();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayerManager.cleanup();
        
        // Unbind from PlayerService
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
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