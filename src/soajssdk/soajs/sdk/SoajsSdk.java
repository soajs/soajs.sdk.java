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
import org.json.JSONArray;
import soajssdk.soajs.sdk.utilities.Utils;

/**
 * @author Etienne on 5/22/2017 Copyright © 2017 SOAJS. All rights reserved.
 */
public class SoajsSdk implements SoajsSdkInterface {

    private SoajsConnection connection;
    private SoajsConnection oauthConnection;

    private String extKey = "d44dfaaf1a3ba93adc6b3368816188f96134dfedec7072542eb3d84ec3e3d260f639954b8c0bc51e742c1dff3f80710e3e728edb004dce78d82d7ecd5e17e88c39fef78aa29aa2ed19ed0ca9011d75d9fc441a3c59845ebcf11f9393d5962549";
    private String authorization;

    private String access_token;
    private String refresh_token;

    /**
     * Constructor
     *
     * @param connection
     */
    public SoajsSdk(SoajsConnection connection) {
        this.connection = connection;

        // TODO handle direct service call
        oauthConnection = new SoajsConnection(false, connection.getHost(), connection.getControllerPort(), "oauth");
    }

    public boolean login(String username, String password) {
        setAuthorization();
        setTokens(username, password, null);
//        this.access_token="4b696935fa7d3d1d7ce4a374a70a1a42eaafc007"; // expired token test
//        this.refresh_token="8e70709a79f8f4226b2e84405608401b0cfea586";
        // TODO handle errors
        return true;
    }

    public boolean login(String refreshToken) {
//        System.out.println("logging in using refresh token .......");
        setAuthorization();
        setTokens(null, null, refreshToken);
        // TODO handle errors
        return true;
    }

    private void setAuthorization() {
        JSONObject authorizationResponse = getOrDelete("authorization", null, "GET", oauthConnection, true);
        JSONObject apiResponse = authorizationResponse.getJSONObject("apiResponse");
        this.authorization = apiResponse.getString("data");
    }

    private void setTokens(String username, String password, String refreshToken) {
        JSONObject body = new JSONObject();

        if (refreshToken != null) {
            body.put("refresh_token", refreshToken);
            body.put("grant_type", "refresh_token");
        } else {
            body.put("username", username);
            body.put("password", password);
            body.put("grant_type", "password");
        }

        JSONObject oauthTokenResponse = postOrPut("token", null, body, "POST", oauthConnection, true);
        boolean error = oauthTokenResponse.getBoolean("error");
        if (!error) {
            JSONObject apiResponse = oauthTokenResponse.getJSONObject("apiResponse");
            if (apiResponse.has("errors")) {
                JSONObject soajsErrors = apiResponse.getJSONObject("errors");
                JSONArray soajsErrorsDetails = soajsErrors.getJSONArray("details");
                JSONObject soajsErrorsDetailsAt0 = soajsErrorsDetails.getJSONObject(0);
                String errorMessage = soajsErrorsDetailsAt0.getString("message");
                throw new Error(errorMessage);
            } else {
                this.access_token = apiResponse.getString("access_token");
                this.refresh_token = apiResponse.getString("refresh_token");
//                System.out.println("access tokens set ...");
            }
        } else {
            System.err.println("Couldn't set tokens ...");
            throw new Error(oauthTokenResponse.getString("errorMessage"));
        }
    }

    /**
     * TODO
     * filter response : add an error flag and show error directly in case of error, if not show response
     * ex1: error: true, code :..., message: '...;
     * ex2: error: false, apiResonse: {}
     * ex3: error: true, {controller error}
     * 
     * @param response
     * @return 
     */
    private JSONObject filterResponse(JSONObject response) {
        boolean error = response.getBoolean("error");
        if (!error) { // error on controllers level
            JSONObject apiResponse = response.getJSONObject("apiResponse");
            if (apiResponse.has("errors")) { // service error
                JSONObject soajsErrors = apiResponse.getJSONObject("errors");
                JSONArray soajsErrorsDetails = soajsErrors.getJSONArray("details");
                JSONObject soajsErrorsDetailsAt0 = soajsErrorsDetails.getJSONObject(0); // will have error code and message
                soajsErrorsDetailsAt0.put("error", true); // add error flag
                return soajsErrorsDetailsAt0;
            } else {
                // TODO add error flag
                return response; // valid response
            }
        } else {
            return response; // controller's error // already have an error flag
        }
    }

    private boolean isAccessTokenExpired(JSONObject response) {
        JSONObject filteredResponse = filterResponse(response);
        boolean error = filteredResponse.getBoolean("error");
        if (error) {
            int errorCode = filteredResponse.getInt("code");
            String errorMessage = filteredResponse.getString("message");
            if (errorCode == 401 && errorMessage.equalsIgnoreCase("The access token provided has expired.")) { // expired token
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * GET api
     *
     * @param path
     * @param headerParams
     * @return
     */
    public JSONObject get(String path, JSONObject headerParams) {
        JSONObject response = getOrDelete(path, headerParams, "GET", this.connection, false);

        if (isAccessTokenExpired(response)) {
            login(refresh_token); // refresh token and retry
            return getOrDelete(path, headerParams, "GET", this.connection, false);
        } else {
            return response;
        }
    }

    /**
     * DELETE api
     *
     * @param path
     * @param headerParams
     * @return
     */
    public JSONObject delete(String path, JSONObject headerParams) {
        JSONObject response = getOrDelete(path, headerParams, "DELETE", this.connection, false);

        if (isAccessTokenExpired(response)) {
            login(refresh_token); // refresh token and retry
            return getOrDelete(path, headerParams, "DELETE", this.connection, false);
        } else {
            return response;
        }
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
        JSONObject response = postOrPut(path, headerParams, body, "POST", this.connection, false);

        if (isAccessTokenExpired(response)) {
            login(refresh_token); // refresh token and retry
            return postOrPut(path, headerParams, body, "POST", this.connection, false);
        } else {
            return response;
        }
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
        JSONObject response = postOrPut(path, headerParams, body, "PUT", this.connection, false);

        if (isAccessTokenExpired(response)) {
            login(refresh_token); // refresh token and retry
            return postOrPut(path, headerParams, body, "PUT", this.connection, false);
        } else {
            return response;
        }
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
    private JSONObject getOrDelete(String path, JSONObject headerParams, String requestMethod, SoajsConnection connection, boolean internalMode) {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = new JSONObject();
        InputStream inStream = null;

        if (headerParams == null || headerParams.length() == 0) {
            headerParams = new JSONObject();
        }

        if (!internalMode) {
            headerParams.put("access_token", this.access_token);
        }

        String header = Utils.constructHeader(headerParams);
        String urlString = connection.getBaseUrl() + path + header;

        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(connection.getConnectTimeout());
            urlConnection.setRequestProperty("key", extKey);
            urlConnection.connect();

            int status = urlConnection.getResponseCode();
            if (status == 200) {
                inStream = urlConnection.getInputStream();
            } else {
                inStream = urlConnection.getErrorStream();
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }

            object.put("error", false);
            object.put("apiResponse", (JSONObject) new JSONTokener(response).nextValue());
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
    private JSONObject postOrPut(String path, JSONObject headerParams, JSONObject body, String requestMethod, SoajsConnection connection, boolean internalMode) {
        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = new JSONObject();
        InputStream inStream = null;

        if (headerParams == null || headerParams.length() == 0) {
            headerParams = new JSONObject();
        }

        if (!internalMode) {
            headerParams.put("access_token", this.access_token);
        }

        String header = Utils.constructHeader(headerParams);
        String urlString = connection.getBaseUrl() + path + header;

        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(connection.getConnectTimeout());
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("key", extKey);

            if (internalMode) {
                urlConnection.setRequestProperty("Authorization", authorization);
            }

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(body.toString());
            out.close();

            urlConnection.connect();
            int status = urlConnection.getResponseCode();
            if (status == 200) {
                inStream = urlConnection.getInputStream();
            } else {
                inStream = urlConnection.getErrorStream();
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";

            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }

            object.put("error", false);
            object.put("apiResponse", (JSONObject) new JSONTokener(response).nextValue());
            return object;

        } catch (Exception e) {
            e.printStackTrace();
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
