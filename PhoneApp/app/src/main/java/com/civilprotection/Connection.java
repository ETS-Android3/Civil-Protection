package com.civilprotection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.preference.PreferenceManager;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Map;
import java.util.Objects;

public class Connection extends MqttAsyncClient {

    private Context context;
    // Topics to write to:
    final static String clientTopicPrefix = "civil/clients/";
    // Topics to listen to:
    final static String serverTopicPrefix = "civil/server/";
    // Preferences
    private String sessionID = "";
    private String serverIp = "";
    private int serverPort = 1883;
    private String serverUri = "";
    private int qos = 2;
    private int timeOutTime = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    private int keepAliveTime = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
    private String lastWillTopic = "";
    private String lastWillPayload = "";
    private int lastWillQos = 0;
    private boolean lastWillRetain = false;
    private boolean useAuth = false;
    private String username = "";
    private String password = "";
    private boolean ssl = false;
    // Fragment specific values
    private String pubTopic = "";
    private String subTopic = "";
    private String message = "";
    private boolean retain = false;
    private boolean autoMode = false;
    private int maxSimulationTime = 10;

    public Connection(String serverURI, String sessionId, MemoryPersistence persistence, Context context, com.civilprotection.CallbackHandler.CallBackListener listener) throws MqttException {
        super(serverURI, sessionId, persistence);
        setContext(context);
        setServerUri(serverURI);
        // Retrieve IP and port from the server URI
        String[] info = getServerUri().split("//");
        setServerIp(info[1].split(":")[0]);
        setServerPort(Integer.parseInt(info[1].split(":")[1]));
        // Initialize all the rest information
        getConnectOptions();
        setPubTopic("");
        setSubTopic("");
        setCallback(new com.civilprotection.CallbackHandler(listener));
    }

    public MqttConnectOptions getConnectOptions() {
        updateConnectionOptions();

        MqttConnectOptions connOptions = new MqttConnectOptions();
        connOptions.setServerURIs(new String[]{getServerUri()});
        connOptions.setConnectionTimeout(getTimeOutTime());
        connOptions.setKeepAliveInterval(getKeepAliveTime());
        connOptions.setHttpsHostnameVerificationEnabled(isSsl());
        if (isUseAuth() && !getUsername().isEmpty() && !getPassword().isEmpty()) {
            connOptions.setUserName(getUsername());
            connOptions.setPassword(getPassword().toCharArray());
        }
        if (!getLastWillTopic().isEmpty() && !getLastWillPayload().isEmpty()) {
            connOptions.setWill(getLastWillTopic(), getLastWillPayload().getBytes(), getLastWillQos(), isLastWillRetain());
        }
        connOptions.setAutomaticReconnect(true);
        connOptions.setMaxReconnectDelay(20);

        return connOptions;
    }

    // Stores all current preferences in a hash map of type <prefKey, prefValue>
    private void updateConnectionOptions() {
        Map<String, String> options = (Map<String, String>) PreferenceManager.getDefaultSharedPreferences(getContext()).getAll();

        // Read all preferences and initiate class members
        for (Map.Entry<String, String> entry : options.entrySet()) {
            switch (entry.getKey()) {
                case "session_id":
                    setSessionID(entry.getValue());
                    break;
                case "server_ip":
                    setServerIp(entry.getValue());
                    setServerUri("tcp://" + getServerIp() + ":" + getServerPort());
                    break;
                case "server_port":
                    setServerPort(Integer.parseInt(entry.getValue()));
                    setServerUri("tcp://" + getServerIp() + ":" + getServerPort());
                    break;
                case "qos":
                    setQos(Integer.parseInt(String.valueOf(entry.getValue())));
                    break;
                case "time_out_time":
                    setTimeOutTime(Integer.parseInt(String.valueOf(entry.getValue())));
                    break;
                case "keep_alive_time":
                    setKeepAliveTime(Integer.parseInt(String.valueOf(entry.getValue())));
                    break;
                case "last_will_topic":
                    setLastWillTopic(entry.getValue());
                    break;
                case "last_will_payload":
                    setLastWillPayload(entry.getValue());
                    break;
                case "last_will_qos":
                    setLastWillQos(Integer.parseInt(entry.getValue()));
                    break;
                case "last_will_retain":
                    setLastWillRetain(String.valueOf(entry.getValue()).equals("true"));
                    break;
                case "use_auth":
                    setUseAuth(String.valueOf(entry.getValue()).equals("true"));
                    break;
                case "username":
                    setUsername(entry.getValue());
                    break;
                case "password":
                    setPassword(entry.getValue());
                    break;
                case "ssl":
                    setSsl(String.valueOf(entry.getValue()).equals("true"));
                    break;
                default:
                    break;
            }
        }

    }

    public boolean isInternetServiceAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED ||
            Objects.requireNonNull(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public int getTimeOutTime() {
        return timeOutTime;
    }

    public void setTimeOutTime(int timeOutTime) {
        this.timeOutTime = timeOutTime;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public String getLastWillTopic() {
        return lastWillTopic;
    }

    public void setLastWillTopic(String lastWillTopic) {
        this.lastWillTopic = lastWillTopic;
    }

    public String getLastWillPayload() {
        return lastWillPayload;
    }

    public void setLastWillPayload(String lastWillPayload) {
        this.lastWillPayload = lastWillPayload;
    }

    public int getLastWillQos() {
        return lastWillQos;
    }

    public void setLastWillQos(int lastWillQos) {
        this.lastWillQos = lastWillQos;
    }

    public boolean isLastWillRetain() {
        return lastWillRetain;
    }

    public void setLastWillRetain(boolean lastWillRetain) {
        this.lastWillRetain = lastWillRetain;
    }

    public boolean isUseAuth() {
        return useAuth;
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getPubTopic() {
        return pubTopic;
    }

    public void setPubTopic(String pubTopic) {
        this.pubTopic = clientTopicPrefix + getSessionID() + "/" + pubTopic;
    }

    public String getSubTopic() {
        return subTopic;
    }

    public void setSubTopic(String subTopic) {
        this.subTopic = serverTopicPrefix + getSessionID() + "/" + subTopic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public boolean isAutoMode()  {
        return autoMode;
    }

    public void setAutoMode(boolean AutoMode)  {
        this.autoMode = AutoMode;
    }

    public int getMaxSimulationTime() {
        return maxSimulationTime;
    }

    public void setMaxSimulationTime(int maxSimulationTime) {
        this.maxSimulationTime = maxSimulationTime;
    }

}
