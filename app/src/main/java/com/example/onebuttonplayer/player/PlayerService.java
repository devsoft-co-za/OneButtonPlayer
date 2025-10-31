package com.example.onebuttonplayer.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.media2.session.MediaSession;
import androidx.media2.session.MediaController;
import androidx.media2.common.SessionPlayer;
import android.view.KeyEvent;

import com.example.onebuttonplayer.AudioPlayerManager;
import com.example.onebuttonplayer.MainActivity;
import com.example.onebuttonplayer.PlayFragment;
import com.example.onebuttonplayer.ShowManager;

public class PlayerService extends Service {

    private final IBinder iBinder = new LocalBinder();
    private AudioPlayerManager audioPlayerManager;
    private PlayerNotificationManager notificationManager;
    private MediaSession mediaSession;
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
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
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
            mediaSession = new MediaSession.Builder(this)
                .setSessionCallback(new MediaSession.SessionCallback() {
                    @Override
                    public void onPlay(MediaSession session, SessionPlayer player) {
                        // Handle play command from media buttons
                        if (audioPlayerManager != null && !audioPlayerManager.isPlaying()) {
                            // Resume playback or trigger next event
                            triggerNextEvent();
                        }
                    }

                    @Override
                    public void onPause(MediaSession session, SessionPlayer player) {
                        // Handle pause command from media buttons
                        if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
                            audioPlayerManager.pauseAll();
                        }
                    }

                    @Override
                    public void onStop(MediaSession session, SessionPlayer player) {
                        // Handle stop command from media buttons
                        if (audioPlayerManager != null) {
                            audioPlayerManager.stopAll();
                            stopForegroundService();
                        }
                    }
                })
                .build();

            // Set session active to receive media button events
            mediaSession.setActive(true);
            
            android.util.Log.i("PlayerService", "MediaSession configured and active");
        } catch (Exception e) {
            android.util.Log.e("PlayerService", "Error configuring MediaSession", e);
        }
    }

    private boolean handleMediaButtonEvent(Intent mediaButtonEvent) {
        boolean isSuccess = false;
        if (mediaButtonEvent == null) {
            return false;
        }

        KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    // Toggle play/pause
                    if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
                        audioPlayerManager.pauseAll();
                    }
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_CLOSE:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    if (audioPlayerManager != null) {
                        audioPlayerManager.pauseAll();
                    }
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // Previous event - handled by PlayFragment
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    // Next event - handled by PlayFragment
                    isSuccess = true;
                    break;

                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    // Not applicable
                    isSuccess = true;
                    break;
            }
        }

        return isSuccess;
    }

    private void triggerNextEvent() {
        // Forward the event trigger to the PlayFragment if the app is in foreground
        MainActivity activity = getMainActivity();
        if (activity != null) {
            PlayFragment playFragment = activity.getCurrentPlayFragment();
            if (playFragment != null) {
                playFragment.handleButtonPress();
            }
        } else {
            // If app is in background, trigger the event directly using the service's audio player
            if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
                // Simulate button press by triggering the next event
                android.util.Log.i("PlayerService", "TRIGGER_EVENT: Simulating button press in background");
                // For now, we'll just pause/play as a simple action
                audioPlayerManager.pauseAll();
                new android.os.Handler().postDelayed(() -> {
                    if (audioPlayerManager != null) {
                        // Resume playback after brief pause
                        // In a full implementation, this would advance to the next event
                        audioPlayerManager.resumeTrack(0); // Simplified - assumes track 0
                    }
                }, 500);
            }
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

    @Override
    public void onDestroy() {
        if (audioPlayerManager != null) {
            audioPlayerManager.cleanup();
        }
        notificationManager = null;
        audioPlayerManager = null;

        if (mediaSession != null) {
            mediaSession.close();
        }

        super.onDestroy();
    }

    public MediaSession getMediaSession() {
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