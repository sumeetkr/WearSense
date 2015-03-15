package sumeetkumar.in.wearsense;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;

public class WearSensingService extends WearableListenerService implements SensorEventListener {
    private static final String TAG = "ListenerService";

    private GoogleApiClient mGoogleApiClient;
    private SensorManager sensorManager;
    private PutDataMapRequest sensorData;
    private PutDataMapRequest audioData;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SoundDataCollector audioCollector;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG);

        if(audioCollector == null) audioCollector = new SoundDataCollector();
        handler = new Handler();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        return;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        String key = event.sensor.getName();
        float[] values = event.values;
        int currentAccuracy = sensorData.getDataMap().getInt(key + " Accuracy");
        if(event.accuracy > currentAccuracy) {
            Logger.log("New reading for sensor: " + key + " value: " + Arrays.toString(values));
            sensorData.getDataMap().putFloatArray(key, values);
            sensorData.getDataMap().putInt(key + " Accuracy", event.accuracy);
        }
        if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            Logger.log("Unregistering sensor: " + key);
            sensorManager.unregisterListener(this, event.sensor);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Logger.log("onMessageReceived: " + path);
        if (path.equals(Constants.START_SENSOR_RECORDING)) {
            try {
                acquireWakeLock();
                startSensorListeners();
                Thread.sleep(Constants.SENSOR_WINDOW);
                stopSensorListeners();
                sendSensorData(sensorData);
                releaseWakeLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if(path.equals(Constants.START_AUDIO_RECORDING)){
            try {
                acquireWakeLock();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        audioData = PutDataMapRequest.create(Constants.AUDIO_DATA_PATH);
                        startAudioCollection();
                    }
                });

                Thread.sleep(Constants.AUDIO_COLLECTION_WINDOW);
//                Thread.sleep(Constants.SENSOR_WINDOW);
                stopAudioCollection();
                prepareAudioAsset();
                sendSensorData(audioData);
                releaseWakeLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "onPeerDisconnected: " + peer);
    }

    private void startSensorListeners() {
        Logger.log("startSensorListeners");

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        sensorData = PutDataMapRequest.create(Constants.SENSOR_DATA_PATH);
        sensorData.getDataMap().putLong("Timestamp", System.currentTimeMillis());

        float[] empty = new float[0];
        for (Sensor sensor : sensors) {
            sensorData.getDataMap().putFloatArray(sensor.getName(), empty);
            sensorData.getDataMap().putInt(sensor.getName() + " Accuracy", 0);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void startAudioCollection(){
        audioData.getDataMap().putLong("Timestamp", System.currentTimeMillis());

        audioCollector.collectData(audioData.getDataMap());
    }

    private void stopSensorListeners() {
        Logger.log("stopSensorListeners");
        sensorManager.unregisterListener(WearSensingService.this);
    }

    private void stopAudioCollection(){
        audioCollector.notifyForDataCollectionFinished();
    }


    private void sendSensorData(PutDataMapRequest data) {
        try{
            Logger.log("trying to send SensorData");

            final PutDataRequest request = data.asPutDataRequest();
            PendingResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
            result.setResultCallback(new ResultCallback() {
                @Override
                public void onResult(Result result) {
                    notifyUser();
                    Logger.log(result.getStatus().getStatusMessage());
                }
            });
        }catch(Exception ex){
            Logger.log("exception n sending SensorData");
        }
    }

    private void prepareAudioAsset() {
        try{
            audioData.getDataMap().putAsset(
                    "audioAsset",
                    Asset.createFromBytes(audioCollector.getCollectedAudio().getBytes()));

            audioCollector.notifyForDataCollectionFinished();
            audioCollector.clear();
            audioCollector = null;
        }catch (Exception ex){
            Logger.log(ex.getMessage());
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private static Bitmap getBitmapFromString(String string){
        byte[] imageAsBytes = Base64.decode(string.getBytes(),Base64.DEFAULT);
        Bitmap bp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        return  bp;
    }

    private void sendMessage( String node, final String message) {
        Logger.log("Sending Message: " + message + " to Node: " + node);
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, message, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Logger.log("Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }else{
                            Logger.log("Successfully sent message");
                        }

                    }
                }
        );
    }

    private void notifyUser() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        int NOTIFICATION_ID = 1;

        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                        .setContentTitle("Sensing!!")
                        .setContentText(currentDateandTime)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setOngoing(false);

        // Build the notification and show it
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void fireMessage() {
        // Send the RPC
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                for (int i = 0; i < result.getNodes().size(); i++) {
                    Node node = result.getNodes().get(i);
                    String nName = node.getDisplayName();
                    String nId = node.getId();
                    Logger.log("Node name and ID: " + nName + " | " + nId);

                    Wearable.MessageApi.addListener(mGoogleApiClient, new MessageApi.MessageListener() {
                        @Override
                        public void onMessageReceived(MessageEvent messageEvent) {
                            Logger.log("Message received: " + messageEvent);
                        }
                    });

//                    PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, "New data available",
//                            Constants.SENSOR_DATA_PATH, null);
//                    messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
//                        @Override
//                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
//                            Status status = sendMessageResult.getStatus();
//                            Logger.log("Status: " + status.toString());
//                            if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {
//                                Log.d(TAG, status.getStatusMessage());
//                            }
//                        }
//                    });
                }
            }
        });
    }

    private void acquireWakeLock() {
        Logger.log("acquireWakeLock");
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        Logger.log("releaseWakeLock");
        wakeLock.release();
    }
}
