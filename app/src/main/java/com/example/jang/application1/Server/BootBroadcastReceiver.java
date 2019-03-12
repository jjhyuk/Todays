package com.example.jang.application1.Server;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            Toast.makeText(context, "SCREEN_ON", Toast.LENGTH_SHORT).show();
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            Toast.makeText(context, "SCREEN_OFF", Toast.LENGTH_SHORT).show();
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, ClientSocketService.class);
            context.startService(i);
        }



    }
}


