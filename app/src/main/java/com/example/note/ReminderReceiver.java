package com.example.note;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("note_id", -1);
        String title = intent.getStringExtra("note_title");
        String content = intent.getStringExtra("note_content");
        int categoryId = intent.getIntExtra("category_id", -1);
        String categoryName = intent.getStringExtra("category_name");

        String channelId = "reminder_channel";
        String channelName = "Reminder Notifications";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Kênh thông báo nhắc nhở ghi chú");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent clickIntent = new Intent(context, NoteListActivity.class);
        clickIntent.putExtra("category_id", categoryId);
        clickIntent.putExtra("category_name", categoryName);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, noteId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(clickPendingIntent);

        notificationManager.notify(noteId, builder.build());
    }
}
