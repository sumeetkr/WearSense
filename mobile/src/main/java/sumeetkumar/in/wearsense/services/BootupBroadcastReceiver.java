package sumeetkumar.in.wearsense.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;

public class BootUpBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.log("boot up intent received");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmManager.setupRepeatingAlarmToWakeUpApplication(
                    context,
                    Constants.TIME_RANGE_TO_SHOW_ALERT_IN_MINUTES*60*1000);
        }

    }
}
