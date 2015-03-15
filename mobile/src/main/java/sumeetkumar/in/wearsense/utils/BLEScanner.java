package sumeetkumar.in.wearsense.utils;

/**
 * Created by sumeet on 3/14/15.
 */


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.telephony.TelephonyManager;

import java.util.HashMap;

import sumeetkumar.in.wearsense.data.BLEData;

public class BLEScanner{
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;

    // Stops scanning after 1 seconds.
    private static final long SCAN_PERIOD = 20000;
    private boolean mScanning = false;
    private Handler mHandler;
    private boolean enable;
    private HashMap<String, BLEData> bleTagsData;
    private String macAddres;
    private String phoneNumber;

    public BLEScanner(Context context){

        macAddres= getMacAddress(context);
        phoneNumber = getPhoneNumber(context);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bleTagsData = new HashMap<String, BLEData>();
        mHandler = new Handler(context.getMainLooper());

        enable = !enable;
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        scanLeDevice(true);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            final int rssiCopy = rssi;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    BLEData data = new BLEData(device.getAddress(),
                            device.getName(), rssiCopy, phoneNumber);

                    Logger.log(data.getMacAddress() +" : " + data.gettagName());
                    bleTagsData.put(data.gettagName(), data);
                }
            });
        }
    };


    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Logger.log(result.getDevice().getName() + " : " + result.getRssi());

            final int rssiCopy = result.getRssi();
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    BLEData data = new BLEData(result.getDevice().getAddress(),
//                            result.getDevice().getName(), rssiCopy, phoneNumber);
//
//                    Logger.log(data.getMacAddress() +" : " + data.gettagName());
//                    bleTagsData.put(data.gettagName(), data);
//                }
//            });
        }
    };

    private void scanLeDevice(boolean enable) {

        try {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;


//                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        scanner.stopScan(callback);
                        for (BLEData data : bleTagsData.values()) {
                            String toPrint = data.gettagName() + " : "
                                    + "Phone no " + data.getPhoneNumber() + " : "
                                    + "Strength "+ data.getSignalStrength() + " db";

                        }
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                scanner.startScan(callback);
//                bluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mScanning = false;
//                bluetoothAdapter.stopLeScan(mLeScanCallback);
                scanner.stopScan(callback);
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    private String getDistanceFromSignalStrength(int signalStrength){
        double distance = (1000/(110 + signalStrength) - 13);
        return Double.toString( distance) + " ft";
    }

    private String getMacAddress(Context context){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();

    }

    private String getPhoneNumber(Context context){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

}