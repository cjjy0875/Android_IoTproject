package com.example.android_iotproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            // 안드로이드 8 이상에서는 foreground 서비스로 시작한다.
            Intent clsIntent = new Intent(context, MainActivity.class);
            context.startForegroundService(clsIntent);
        } else {
            Intent clsIntent = new Intent(context, MainActivity.class);
            context.startService(clsIntent);
        }
    }

}