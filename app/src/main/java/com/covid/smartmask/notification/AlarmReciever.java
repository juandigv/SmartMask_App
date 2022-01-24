package com.covid.smartmask.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.covid.smartmask.ExerciseActivity;
import com.covid.smartmask.R;

public class AlarmReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, ExerciseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("Notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "smartmaskExercise")
                .setSmallIcon(R.drawable.facemaskicon)
                .setContentTitle("Smart Mask Activity")
                .setContentText("Ingrese a la aplicación para tomar medidas")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Es hora de hacer algunos ejercicios y tomar medidas con la máscara")
                        .setBigContentTitle("Actividad Pendiente"))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(1800000) //Disappear after 30 minutes
                .setLights(Color.BLUE, 500, 500)
                .setSound(alarmSound)
                .setVibrate(pattern);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());

    }
}

