package com.example.primera_entrega;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "gamelog_channel";
    private static final String CHANNEL_NAME = "GameLog Notificaciones";
    private static int notificationId = 0;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de GameLog");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void sendNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (manager != null) {
            manager.notify(notificationId++, builder.build());
        }
    }

    public static void sendGameAddedNotification(Context context, String gameName) {
        sendNotification(context, "Juego añadido", gameName + " se ha añadido a tu GameLog");
    }

    public static void sendGameCompletedNotification(Context context, String gameName) {
        sendNotification(context, "¡Juego completado!", "Has completado " + gameName + ". ¡Enhorabuena!");
    }
}