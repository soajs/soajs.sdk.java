package soajssdk.soajs.sdk.utilities;

import java.util.Iterator;
import java.util.Vector;
import org.json.JSONObject;

/**
 * @author Etienne on 5/22/2017 Copyright © 2017 SOAJS. All rights reserved.
 */
public class Utils {

    /**
     * construct base url using controller ex:
     * http://192.168.2.23:4000/soajsTestSdkDel/
     *
     * @param secureProtocol
     * @param host
     * @param controllerPort
     * @param serviceName
     * @return
     */
    public static String fetchBaseUrlUsingController(boolean secureProtocol, String host, String controllerPort, String serviceName) {
        String baseUrl;

        if (secureProtocol) {
            baseUrl = "https://";
        } else {
            baseUrl = "http://";
        }

        if (host == null || host.isEmpty()) {
            throw new Error("Invalid Host");
        }

        if (controllerPort == null || controllerPort.isEmpty()) {
            throw new Error("Invalid Controller Port");
        }

        if (serviceName == null || serviceName.isEmpty()) {
            throw new Error("Invalid Service Name");
        }

        baseUrl += host + ":" + controllerPort + "/" + serviceName + "/";

        return baseUrl;
    }

    /**
     * construct base url without controller ex: http://192.168.2.23/4096/
     *
     *
     * @param secureProtocol
     * @param host
     * @param port
     * @return
     */
    public static String fetchBaseUrl(boolean secureProtocol, String host, String port) {
        String baseUrl;

        if (host == null || host.isEmpty()) {
            throw new Error("Invalid Host");
        }

        if (port == null || port.isEmpty()) {
            throw new Error("Invalid Port");
        }

        if (secureProtocol) {
            baseUrl = "https://";
        } else {
            baseUrl = "http://";
        }

        baseUrl += host + ":" + port + "/";

        return baseUrl;
    }

    /**
     * construct query header from a JSON object
     *
     * @param headerParams
     * @return
     */
    public static String constructHeader(JSONObject headerParams) {
        String header = "";

        if (headerParams == null || headerParams.length() == 0) {
            return header;
        }

        Vector<String> allKeysAndValues = getAllKeysAndValuesFromJsonObject(headerParams);

        // it has at least 1
        header = "?" + allKeysAndValues.get(0);

        // more then 1
        for (int i = 1; i < allKeysAndValues.size(); i++) {
            header += "&" + allKeysAndValues.get(i);
        }

        return header;
    }

    /**
     * Get All Keys And Values from a JSON object and return them in a vector of
     * strings
     *
     * @param jObject
     * @return
     */
    private static Vector<String> getAllKeysAndValuesFromJsonObject(JSONObject jObject) {
        Vector<String> allKeys = new Vector<String>();

        try {
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String keyAndValue = key + "=" + jObject.get(key);
                allKeys.add(keyAndValue);
            }
        } catch (Exception e) {
            throw e;
        }
        return allKeys;
    }
}