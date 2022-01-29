package com.covid.smartmask.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.covid.smartmask.R;

public class AlarmWarningReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "smartmaskWarning")
                .setSmallIcon(R.drawable.facemaskicon)
                .setContentTitle("Concentración de aire muy alto")
                .setContentText("Por favor quítese la máscara por un tiempo")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(18000) //Disappear after 3 minutes
                .setLights(Color.BLUE, 500, 500)
                .setSound(alarmSound)
                .setVibrate(pattern)
                .setColor(context.getColor(R.color.green_acid));


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(2, builder.build());

    }
}
