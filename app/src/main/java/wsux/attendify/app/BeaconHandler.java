package wsux.attendify.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconHandler {

    private final static String TAG = "BEACON_HANDLER";
    public final static int REQUEST_ENABLE_BT = 9999;
    private final static String OUR_BEACON_MAC = "CB:B9:C1:9E:E8:10";

    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private ScanCallback callback;
    private OnBeaconSearchResult onBeaconSearchResult;
    private Map<String, Long> devicesFound = new HashMap<>();
    private Activity activity;


    public BeaconHandler(Context context, OnBeaconSearchResult onBeaconSearchResult) {
        this.activity = (Activity) context;
        this.manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.adapter = manager.getAdapter();
        this.onBeaconSearchResult = onBeaconSearchResult;

        ensureTheBluetoothIsEnable();

        this.scanner = adapter.getBluetoothLeScanner();
        setCallBack();
    }

    private void ensureTheBluetoothIsEnable() {
        if (isBluetootOff()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean isBluetootOff() {
        return adapter == null || !adapter.isEnabled();
    }

    private void setCallBack() {
        callback = new ScanCallback() {

            @Override
            public synchronized void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(TAG, "onScanResult: " + result.getDevice().getName() + " " + result.getDevice().getAddress());

                String deviceFoundMAC = result.getDevice().getAddress();

                if (OUR_BEACON_MAC.equals(deviceFoundMAC)) {
                    deviceFound();
                } else {
                    deviceNotFound(deviceFoundMAC);

                }
            }
        };
    }

    private void deviceNotFound(String address) {
        devicesFound.put(address, System.currentTimeMillis());
        notFound(devicesFound.size());
        removeDeviceFoundOld();
    }

    private void removeDeviceFoundOld() {
        List<String> keysToRemove = new LinkedList<>();

        for (Map.Entry<String, Long> deviceFound: devicesFound.entrySet()){
            if((System.currentTimeMillis() - deviceFound.getValue()) > 2) {
                keysToRemove.add(deviceFound.getKey());
            }
        }

        for (String key : keysToRemove) {
            devicesFound.remove(key);
        }
    }
    private void deviceFound() {
        this.stop();
        onBeaconSearchResult.found();
    }

    private void notFound(int numberOfOtherDevicesInScan) {
        onBeaconSearchResult.notFound(numberOfOtherDevicesInScan);
    }

    public void start() {
        Log.d(TAG, "start: ");
        scanner.startScan(callback);
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        scanner.stopScan(callback);
    }

    public interface OnBeaconSearchResult {
        void found();

        void foundButNotInTime(String deviceName, String hourStart, String hourEnd);

        void notFound(int numberOfOtherDevicesInScan);
    }

}
