package sumeetkumar.in.wearsense.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import sumeetkumar.in.wearsense.utils.Logger;

/**
 * Created by sumeet on 9/24/14.
 */
public class AlarmManager {
    public static final int ALARM_REQUEST_CODE_FOR_REPEATING = Integer.MAX_VALUE;

    public static void setupRepeatingAlarmToWakeUpApplication(
            Context context,
            long timeBetweenRepeatsInMilliSeconds){

        android.app.AlarmManager alarmMgr = (android.app.AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Logger.log("setting up repeating alarm to trigger after milliseconds "
                + String.valueOf(timeBetweenRepeatsInMilliSeconds));

        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(BackgroundService.FETCH_WEAR_DATA);

        PendingIntent alarmIntent = PendingIntent.getService(
                context,
                AlarmManager.ALARM_REQUEST_CODE_FOR_REPEATING,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Logger.log("Alarm is already active");


        alarmMgr.setInexactRepeating(android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                timeBetweenRepeatsInMilliSeconds,
                alarmIntent);
    }
}
