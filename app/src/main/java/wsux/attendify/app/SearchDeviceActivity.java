package wsux.attendify.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SearchDeviceActivity extends Activity {

    private BeaconHandler beaconHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        BeaconHandler beaconHandler = new BeaconHandler(this, new BeaconHandler.OnBeaconSearchResult() {
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
