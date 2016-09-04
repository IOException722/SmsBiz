package com.smsbiz;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiveSms extends BroadcastReceiver {
    private String TAG = ReceiveSms.class.getSimpleName();
    private String msgFrom;
    public ReceiveSms() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress() + " : ";
                msgFrom =  msgs[i].getOriginatingAddress();
                int id = msgs[i].getStatus();
                str += msgs[i].getMessageBody().toString();
                str += "\n";
            }
            Log.d(TAG, str);
            Notification(context,msgFrom, str);
        }
    }
    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }

    public void Notification(Context context, String msgFrom, String message) {

        String GROUP_KEY_SMS = "SMS_GROUP_NOTIFICATION";
        String strtitle = context.getString(R.string.newmsg )+" From "+msgFrom;
        Intent intent = new Intent(context, ComposeSms.class);
        intent.putExtra("title", strtitle);
        intent.putExtra("text", message);
        intent.putExtra("fromNotification",true);
        intent.putExtra("senderid",msgFrom);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_menu_black_24dp)
                .setTicker(msgFrom)
                .setContentTitle(msgFrom)
                .setContentText(message)
                .setGroup(GROUP_KEY_SMS)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.notify(createID(), notif);
    }


}
