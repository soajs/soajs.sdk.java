package soajssdk;

import org.json.JSONObject;
import soajssdk.soajs.sdk.SoajsSdk;
import soajssdk.soajs.sdk.SoajsConnection;
import soajssdk.soajs.sdk.SoajsSdkInterface;

/**
 * @author Etienne on 5/22/2017
 * Copyright © 2017 SOAJS. All rights reserved.
 */
public class SoajsSdkTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Welcome to the SOAJS SDK tester ...");
        
        /*
        
        if :
            no host
            no port
            no controller
            or no service name
        Runtime Error ---> Exception in thread "main" java.lang.Error: Controller's port not found!
        
        ------------------------
        if the controller is off
        ------------------------
        ---> java.net.ConnectException: Connection refused
        
        -------------------------------------------------------------
        if the controller is ok and reachable, yet the service is off
        -------------------------------------------------------------
        --->
        {"result":false,"errors":{"codes":[133],"details":[{"code":133,"message":"The service you are trying to reach is not reachable at this moment."}]}}
        
        -------------------------------------------------------
        if the controller and the service are ok and responding
        -------------------------------------------------------
        ---> Welcome to the SOAJS SDK tester ...
        {"result":true,"data":{"returningTheDataSentInGet":"hp1 - hp2"}}
        {"result":true,"data":{"returningTheDataSentInDelete":"hp1 - hp2"}}
        {"result":true,"data":{"returningTheDataSentInPost":"hp1 - hp2 - bp1 - bp2"}}
        {"result":true,"data":{"returningTheDataSentInPut":"hp1 - hp2 - bp1 - bp2"}}
        
        */

        // You can initialize your soajs connection using the host and the direct port of the service
//        String host = "192.168.5.118";
//        String port = "4098";
//        Connection cnx = new Connection(false, host, port);
//        ApiInterface soajsApi = new Api(cnx);

        // or your can initialize it using the controller port and the service name
        String host = "192.168.5.108";
        String controllerPort = "4000";
        String serviceName = "project";

        SoajsConnection cnx = new SoajsConnection(false, host, controllerPort, serviceName);
        SoajsSdkInterface soajsSdk = new SoajsSdk(cnx);
        // now soajs sdk is ready to send requests and to get responses
        
        try {
            // set your headers
            JSONObject headers = new JSONObject();
            headers.put("headerParam1", "hp1");
            headers.put("headerParam2", "hp2");

            // DELETE example
            JSONObject outputDelete = soajsSdk.delete("soajsTestSdkDel", headers);

            // GET example
            JSONObject outputGet = soajsSdk.get("soajsTestSdkGet", headers);

            // set your body
            JSONObject postBody = new JSONObject();
            postBody.put("bodyParam1", "bp1");
            postBody.put("bodyParam2", "bp2");

            // POST example
            JSONObject outputPost = soajsSdk.post("soajsTestSdkPost", headers, postBody);

            // PUT example
            JSONObject outputPut = soajsSdk.put("soajsTestSdkPut", headers, postBody);

            viewOutput(outputGet);
            viewOutput(outputDelete);
            viewOutput(outputPost);
            viewOutput(outputPut);

        } catch (Exception e) {
            System.err.println("Error thrown : ");
            e.printStackTrace();
        }
    }
    
    private static void viewOutput(JSONObject output) {
        if (output.getBoolean("error")) {
            System.out.println(output.getString("errorMessage"));
        } else {
            System.out.println(output.getJSONObject("apiResonse").toString());
        }
    }
}
