package wsux.attendify.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private final static String EMAIL_MATCH = "[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+";
    private final static String NAME_MATCH = "([A-Za-z]+|[א-ת]+)( ([A-Za-z]+|[א-ת]+))+";
    private final static String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST_CODE = 1111;
    private static final Object FOUND_LOCK = new Object();
    private static final Object SENT_LOCK = new Object();
    private View loadingScreenBackground;
    private View loadingScreenPopup;
    private TextView loadingScreenText;
    private ProgressBar loadingIV;
    private Button sendButton;
    private BeaconHandler beaconHandler;
    private boolean isFound = false;
    private boolean isSending = false;
    private Runnable sendFormPending;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        isFound = false;
        isSending = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            }
        }

        loadingScreenBackground = findViewById(R.id.loadingCover);
        loadingScreenPopup = findViewById(R.id.loadingPopup);
        loadingScreenText = findViewById(R.id.loadingText);

        beaconHandler = new BeaconHandler(this, new BeaconHandler.OnBeaconSearchResult() {
            @Override
            public void found() {
                synchronized (FOUND_LOCK) {
                    isFound = true;
                    if (sendFormPending != null) {
                        sendFormPending.run();
                        sendFormPending = null;
                    }
                }
                Log.d(TAG, "found");
                beaconHandler.stop();

            }

            @Override
            public void foundButNotInTime(String deviceName, String hourStart, String hourEnd) {

            }

            @Override
            public void notFound(int numberOfOtherDevicesInScan) {
                if (loadingScreenText != null) {
                    loadingScreenText.setText(getString(R.string.devices_found, numberOfOtherDevicesInScan));
                }
            }
        });

        final EditText emailET = findViewById(R.id.emailET);
        final EditText nameET = findViewById(R.id.fullNameET);
        sendButton = findViewById(R.id.sendBT);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!lockSending()) {
                    return;
                }

                final String email = emailET.getText().toString();
                final String name = nameET.getText().toString();

                if (!name.matches(NAME_MATCH)) {
                    popup(R.string.invalid_name);
                    unlockSending();
                    return;
                }

                if (!email.matches(EMAIL_MATCH)) {
                    popup(R.string.invalid_email);
                    unlockSending();
                    return;
                }

                synchronized (FOUND_LOCK) {
                    if (!isFound) {
                        sendFormPending = new Runnable() {
                            @Override
                            public void run() {
                                sendGoogleForm(name, email);
                            }
                        };
                        showLoadingScreen();
                        return;
                    }
                }

                sendGoogleForm(name, email);

            }
        });
    }

    private boolean lockSending() {
        synchronized (SENT_LOCK) {
            if (isSending) {
                return false;
            }

            isSending = true;
            sendButton.setEnabled(false);
            return true;
        }
    }

    private void unlockSending() {
        synchronized (SENT_LOCK) {
            isSending = false;
        }
        sendButton.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isFound) {
            beaconHandler.start();
        }
    }

    private void showLoadingScreen() {
        if (loadingScreenBackground == null || loadingScreenBackground == null) {
            return;
        }
        loadingScreenPopup.setVisibility(View.VISIBLE);
        loadingScreenBackground.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen() {
        if (loadingScreenBackground == null || loadingScreenBackground == null) {
            return;
        }
        loadingScreenPopup.setVisibility(View.GONE);
        loadingScreenBackground.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconHandler.stop();
    }

    private void sendGoogleForm(final String name, final String email) {
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        GoogleFormHandler.sendGoogleForm(name, email, androidID, new GoogleFormHandler.OnGoogleFormResponse() {
            @Override
            public void success() {
                popupSuccess();
            }

            @Override
            public void failed() {
                hideLoadingScreen();
                popup(R.string.attendence_error);
                unlockSending();
            }
        });
    }

    private void popupSuccess() {
        View confirmMessage = findViewById(R.id.confirmMessagePopup);
        confirmMessage.setVisibility(View.VISIBLE);

        View confirmButton = findViewById(R.id.confirmMessagePopupConfirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void popup(int message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton("אוקי", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }
}
