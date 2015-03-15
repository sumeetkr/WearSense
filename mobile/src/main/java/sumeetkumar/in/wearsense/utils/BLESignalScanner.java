package sumeetkumar.in.wearsense.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sumeet on 3/14/15.
 */
public class BLESignalScanner {
    private static BluetoothLeScanner scanner;
    public static double getSignalStrength(String nodeId, Context context){
        double signalStrength = 0.0;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Logger.log("Device does not support Bluetooth");
        }else if(!mBluetoothAdapter.isEnabled()){
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, BluetoothAdapter);
            Logger.log("Bluetooth not enabled.");
        }else{
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
//                    mBluetoothAdapter.add(device.getName() + "\n" + device.getAddress());
                    Logger.log(device.getName());
                }
            }
        }

        List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        ScanSettings scanSettings = scanSettingsBuilder.build();

        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanFilters,scanSettings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Logger.log(result.getDevice().getName() + " : " + result.getRssi());
            }
        });

        return  signalStrength;
    }

    private static BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            final int rssiCopy = rssi;
        }
    };
}
