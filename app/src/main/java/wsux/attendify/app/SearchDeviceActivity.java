package wsux.attendify.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;

import android.support.design.widget.Snackbar;


public class SearchDeviceActivity extends Activity
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

        if(savedInstanceState != null)
        {
            mSubscribe = savedInstanceState.getBoolean(KEY_SUBSCRIBED,false);
        }

        if (! havePermissions())
        {
            Log.i(TAG,"Requesting permissions needed for this app.");
            requestPermissions();
        }


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(havePermissions())
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

    }

    private boolean havePermissions()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSIONS_REQUEST_CODE);
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

    }
}
