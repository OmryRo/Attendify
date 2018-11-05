package wsux.attendify.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfirmIdentityActivity extends Activity {

    private final static String EMAIL_MATCH = "[a-zA-Z0-9_\\.]+@[a-zA-Z0-9_\\.]+";
    private final static String NAME_MATCH = "([A-Za-z]+|[א-ת]+)( ([A-Za-z]+|[א-ת]+))+";

    private final static Object LOCK = new Object();
    private boolean isSending = false;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_identity);

        final EditText emailET = findViewById(R.id.emailET);
        final EditText nameET = findViewById(R.id.fullNameET);
        button = findViewById(R.id.sendBT);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!lockSending()) {
                    return;
                }

                String email = emailET.getText().toString().trim();
                String name = nameET.getText().toString().trim();

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

                String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                GoogleFormHandler.sendGoogleForm(name, email, androidID, new GoogleFormHandler.OnGoogleFormResponse() {
                    @Override
                    public void success() {
                        popup(R.string.attendence_confirmed);
                        unlockSending();
                    }

                    @Override
                    public void failed() {
                        popup(R.string.attendence_error);
                        unlockSending();
                    }
                });

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

    private boolean lockSending() {
        synchronized (LOCK) {
            if (isSending) {
                return false;
            }

            if (button != null) {
                button.setEnabled(false);
            }

            isSending = true;
            return true;
        }
    }

    private void unlockSending() {
        synchronized (LOCK) {
            isSending = false;

            if (button != null) {
                button.setEnabled(true);
            }
        }
    }
}
