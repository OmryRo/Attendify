package wsux.attendify.app;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchDeviceActivity extends Activity {

    private BeaconHandler beaconHandler;
    ImageView loadingIV;
    TextView loadingTV;

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
}
