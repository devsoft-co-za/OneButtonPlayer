package com.example.onebuttonplayer.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.media.session.MediaSessionCompat;
import androidx.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.example.onebuttonplayer.AudioPlayerManager;
import com.example.onebuttonplayer.MainActivity;
import com.example.onebuttonplayer.ShowManager;

public class PlayerService extends Service {

    private final IBinder iBinder = new LocalBinder();
    private AudioPlayerManager audioPlayerManager;
    private PlayerNotificationManager notificationManager;
    private MediaSessionCompat mediaSessionCompat;
    private final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            // Handle play from media controls
            if (audioPlayerManager != null && !audioPlayerManager.isPlaying()) {
                // Resume playback
                // This would need to be coordinated with the UI
            }
        }

        @Override
        public void onPause() {
            // Handle pause from media controls
            if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
                audioPlayerManager.pauseAll();
            }
        }

        @Override
        public void onStop() {
            // Handle stop from media controls
            if (audioPlayerManager != null) {
                audioPlayerManager.stopAll();
            }
        }
    };
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
        // Handle media button events
        if (intent != null && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                handleMediaButtonEvent(intent);
            }
        }
        
        // Start as foreground service when audio is playing
        if (audioPlayerManager != null && audioPlayerManager.isPlaying()) {
            startForegroundService();
        }
        return START_STICKY;
    }

    private void configureMediaSession() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        ComponentName mediaButtonReceiverComponentName = new ComponentName(this, NotificationReceiver.class);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE);

        mediaSessionCompat = new MediaSessionCompat(this, "OneButtonPlayerMediaSession", mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent);
        mediaSessionCompat.setActive(true);
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        
        // Set initial playback state
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | 
                           PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP);
        mediaSessionCompat.setPlaybackState(stateBuilder.build());
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
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.setReferenceCounted(false);
        }

        configureMediaSession();
    }

    @Override
    public void onDestroy() {
        if (audioPlayerManager != null) {
            audioPlayerManager.cleanup();
        }
        notificationManager = null;
        audioPlayerManager = null;

        if (mediaSessionCompat != null) {
            mediaSessionCompat.release();
        }

        super.onDestroy();
    }

    public MediaSessionCompat getMediaSessionCompat() {
        return mediaSessionCompat;
    }

    public void startForegroundService() {
        if (notificationManager != null) {
            notificationManager.startForegroundNotification();
        }
    }

    public void stopForegroundService() {
        if (notificationManager != null) {
            notificationManager.stopForegroundNotification();
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
                    case "PLAY_PAUSE":
                        // Forward to service
                        serviceIntent.setAction("PLAY_PAUSE");
                        context.startService(serviceIntent);
                        break;
                    case "STOP":
                        // Forward to service
                        serviceIntent.setAction("STOP");
                        context.startService(serviceIntent);
                        break;
                }
            }
        }
    }
}
