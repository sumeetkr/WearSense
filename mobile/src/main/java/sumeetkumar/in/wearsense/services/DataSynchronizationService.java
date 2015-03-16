package sumeetkumar.in.wearsense.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.FileWriter;
import sumeetkumar.in.wearsense.utils.Logger;
import sumeetkumar.in.wearsense.utils.NewDataIntent;
import sumeetkumar.in.wearsense.utils.SoundDataCollector;
import sumeetkumar.in.wearsense.utils.SoundPlayer;

public class DataSynchronizationService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;
    private Handler handler;
    private DataMap audioDataMap;
    private SoundDataCollector soundRecorder;

    public DataSynchronizationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle)
            {
                Logger.log("onConnected");
            }

            @Override
            public void onConnectionSuspended(int i)
            {
                Logger.log("onConnectionSuspended");
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        try{
            Logger.log("onDataChanged: New data to sync" + dataEvents);
            Toast.makeText(this, " onDataChanged", Toast.LENGTH_SHORT);

            final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
            dataEvents.close();
            if(!mGoogleApiClient.isConnected()) {
                ConnectionResult connectionResult = mGoogleApiClient
                        .blockingConnect(30, TimeUnit.SECONDS);
                if (!connectionResult.isSuccess()) {
                    Logger.log("DataLayerListenerService failed to connect to GoogleApiClient.");
                    return;
                }
            }
            // Loop through the events and send a message back to the node that created the data item.
            for (DataEvent event : events) {
                Uri uri = event.getDataItem().getUri();
                String path = uri.getPath();
                if (Constants.SENSOR_DATA_PATH.equals(path) && event.getType() == DataEvent.TYPE_CHANGED) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        DataMap map = dataMapItem.getDataMap();
                        String json = FileWriter.dataMapAsJSONObject(map).toString();
                        NewDataIntent dataIntent = new NewDataIntent("", json, true);
                        getApplicationContext().sendBroadcast(dataIntent);

                        FileWriter.saveData(json, Constants.SENSOR_DATA_PATH);
                        Logger.log("Received sensor data ");


                }else if(Constants.AUDIO_DATA_PATH.equals(path) && event.getType() == DataEvent.TYPE_CHANGED) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset audioAsset = dataMapItem.getDataMap().getAsset("audioAsset");
                    if(audioAsset!= null){
                        String audio = loadStringFromAsset(audioAsset);
                        DataMap dataMap = new DataMap();
                        dataMap.putLong(Constants.TIMESTAMP_START,
                                dataMapItem.getDataMap().getLong(Constants.TIMESTAMP_START));
                        dataMap.putLong(Constants.TIMESTAMP_END,
                                dataMapItem.getDataMap().getLong(Constants.TIMESTAMP_END));
                        dataMap.putString("audioData", audio);

                        FileWriter.saveData(FileWriter.dataMapAsJSONObject(dataMap).toString(), Constants.AUDIO_DATA_PATH);

                        Logger.log("Received audio data ");
                    }

                    Intent startSensingIntent = new Intent(this, StartSensingBroadcastReceiver.class);
                    startSensingIntent.putExtra(Constants.ACTION, Constants.START_SENSOR_RECORDING);
                    sendBroadcast(startSensingIntent);
                }
            }

        }catch (Exception ex){
            Logger.log("failed while retrieving data" + ex.getMessage());
        }
    }

    public String loadStringFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            Logger.log("bitmap result failed");
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Logger.log( "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        BufferedReader r = new BufferedReader(new InputStreamReader(assetInputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total.toString();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        Logger.log("Message received " + messageEvent.toString());
        super.onMessageReceived(messageEvent);
        if (messageEvent.getPath().contains(Constants.AUDIO_RECORDING_STARTED))
        {
         // Start mobile audio recording
            handler.post(new Runnable() {
                @Override
                public void run() {
                    startAudioRecording();
                }
            });

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopAudioCollection();
                    audioDataMap.putString("audioData", soundRecorder.getCollectedAudio());
                    FileWriter.saveData(FileWriter.dataMapAsJSONObject(audioDataMap).toString(), Constants.MOBILE_AUDIO_DATA_PATH);
                    clearAudioData();
                }
            }, Constants.AUDIO_COLLECTION_WINDOW);

            //Start ultrasonic sound
            SoundPlayer player = new SoundPlayer();
            player.playSound();
        }
    }

    public void startAudioRecording(){
        try {
            audioDataMap = new DataMap();
            soundRecorder = new SoundDataCollector();

            Logger.log("Starting audio data collection");
            audioDataMap.putLong(Constants.TIMESTAMP_START, System.currentTimeMillis());
            soundRecorder.collectData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAudioCollection() {
        Logger.log("Stopping audio data collection");
        audioDataMap.putLong(Constants.TIMESTAMP_END, System.currentTimeMillis());
        soundRecorder.notifyForDataCollectionFinished();
    }

    private void clearAudioData(){
        soundRecorder.clear();
    }

    @Override
    public void onPeerConnected(Node peer)
    {
        Logger.log("onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer)
    {
        Logger.log("onPeerDisconnected: " + peer);
    }

}
