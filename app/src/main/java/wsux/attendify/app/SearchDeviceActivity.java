package wsux.attendify.app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class SearchDeviceActivity extends Activity

{

    private static final String TAG = SearchDeviceActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_CODE = 1111;
    private static final String KEY_SUBSCRIBED = "subscribed";
    private BeaconHandler beaconHandler;
    private boolean mSubscribed = false;
    private BluetoothAdapter mBluetoothAdapter;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (savedInstanceState != null)
        {
            mSubscribed = savedInstanceState.getBoolean(KEY_SUBSCRIBED, false);
        }

        if (!havePermissions())
        {
            Log.i(TAG, "Requesting permissions needed for this app.");
            requestPermissions();
        }
        setBeaconHandler();


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (havePermissions())
        {
            beaconHandler.start();

        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        beaconHandler.stop();

    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SUBSCRIBED, mSubscribed);
    }

    private boolean havePermissions()
    {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    private void requestPermissions()
    {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    private void setBeaconHandler()
    {
        beaconHandler = new BeaconHandler(this, new BeaconHandler.OnBeaconSearchResult()
        {
            @Override
            public void found()
            {

                Intent changeToConfirmIdentity = new Intent(
                        SearchDeviceActivity.this,
                        ConfirmIdentityActivity.class
                );
                startActivity(changeToConfirmIdentity);

            }

            @Override
            public void foundButNotInTime(String deviceName, String hourStart, String hourEnd)
            {

            }

            @Override
            public void notFound(int numberOfOtherDevicesInScan)
            {

            }
        });
    }

}
