package wsux.attendify.app;

import android.content.Context;

public class GoogleLoginHandler {

    public GoogleLoginHandler(Context context) {

    }

    public boolean isLoggedIn() {
        return true;
    }

    public boolean attemptLogin() {
        return true;
    }

    public String getRealName() {
        return "Palony Almony";
    }

    public String getEmail() {
        return "palony.almony@gmail.com";
    }

}
