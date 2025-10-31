package com.example.onebuttonplayer.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.media.session.MediaSessionCompat;

import com.example.onebuttonplayer.MainActivity;
import com.example.onebuttonplayer.R;

public class PlayerNotificationManager {
    private static final String CHANNEL_ID = "onebuttonplayer_channel";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;
    private final PlayerService playerService;

    public PlayerNotificationManager(PlayerService playerService) {
        this.playerService = playerService;
        this.context = playerService;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "One Button Player",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Media playback controls");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startForegroundNotification() {
        Notification notification = createNotification();
        playerService.startForeground(NOTIFICATION_ID, notification);
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

        // Create play/pause action
        Intent playPauseIntent = new Intent(context, PlayerService.NotificationReceiver.class);
        playPauseIntent.setAction("PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE);

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
                .setPriority(Notification.PRIORITY_LOW);

        // Add actions
        builder.addAction(R.drawable.ic_play_arrow, "Play/Pause", playPausePendingIntent);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        // Add media session token for lock screen controls
        MediaSessionCompat mediaSession = playerService.getMediaSessionCompat();
        if (mediaSession != null) {
            builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1));
        }

        return builder.build();
    }
}
