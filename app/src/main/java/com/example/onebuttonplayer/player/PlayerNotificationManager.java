package com.example.onebuttonplayer.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.media.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.onebuttonplayer.MainActivity;
import com.example.onebuttonplayer.R;

public class PlayerNotificationManager {
    private static final String CHANNEL_ID = "onebuttonplayer_channel";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;
    private final PlayerService playerService;
    private MediaSessionCompat mediaSession;

    public PlayerNotificationManager(PlayerService playerService) {
        this.playerService = playerService;
        this.context = playerService;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public void setMediaSession(MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "One Button Player",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Media playback controls");
                channel.setShowBadge(false);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.setSound(null, null); // No sound for media notifications
                channel.enableLights(false);
                channel.enableVibration(false);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void startForegroundNotification() {
        Notification notification = createNotification();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            playerService.startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            playerService.startForeground(NOTIFICATION_ID, notification);
        }
    }

    public void stopForegroundNotification() {
        playerService.stopForeground(true);
    }

    public void updateNotification() {
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification() {
        // Create intent to open the app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Create event trigger action (like the top button on PLAY screen)
        Intent triggerEventIntent = new Intent(context, PlayerService.NotificationReceiver.class);
        triggerEventIntent.setAction("TRIGGER_EVENT");
        PendingIntent triggerEventPendingIntent = PendingIntent.getBroadcast(context, 1, triggerEventIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create stop action
        Intent stopIntent = new Intent(context, PlayerService.NotificationReceiver.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setContentTitle("One Button Player")
                .setContentText("Playing audio")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE);

        // Add actions - single button to trigger events
        builder.addAction(R.drawable.ic_play_arrow, "Trigger Event", triggerEventPendingIntent);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        // Add media session token for lock screen controls
        if (mediaSession != null) {
            MediaStyle mediaStyle = new MediaStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());
            mediaStyle.setShowActionsInCompactView(0); // Show first action in compact view
            builder.setStyle(mediaStyle);
        }

        return builder.build();
    }
}
