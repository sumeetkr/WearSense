package sumeetkumar.in.wearsense.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by sumeet on 3/12/15.
 */
public class StartSensingBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, WearMessageService.class);
        startWakefulService(context, service);
    }
}
