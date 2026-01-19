package com.example.onebuttonplayer.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import androidx.core.os.BundleCompat; // Added this import

import com.example.onebuttonplayer.AudioPlayerManager;
import com.example.onebuttonplayer.MainActivity;
import com.example.onebuttonplayer.PlayFragment;
import com.example.onebuttonplayer.Show;
import com.example.onebuttonplayer.ShowEvent;
import com.example.onebuttonplayer.ShowFile;
import com.example.onebuttonplayer.ShowManager;

public class PlayerService extends Service {

    private final IBinder iBinder = new LocalBinder();
    private AudioPlayerManager audioPlayerManager;
    private PlayerNotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private PowerManager.WakeLock wakeLock;

    public PlayerService() {
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public PlayerNotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.i("PlayerService", "onStartCommand called with action: " + (intent != null ? intent.getAction() : "null"));
        
        // Handle media button events
        if (intent != null && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = null; // Initialized keyEvent
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.class);
            } else {
                keyEvent = BundleCompat.getParcelable(intent.getExtras(), Intent.EXTRA_KEY_EVENT, KeyEvent.class); // Using BundleCompat
            }
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                android.util.Log.i("PlayerService", "Media button pressed: " + keyEvent.getKeyCode());
                handleMediaButtonEvent(intent);
            }
        }
        
        // Handle notification actions
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "TRIGGER_EVENT":
                    // Trigger next event (like the top button on PLAY screen)
                    android.util.Log.i("PlayerService", "TRIGGER_EVENT action received");
                    triggerNextEvent();
                    break;
                case "STOP":
                    // Stop the show
                    android.util.Log.i("PlayerService", "STOP action received");
                    if (audioPlayerManager != null) {
                        audioPlayerManager.stopAll();
                    }
                    stopForegroundService();
                    break;
            }
        }
        
        // Start as foreground service when audio is playing
        if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
            android.util.Log.i("PlayerService", "Audio is playing, starting foreground service");
            startForegroundService();
        }
        return START_STICKY;
    }

    private void configureMediaSession() {
        try {
            // Create media session for media button handling
            mediaSession = new MediaSessionCompat(this, "OneButtonPlayer");
            
            // Set session active to receive media button events
            mediaSession.setActive(true);
            
            // Set media session flags to receive media button events

            
            // Set playback state to indicate we can handle media buttons
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY | 
                               PlaybackStateCompat.ACTION_PAUSE | 
                               PlaybackStateCompat.ACTION_PLAY_PAUSE |
                               PlaybackStateCompat.ACTION_STOP |
                               PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                               PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
            
            mediaSession.setPlaybackState(stateBuilder.build());
            
            // Set media button event receiver
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    android.util.Log.i("PlayerService", "MediaSession callback: onPlay");
                    triggerNextEvent();
                }

                @Override
                public void onPause() {
                    android.util.Log.i("PlayerService", "MediaSession callback: onPause");
                    triggerNextEvent();
                }

                @Override
                public void onStop() {
                    android.util.Log.i("PlayerService", "MediaSession callback: onStop");
                    if (audioPlayerManager != null) {
                        audioPlayerManager.stopAll();
                    }
                    stopForegroundService();
                }

                @Override
                public void onSkipToNext() {
                    android.util.Log.i("PlayerService", "MediaSession callback: onSkipToNext");
                    triggerNextEvent();
                }

                @Override
                public void onSkipToPrevious() {
                    android.util.Log.i("PlayerService", "MediaSession callback: onSkipToPrevious");
                    // Previous event - not implemented for now
                }
            });
            
            // Set session token for notification manager
            if (notificationManager != null) {
                notificationManager.setMediaSession(mediaSession);
            }
            
            android.util.Log.i("PlayerService", "MediaSession configured and active with media button handling");
        } catch (Exception e) {
            android.util.Log.e("PlayerService", "Error configuring MediaSession", e);
        }
    }

    private boolean handleMediaButtonEvent(Intent mediaButtonEvent) {
        boolean isSuccess = false;
        if (mediaButtonEvent == null) {
            return false;
        }

        KeyEvent keyEvent = null; // Initialized keyEvent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent.class);
        } else {
            keyEvent = BundleCompat.getParcelable(mediaButtonEvent.getExtras(), Intent.EXTRA_KEY_EVENT, KeyEvent.class); // Using BundleCompat
        }

        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    // Toggle play/pause or trigger next event
                    android.util.Log.i("PlayerService", "Media button: Play/Pause/Next Event");
                    triggerNextEvent();
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_CLOSE:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    android.util.Log.i("PlayerService", "Media button: Stop");
                    if (audioPlayerManager != null) {
                        audioPlayerManager.stopAll();
                    }
                    stopForegroundService();
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    android.util.Log.i("PlayerService", "Media button: Previous");
                    // Previous event - not implemented for now
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    android.util.Log.i("PlayerService", "Media button: Next");
                    // Next event - trigger next event
                    triggerNextEvent();
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    android.util.Log.i("PlayerService", "Media button: Rewind");
                    // Not applicable
                    isSuccess = true;
                    break;
            }
        }

        return isSuccess;
    }

    private void triggerNextEvent() {
        android.util.Log.i("PlayerService", "triggerNextEvent() called");
        
        // First try to forward to PlayFragment if app is in foreground
        MainActivity activity = getMainActivity();
        if (activity != null) {
            android.util.Log.i("PlayerService", "App is in foreground, forwarding to PlayFragment");
            PlayFragment playFragment = activity.getCurrentPlayFragment();
            if (playFragment != null) {
                playFragment.handleButtonPress();
                return;
            }
        }
        
        // If app is in background, trigger the event directly
        android.util.Log.i("PlayerService", "App is in background, triggering event directly");
        if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
            // Get current show from ShowManager
            ShowManager showManager = new ShowManager(this);
            Show currentShow = showManager.getCurrentShow();
            
            if (currentShow != null && !currentShow.getEvents().isEmpty()) {
                int currentEventIndex = audioPlayerManager.getCurrentEventIndex();
                android.util.Log.i("PlayerService", "Current event index: " + currentEventIndex);
                
                if (currentEventIndex < currentShow.getEvents().size()) {
                    ShowEvent event = currentShow.getEvents().get(currentEventIndex);
                    android.util.Log.i("PlayerService", "Executing event: " + event.getType() + " - " + event.getTrackName());
                    executeEvent(event);
                    audioPlayerManager.setCurrentEventIndex(currentEventIndex + 1);
                } else {
                    // Show completed
                    android.util.Log.i("PlayerService", "Show completed, stopping playback");
                    audioPlayerManager.stopAll();
                    stopForegroundService();
                }
            } else {
                android.util.Log.w("PlayerService", "No current show or events available");
            }
        } else {
            android.util.Log.w("PlayerService", "AudioPlayerManager not available or not playing");
        }
    }

    private void executeEvent(ShowEvent event) {
        int trackIndex = event.getTrackIndex();
        ShowManager showManager = new ShowManager(this);
        Show currentShow = showManager.getCurrentShow();
        
        if (currentShow == null || currentShow.getFiles().size() <= trackIndex) {
            android.util.Log.e("PlayerService", "Track index out of bounds: " + trackIndex);
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

    private MainActivity getMainActivity() {
        // Try to get the MainActivity instance
        try {
            // This is a simplified approach - in a real app you'd use a better communication method
            return MainActivity.getInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (audioPlayerManager == null) {
            audioPlayerManager = new AudioPlayerManager(this);
            notificationManager = new PlayerNotificationManager(this);
        }

        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize managers if not already done
        if (audioPlayerManager == null) {
            audioPlayerManager = new AudioPlayerManager(this);
        }
        if (notificationManager == null) {
            notificationManager = new PlayerNotificationManager(this);
        }
        
        // Set up wake lock to keep CPU running when screen is off
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.setReferenceCounted(false);
        }
        
        // Configure media session for media button handling
        configureMediaSession();
    }

    @Override
    public void onDestroy() {
        if (audioPlayerManager != null) {
            audioPlayerManager.cleanup();
        }
        notificationManager = null;
        audioPlayerManager = null;

        if (mediaSession != null) {
            mediaSession.release();
        }

        super.onDestroy();
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public void startForegroundService() {
        if (notificationManager != null) {
            notificationManager.startForegroundNotification();
        }
        // Acquire wake lock when service starts
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    public void stopForegroundService() {
        if (notificationManager != null) {
            notificationManager.stopForegroundNotification();
        }
        // Release wake lock when service stops
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void updateNotification() {
        if (notificationManager != null) {
            notificationManager.updateNotification();
        }
    }

    public class LocalBinder extends Binder {
        public PlayerService getInstance() {
            return PlayerService.this;
        }
    }

    public static class NotificationReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle notification actions
            if (intent.getAction() != null) {
                Intent serviceIntent = new Intent(context, PlayerService.class);
                switch (intent.getAction()) {
                    case "TRIGGER_EVENT":
                        // Trigger next event (like the top button on PLAY screen)
                        serviceIntent.setAction("TRIGGER_EVENT");
                        context.startService(serviceIntent);
                        break;
                    case "STOP":
                        // Stop the show
                        serviceIntent.setAction("STOP");
                        context.startService(serviceIntent);
                        break;
                }
            }
        }
    }
}