package sumeetkumar.in.wearsense.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sumeet on 3/15/15.
 */
public class WearablesHelper {

    public static Collection<String> getNodes(Context context) {

        HashSet<String> results = new HashSet<String>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Logger.log(device.getName());
                results.add(device.getName());
            }
        }
        return results;
    }
}
