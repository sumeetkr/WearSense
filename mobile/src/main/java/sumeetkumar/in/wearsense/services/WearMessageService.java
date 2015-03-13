package sumeetkumar.in.wearsense.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;

public class WearMessageService extends IntentService {

    private GoogleApiClient client;


    public WearMessageService(String name) {
        super(name);
    }

    public WearMessageService() {
        super("WearMessageService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        for (String node : getNodes()) {
            sendMessage(node, Constants.SENSOR_DATA_PATH);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        client.connect();
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(client).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    private void sendMessage(String node, final String message) {
        Logger.log("Sending Message: " + message + " to Node: " + node);
        Wearable.MessageApi.sendMessage(
                client, node, message, new byte[0]).setResultCallback(
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
}
