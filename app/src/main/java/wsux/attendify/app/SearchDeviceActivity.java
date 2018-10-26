package wsux.attendify.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
 import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
//import android.widget.Toolbar ;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;

import android.support.design.widget.Snackbar;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

public class SearchDeviceActivity extends FragmentActivity
    implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    private static final String TAG = SearchDeviceActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_CODE = 1111;
    private static final String KEY_SUBSCRIBED = "subscribed";
    private RelativeLayout mContainer;

    private BeaconHandler beaconHandler;
    private GoogleApiClient mGoogleApiClient;
    private boolean mSubscribe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

//        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(myToolbar);
        mContainer = (RelativeLayout) findViewById(R.id.main_activity_container);
        if(savedInstanceState != null)
        {
            mSubscribe = savedInstanceState.getBoolean(KEY_SUBSCRIBED,false);
        }

        if (! havePermissions())
        {
            Log.i(TAG,"Requesting permissions needed for this app.");
            requestPermissions();
        }
        setBeaconHandler();


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(havePermissions())
        {
            beaconHandler.start();
            buildGoogleApiClient();

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
        outState.putBoolean(KEY_SUBSCRIBED,mSubscribe);
    }

    private boolean havePermissions()
    {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        Log.i(TAG,"Attendify connected.");

    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.w(TAG,"Connected suspended. error code: "+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        if (mContainer != null) {
            Snackbar.make(mContainer,
                    "Exception while connecting to Google Play services: " +
                            connectionResult.getErrorMessage(),
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }
    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                            .setPermissions(NearbyPermissions.BLE).build())
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .build();
        }
    }

    private void setBeaconHandler()
    {
        beaconHandler = new BeaconHandler(this, new BeaconHandler.OnBeaconSearchResult() {
            @Override
            public void found() {

                Intent changeToConfirmIdentity = new Intent(
                        SearchDeviceActivity.this,
                        ConfirmIdentityActivity.class
                );
                startActivity(changeToConfirmIdentity);

            }

            @Override
            public void foundButNotInTime(String deviceName, String hourStart, String hourEnd) {

            }

            @Override
            public void notFound(int numberOfOtherDevicesInScan) {

            }
        });
    }


    private void showLinkToSettingsSnackbar() {
        if (beaconHandler == null) {
            return;
        }
        Snackbar.make(mContainer,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
    }

    private void showRequestPermissionsSnackbar() {
        if (mContainer == null) {
            return;
        }
        Snackbar.make(mContainer, R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request permission.
                        ActivityCompat.requestPermissions(SearchDeviceActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
    }
}
