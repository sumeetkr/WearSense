package sumeetkumar.in.wearsense.services;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;
import sumeetkumar.in.wearsense.utils.NewDataIntent;

public class DataSyncListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;

    public DataSyncListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
                try{
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap map = dataMapItem.getDataMap();

                    Asset audioAsset = dataMapItem.getDataMap().getAsset("audioAsset");
                    String audio = loadStringFromAsset(audioAsset);

                    saveData(dataMapAsJSONObject(map).toString() + "\n" + audio);

                    Logger.log("Received data ");

                }catch (Exception ex){
                    Logger.log("failed while retrieving data" + ex.getMessage());
                }
            }
        }
    }

    private JSONObject dataMapAsJSONObject(DataMap data) {
        Bundle bundle = data.toBundle();
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                Logger.log(e.getMessage());
            }
        }
        return json;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void saveData(String dataJSON) {
        if (!isExternalStorageWritable()) {
            Logger.log("External Storage Not Writable");
            return;
        }
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        directory.mkdirs();
        File file = new File(directory, "wearable_data.txt");

        NewDataIntent dataIntent = new NewDataIntent("",dataJSON,true);
        getApplicationContext().sendBroadcast(dataIntent);

        try {
            FileOutputStream stream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(dataJSON);
            writer.close();

        } catch (Exception e) {
            Logger.log("Error Saving");
            e.printStackTrace();
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
        if (messageEvent.getPath().contains(Constants.SENSOR_DATA_PATH))
        {
        //Need to pull data?
        }
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
