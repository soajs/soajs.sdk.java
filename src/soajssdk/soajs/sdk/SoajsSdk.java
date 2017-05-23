package soajssdk.soajs.sdk;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import soajssdk.soajs.sdk.utilities.Utils;

/**
 * @author Etienne on 5/22/2017 Copyright © 2017 SOAJS. All rights reserved.
 */
public class SoajsSdk implements SoajsSdkInterface {

    private SoajsConnection connection;

    /**
     * Constructor
     *
     * @param connection
     */
    public SoajsSdk(SoajsConnection connection) {
        this.connection = connection;
    }

    /**
     * GET api
     *
     * @param path
     * @param headerParams
     * @return
     */
    public JSONObject get(String path, JSONObject headerParams) {
        return getOrDelete(path, headerParams, "GET");
    }

    /**
     * DELETE api
     *
     * @param path
     * @param headerParams
     * @return
     */
    public JSONObject delete(String path, JSONObject headerParams) {
        return getOrDelete(path, headerParams, "DELETE");
    }

    /**
     * POST api
     *
     * @param path
     * @param headerParams
     * @param body
     * @return
     */
    public JSONObject post(String path, JSONObject headerParams, JSONObject body) {
        return postOrPut(path, headerParams, body, "POST");
    }

    /**
     * PUT api
     *
     * @param path
     * @param headerParams
     * @param body
     * @return
     */
    public JSONObject put(String path, JSONObject headerParams, JSONObject body) {
        return postOrPut(path, headerParams, body, "PUT");
    }

    /**
     * get or delete common function send http request, and returns response in
     * a JSON object which will have an error flag and an errorMessage or an
     * apiResponse read time out set to 10 sec connection timeout set by user
     * and defaulted to 5 seconds in connection
     *
     * @param path
     * @param headerParams
     * @param requestMethod
     * @return
     */
    private JSONObject getOrDelete(String path, JSONObject headerParams, String requestMethod) {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = new JSONObject();
        InputStream inStream = null;

        String header = Utils.constructHeader(headerParams);
        String urlString = this.connection.getBaseUrl() + path + header;

        try {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(this.connection.getConnectTimeout());
            urlConnection.connect();

            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }

            object.put("error", false);
            object.put("apiResonse", (JSONObject) new JSONTokener(response).nextValue());
        } catch (Exception e) {
            try {
                object.put("error", true);
                object.put("errorMessage", e.toString());
            } catch (Exception e2) {
                throw e2;
            }
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return object;
    }

    /**
     * post or put common function send http request, and returns response in a
     * JSON object which will have an error flag and an errorMessage or an
     * apiResponse read time out set to 10 sec connection timeout set by user
     * and defaulted to 5 seconds in connection
     *
     *
     * @param path
     * @param headerParams
     * @param body
     * @param requestMethod
     * @return
     */
    public JSONObject postOrPut(String path, JSONObject headerParams, JSONObject body, String requestMethod) {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = new JSONObject();
        InputStream inStream = null;

        String header = Utils.constructHeader(headerParams);
        String urlString = this.connection.getBaseUrl() + path + header;
        try {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(this.connection.getConnectTimeout());
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(body.toString());
            out.close();

            urlConnection.connect();
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }

            object.put("error", false);
            object.put("apiResonse", (JSONObject) new JSONTokener(response).nextValue());
            return object;

        } catch (Exception e) {
            try {
                object.put("error", true);
                object.put("errorMessage", e.toString());
            } catch (Exception e2) {
                throw e2;
            }
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return object;
    }

    /**
     * SETTORS AND GETTORS
     */
    public SoajsConnection getConnection() {
        return connection;
    }

    public void setConnection(SoajsConnection connection) {
        this.connection = connection;
    }
}