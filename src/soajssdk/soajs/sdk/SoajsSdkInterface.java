package soajssdk.soajs.sdk;

import org.json.JSONObject;

/**
 * @author Etienne on 5/22/2017 Copyright © 2017 SOAJS. All rights reserved.
 */
public interface SoajsSdkInterface {

    public JSONObject get(String path, JSONObject headerParams);

    public JSONObject delete(String path, JSONObject headerParams);

    public JSONObject post(String path, JSONObject headerParams, JSONObject body);

    public JSONObject put(String path, JSONObject headerParams, JSONObject body);
}