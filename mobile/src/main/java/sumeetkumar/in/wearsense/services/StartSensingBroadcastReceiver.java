package sumeetkumar.in.wearsense.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Collection;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;
import sumeetkumar.in.wearsense.utils.WearablesHelper;

/**
 * Created by sumeet on 3/12/15.
 */
public class StartSensingBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // plan
        // First find if any wearable devices are available or not
        // Then ask them to start audio recording
        // Once they respond mentioning that audio recording has started, send ultrasonic chirps
        // Send message to stop recording
        // Send message to collect other sensor data, also start phones sensor collection at the same time
        Collection<String> nodes =  WearablesHelper.getNodes(context);
        if(nodes.size()>0){
            Logger.log("Found wearables connected, count: " + nodes.size());
            Intent service = new Intent(context, WearMessageService.class);
            String action = intent.getStringExtra(Constants.ACTION);
            service.putExtra(Constants.ACTION, action);
            startWakefulService(context, service);
        }else{
            Logger.log("No wearables connected at this time");
            //Also add the info to the json
        }
    }
}
