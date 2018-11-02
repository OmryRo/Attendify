package wsux.attendify.app;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleFormHandler {

    private static final String TAG = "GOOGLE_FORM_HANDLER";
    private static final String FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSeaKBHeSVGBpQZeUv2VxMDjxfcEtZ0vfobn99UKDqaX_g7ICg/";
    private static final String FORM_URL_GET = String.format("%sviewform", FORM_URL);
    private static final String FORM_URL_POST = String.format("%sformResponse", FORM_URL);
    private static final String FORM_REAL_NAME_INPUT_ID = "entry.90001332";
    private static final String FORM_EMAIL_INPUT_ID = "entry.84085877";
    private static final String FORM_DEVICE_ID_INPUT_ID = "entry.2012310578";
    private static final String GOOGLE_FORM_CONFIRM_MESSAGE_CLASS = "freebirdFormviewerViewResponseConfirmContentContainer"; // blame google.
    private static final int HTTP_TIMEOUT = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";

    public static void sendGoogleForm(String name, String email, String deviceId, OnGoogleFormResponse onGoogleFormResponse) {
        new SendGoogleForm().execute(name, email, deviceId, onGoogleFormResponse);
    }

    private static Pair<Integer, String> requestGoogleForm(String formURL, Map<String, String> postParams) {

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> params : postParams.entrySet()) {
            formBodyBuilder.add(params.getKey(), params.getValue());
        }

        FormBody body = formBodyBuilder.build();

        Request request = new Request.Builder()
                .url(formURL)
                .header("User-Agent", USER_AGENT)
                .header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Charset", "UTF-8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return new Pair<>(response.code(), response.body().string());
        } catch (IOException e) {
            Log.e(TAG, "requestGoogleForm: ", e);
        }

        return null;
    }

    private static Map<String, String> getAntiBottingInfo(String formURL) {

        Document googleForm;
        try {
            URL url = new URL(formURL);
            googleForm = Jsoup.parse(url, HTTP_TIMEOUT);
        } catch (IOException e) {
            Log.e(TAG, "getAntiBottingInfo: ", e);
            return null;
        }

        // anti logging form fields from the html, if there are  more, add them here.
        Elements fieldsFvv = googleForm.getElementsByAttributeValue("name", "fvv");
        Elements fieldsDraftResponse = googleForm.getElementsByAttributeValue("name", "draftResponse");
        Elements fieldsPageHistory = googleForm.getElementsByAttributeValue("name", "pageHistory");
        Elements fieldsFbzx = googleForm.getElementsByAttributeValue("name", "fbzx");

        Map<String, String> botTraps =  new HashMap<>();
        botTraps.put("fvv", fieldsFvv.attr("value"));
        botTraps.put("draftResponse", fieldsDraftResponse.attr("value"));
        botTraps.put("pageHistory", fieldsPageHistory.attr("value"));
        botTraps.put("fbzx", fieldsFbzx.attr("value"));

        return botTraps;
    }

    private static boolean validateResponse(String output, int responseCode) {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "validateResponse: failed, response code: " + responseCode);
            return false;
        }

        Document response = Jsoup.parse(output);
        Elements confirmationElements = response.getElementsByClass(GOOGLE_FORM_CONFIRM_MESSAGE_CLASS);
        if (confirmationElements.size() == 0) {
            Log.d(TAG, "validateResponse: failed, no confirmation message: " + output);
            return false;
        }

        return true;
    }

    private static class SendGoogleForm extends AsyncTask<Object, Void, Pair<OnGoogleFormResponse, Boolean>> {

        @Override
        protected Pair<OnGoogleFormResponse, Boolean> doInBackground(Object... objects) {
            Map<String, String> params = getAntiBottingInfo(FORM_URL_GET);

            if (params == null) {
                return new Pair<>((OnGoogleFormResponse) objects[3], false);
            }

            params.put(FORM_REAL_NAME_INPUT_ID, (String) objects[0]);
            params.put(FORM_EMAIL_INPUT_ID, (String) objects[1]);
            params.put(FORM_DEVICE_ID_INPUT_ID, (String) objects[2]);

            Pair<Integer, String> response = requestGoogleForm(FORM_URL_POST, params);
            boolean success =  response != null && validateResponse(response.second, response.first);
            return new Pair<>((OnGoogleFormResponse) objects[3], success);
        }

        @Override
        protected void onPostExecute(Pair<OnGoogleFormResponse, Boolean> interfaceAndResponsePair) {
            super.onPostExecute(interfaceAndResponsePair);

            if (interfaceAndResponsePair.second) {
                interfaceAndResponsePair.first.success();
            } else {
                interfaceAndResponsePair.first.failed();
            }

        }
    }

    public interface OnGoogleFormResponse {
        void success();
        void failed();
    }

}
