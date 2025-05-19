package com.mgregur.persistentcounter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class MidnightReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "daily_update_channel";
    private static final String TAG = "MidnightReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
            return;
        }

        if (MainActivity.ACTION_MIDNIGHT_UPDATE.equals(intent.getAction())) {
            incrementCounterAndNotify(context);
        }
    }

    private void incrementCounterAndNotify(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        int increment = prefs.getInt(MainActivity.INCREMENT_KEY, MainActivity.INCREMENT_STARTING_VALUE);
        int counter = prefs.getInt(MainActivity.COUNTER_KEY, MainActivity.COUNTER_STARTING_VALUE);

        counter += increment;

        prefs.edit().putInt(MainActivity.COUNTER_KEY, counter).apply();

        showNotification(context, counter, increment);
    }

    private void showNotification(Context context, int counter, int increment) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Counter Updated")
                .setContentText("+" + increment + " → Total: " + counter)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Daily Counter Updates",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Notification when the counter is updated at midnight");

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}