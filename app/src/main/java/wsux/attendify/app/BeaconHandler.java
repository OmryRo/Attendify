package wsux.attendify.app;

import android.content.Context;

public class BeaconHandler {

    public BeaconHandler(Context context, OnBeaconSearchResult onBeaconSearchResult) {

    }

    public void start() {}

    public void stop() {}

    public interface OnBeaconSearchResult {
        void found();
        void foundButNotInTime(String deviceName, String hourStart, String hourEnd);
        void notFound(int numberOfOtherDevicesInScan);
    }

}
