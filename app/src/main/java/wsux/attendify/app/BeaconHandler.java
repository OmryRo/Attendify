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
import java.util.Map;

public class BeaconHandler
{

    private final static String TAG = "BEACON_HANDLER";
    public final static int REQUEST_ENABLE_BT = 9999;
    private final static String OUR_BEACON = "CB:B9:C1:9E:E8:10";

    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private ScanCallback callback;
    private OnBeaconSearchResult onBeaconSearchResult;

    public BeaconHandler(Context context, OnBeaconSearchResult onBeaconSearchResult) {
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        this.onBeaconSearchResult = onBeaconSearchResult;

        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanner = adapter.getBluetoothLeScanner();
        callback = new ScanCallback()
        {

            Map<String, Long> devicesFound = new HashMap<>();

            @Override
            public synchronized void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(TAG, "onScanResult: " + result.getDevice().getName() + " " + result.getDevice().getAddress());

                String address = result.getDevice().getAddress();

                if (OUR_BEACON.equals(address)) {
                    deviceFound();
                } else {
                    devicesFound.put(address, System.currentTimeMillis());
                    notFound(devicesFound.size());

                    for (Map.Entry<String, Long> entry : devicesFound.entrySet()) {
                        if ((System.currentTimeMillis() - entry.getValue()) > 2)
                            devicesFound.remove(entry.getKey());
                    }

                }
            }
        };
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

    public interface OnBeaconSearchResult
    {
        void found();

        void foundButNotInTime(String deviceName, String hourStart, String hourEnd);

        void notFound(int numberOfOtherDevicesInScan);
    }

}
