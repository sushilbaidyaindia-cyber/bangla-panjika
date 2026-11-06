package com.sushilbaidyaindia.banglapanjika;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** The morning alarm, and re-setting the alarm after a restart, an app update or a time change. */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent == null ? null : intent.getAction();
        try {
            if (Reminders.ACTION_FIRE.equals(a)) Reminders.fire(context);
            else Reminders.schedule(context);
        } catch (RuntimeException e) {
            Reminders.schedule(context);
        }
    }
}
