package com.tnt9.rsiwatchlist3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class CustomBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean backgroundRefresh = sharedPreferences.getBoolean("background_refresh_checkbox", false);
        if (backgroundRefresh){
            Task.scheduleJob(context);
        }
    }
}
