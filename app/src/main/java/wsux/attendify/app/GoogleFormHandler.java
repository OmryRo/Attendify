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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HttpsURLConnection;

public class GoogleFormHandler {

    private static final String TAG = "GOOGLE_FORM_HANDLER";
    private static final String FORM_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSeaKBHeSVGBpQZeUv2VxMDjxfcEtZ0vfobn99UKDqaX_g7ICg/viewform";
    private static final String FORM_REAL_NAME_INPUT_ID = "entry.90001332";
    private static final String FORM_EMAIL_INPUT_ID = "entry.84085877";
    private static final String FORM_DEVICE_ID_INPUT_ID = "entry.2012310578";

    public static void sendGoogleForm(String name, String email, long deviceId, OnGoogleFormResponse onGoogleFormResponse) {
        new SendGoogleForm().execute(name, email, String.valueOf(deviceId), onGoogleFormResponse);
    }

    private static Pair<Integer, String> requestGoogleForm(String formURL, Map<String, String> postParams) {
        int responseCode;
        StringBuffer response = new StringBuffer();

        try {
            URL url = new URL(FORM_URL);
            URLConnection connection = url.openConnection();
            HttpsURLConnection https = (HttpsURLConnection) connection;

            if (postParams != null) {
                https.setRequestMethod("POST");
                https.setDoOutput(true);

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : postParams.entrySet())
                    sb.append(String.format(
                            "%s=%s&",
                            URLEncoder.encode(entry.getKey(), "UTF-8"),
                            URLEncoder.encode(entry.getValue(), "UTF-8")
                    ));
                sb.deleteCharAt(sb.length() - 1);
                byte[] out = sb.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                https.setFixedLengthStreamingMode(length);
                https.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                https.connect();

                OutputStream os = https.getOutputStream();
                os.write(out);
                os.flush();
                os.close();
            } else {
                https.setRequestMethod("GET");
            }

            responseCode = https.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException(String.format("Bad http response %s", responseCode));
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d(TAG, String.format("sendGoogleForm: %s", response));

        } catch (IOException e) {
            Log.e(TAG, "sendGoogleForm: ", e);
            return null;
        }

        return new Pair<>(responseCode, response.toString());
    }

    private static Map<String, String> getAntiBottingInfo(String formURL) {

        Pair<Integer, String> response = requestGoogleForm(formURL, null);

        if (response == null) {
            return null;
        }

        Document googleForm = Jsoup.parse(response.second);
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
        return true;
    }

    private static class SendGoogleForm extends AsyncTask<Object, Void, Pair<OnGoogleFormResponse, Boolean>> {

        @Override
        protected Pair<OnGoogleFormResponse, Boolean> doInBackground(Object... objects) {
            Map<String, String> params = getAntiBottingInfo(FORM_URL);

            if (params == null) {
                return new Pair<>((OnGoogleFormResponse) objects[3], false);
            }

            params.put(FORM_REAL_NAME_INPUT_ID, (String) objects[0]);
            params.put(FORM_EMAIL_INPUT_ID, (String) objects[1]);
            params.put(FORM_DEVICE_ID_INPUT_ID, (String) objects[2]);

            Pair<Integer, String> response = requestGoogleForm(FORM_URL, params);
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
