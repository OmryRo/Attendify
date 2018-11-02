package wsux.attendify.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchDeviceActivity extends Activity {

    private BeaconHandler beaconHandler;
    private ImageView loadingIV;
    private TextView loadingTV;
    private static final int FINE_LOCATION_REQUEST_CODE = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);
        loadingTV = (TextView) findViewById(R.id.loadingTV);
        loadingIV = (ImageView) findViewById(R.id.loadingIV);

        Animation animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);
        loadingIV.setAnimation(animation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            }
        }

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
                loadingIV.clearAnimation();

                loadingTV.setText(getString(R.string.devices_found, numberOfOtherDevicesInScan));


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconHandler.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconHandler.stop();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BeaconHandler.REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            finish();
        }
    }
}
