package soajssdk.soajs.sdk;

import soajssdk.soajs.sdk.utilities.Utils;

/**
 * @author Etienne on 5/22/2017 Copyright © 2017 SOAJS. All rights reserved.
 */
public class SoajsConnection {

    private int connectTimeout = 5000; // connection timeout in milliseconds
    private boolean secureProtocol; // http or https
    private String host; // host ip
    private String port; // service direct port
    private String controllerPort; // controller's port
    private String serviceName; // service name
    private String baseUrl; // base URL fetched
    private boolean usingController;

    /**
     * Constructor: initialize the connection through controller
     *
     * @param secureProtocol
     * @param host
     * @param controllerPort
     * @param serviceName
     */
    public SoajsConnection(boolean secureProtocol, String host, String controllerPort, String serviceName) {
        this.host = host;
        this.controllerPort = controllerPort;
        this.serviceName = serviceName;
        this.secureProtocol = secureProtocol;
        this.usingController = true;

        fetchBaseUrl();
    }

    /**
     * Constructor: initialize the connection directly
     *
     * @param host
     * @param port
     * @param secureProtocol
     */
    public SoajsConnection(boolean secureProtocol, String host, String port) {
        this.host = host;
        this.port = port;
        this.secureProtocol = secureProtocol;
        this.usingController = false;

        fetchBaseUrl();
    }

    private void fetchBaseUrl() {
        if (usingController) {
            this.baseUrl = Utils.fetchBaseUrlUsingController(secureProtocol, host, controllerPort, serviceName);
        } else {
            this.baseUrl = Utils.fetchBaseUrl(secureProtocol, host, port);
        }
    }

    @Override
    public String toString() {
        return this.baseUrl;
    }

    /**
     * SETTORS AND GETTORS
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean isSecureProtocol() {
        return secureProtocol;
    }

    public void setSecureProtocol(boolean secureProtocol) {
        this.secureProtocol = secureProtocol;
        fetchBaseUrl();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        fetchBaseUrl();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
        fetchBaseUrl();
    }

    public String getControllerPort() {
        return controllerPort;
    }

    public void setControllerPort(String controllerPort) {
        this.controllerPort = controllerPort;
        fetchBaseUrl();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        fetchBaseUrl();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        fetchBaseUrl();
    }

    public boolean isUsingController() {
        return usingController;
    }

    public void setUsingController(boolean usingController) {
        this.usingController = usingController;
    }
}
