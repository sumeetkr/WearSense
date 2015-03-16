package sumeetkumar.in.wearsense.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;

public class BackgroundService extends Service {
    public static final String FETCH_WEAR_DATA = "fetch_wear_data";
    private final IBinder mBinder = new LocalBinder();


    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if(intent == null){
            Logger.log("Intent was null");
        }else{
            //do something
        }

        Intent startSensingIntent = new Intent(this, StartSensingBroadcastReceiver.class);
        startSensingIntent.putExtra(Constants.ACTION, Constants.START_AUDIO_RECORDING);
        sendBroadcast(startSensingIntent);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Service.START_NOT_STICKY;
    }
}