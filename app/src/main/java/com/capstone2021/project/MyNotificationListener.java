package com.capstone2021.project;

import android.app.Notification;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyNotificationListener extends NotificationListenerService {
    public final static String TAG = "MyNotificationListener";


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();




        Intent intent =  new Intent("android.service.notification.NotificationListenerService");
        intent.putExtra("text",text);
        intent.putExtra("title",title);





        Log.d(TAG, "onNotificationPosted ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId() +
                " postTime: " + sbn.getPostTime() +
                " title: " + title +
                " text : " + text +
                " subText: " + subText);


        if ((sbn.getPackageName().contains("com.kakao.talk") || sbn.getPackageName().contains("com.samsung.android.messaging") || sbn.getPackageName().contains("com.google.android.apps.messaging")
               || sbn.getPackageName().contains("insta"))&& (title!=null && text!=null)) {
            if (sbn.getPackageName().contains("com.kakao.talk")) {
                ((MainActivity) MainActivity.mContext).textView2.setText( "????????????" + " \n??????: " + title + " \n??????: " + text);
            }else if(sbn.getPackageName().contains("com.samsung.android.messaging") || sbn.getPackageName().contains("com.google.android.apps.messaging")) {
                ((MainActivity) MainActivity.mContext).textView2.setText( "SMS??????" + " \n??????: " + title + " \n??????: " + text);
            }else
                ((MainActivity) MainActivity.mContext).textView2.setText( "???????????????" + "\n??????: " + text);
            MyNotificationListener.this.sendBroadcast(intent); //?????????????????? ???????????? ????????? ??????

            //"packagename: "

        }
    }


}