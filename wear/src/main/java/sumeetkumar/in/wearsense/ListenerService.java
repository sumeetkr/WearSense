package sumeetkumar.in.wearsense;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.WearableStatusCodes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;

public class ListenerService extends WearableListenerService implements SensorEventListener {
    private static final String TAG = "ListenerService";

    private GoogleApiClient mGoogleApiClient;
    private SensorManager sensorManager;
    private PutDataMapRequest sensorData;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SoundDataCollector audioCollector;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

//        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG);

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
        if (path.equals(Constants.SENSOR_DATA_PATH)) {
            try {
                acquireWakeLock();
                startSensorListeners();
                Thread.sleep(Constants.SENSOR_WINDOW);
                stopSensorListeners();
                sendSensorData();
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

        if(audioCollector == null) audioCollector = new SoundDataCollector();
        audioCollector.collectData(getApplicationContext(),false);

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

    private void stopSensorListeners() {
        Logger.log("stopSensorListeners");
        sensorManager.unregisterListener(ListenerService.this);
        audioCollector.notifyForDataCollectionFinished();
    }

    private void sendSensorData() {
        try{
            Logger.log("sendSensorData");
            final PutDataRequest request = sensorData.asPutDataRequest();
            final ListenerService srv = this;

            PendingResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
            result.setResultCallback(new ResultCallback() {
                int NOTIFICATION_ID = 1;
                @Override
                public void onResult(Result result) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());

                    Notification.Builder notificationBuilder =
                            new Notification.Builder(srv)
                                    .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                                    .setContentTitle("Wear Sense")
                                    .setContentText(currentDateandTime)
                                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                    .setOngoing(true);

                    // Build the notification and show it
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }
            });
            fireMessage();
        }catch(Exception ex){
            Logger.log("exception at sendSensorData");
        }
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

                    PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            Constants.SENSOR_DATA_PATH, null);
                    messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Status status = sendMessageResult.getStatus();
                            Logger.log("Status: " + status.toString());
                            if (status.getStatusCode() != WearableStatusCodes.SUCCESS) {
                                Log.d(TAG, status.getStatusMessage());
                            }
                        }
                    });
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
