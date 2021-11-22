package com.civilprotectionsensor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;

import com.civilprotectionsensor.ui.fragments.FragmentUvSensor;
import com.civilprotectionsensor.ui.fragments.FragmentTempSensor;
import com.civilprotectionsensor.ui.fragments.FragmentSmokeSensor;
import com.civilprotectionsensor.ui.fragments.FragmentGasSensor;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.civilprotectionsensor.ui.PagerAdapter;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private CallbackHandler.CallBackListener listener;
    PagerAdapter adapter;

    private Connection connection;

    private FloatingActionButton fab;

    List<Sensor> sensors = null;
    private final static String sensorConfigFile = "sensors.json";

    private static final int NEW_SENSOR_REQUEST_CODE = 1;
    private static final int LOCATION_PROVIDER_CODE = 2;

    private final AtomicBoolean stopRunnable = new AtomicBoolean(true);
    private final AtomicBoolean gpsPermissionGranted = new AtomicBoolean(false);
    private final AtomicBoolean gpsReady = new AtomicBoolean(false);
    private FusedLocationProviderClient locationProvider;
    private static final String[][] MANUAL_COORDS = {
        {"37.96809452684323", "23.76630586399502"},
        {"37.96799937191987", "23.766603589104385"},
        {"37.967779456380754", "23.767174897611685"},
        {"37.96790421900921", "23.76626294807113"}
    };
    private String latitude = "";
    private String longitude = "";
    BatteryManager batteryManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpTheme();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createTabs();

        setUpConnectionFABListener();

        // Create the connection and register the callback listener
        String serverUri = "tcp://" + getResources().getString(R.string.defaultServerIp) + ":" + getResources().getString(R.string.defaultServerPort);
        setupConnectionListener();
        try {
            connection = new Connection(serverUri, readStringSetting("session_id"), new MemoryPersistence(), this, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferencesMenu:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.createSensorMenu:
                startActivityForResult(new Intent(this, SensorCreateActivity.class), NEW_SENSOR_REQUEST_CODE);
                return true;
            case R.id.resetSettingsMenu:
                String sessionId = readStringSetting("session_id");
                boolean isDarkThemeOn = readBooleanSetting("dark_theme");
                getDefaultSharedPreferences(this).edit().clear().apply();
                // Restore the session ID
                setStringSetting("session_id", sessionId);
                // Restore the selected theme
                setBooleanSetting("dark_theme", isDarkThemeOn);
                // Delete all extra sensors
                if (sensors.size() > 2) sensors.subList(2, sensors.size()).clear();
                refreshUi();
                return true;
            case R.id.exitMenu:
                Utils.storeJsonContent(this, sensorConfigFile, sensors);
                showExitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PROVIDER_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpLocationProvider();
            } else {
                stopRunnable.set(true);
                gpsPermissionGranted.set(false);
                Toast.makeText(this, "You need to give location permission to enable Auto Coordinates mode", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_SENSOR_REQUEST_CODE && resultCode == RESULT_OK) {
            Sensor s = new Sensor(data.getStringExtra("type"), Float.parseFloat(data.getStringExtra("min")),
                    Float.parseFloat(data.getStringExtra("max")), Float.parseFloat(data.getStringExtra("current")));
            sensors.add(s);
            refreshUi();
        }
    }

    private void createTabs() {
        adapter = new PagerAdapter(getSupportFragmentManager());
        if (readStringSetting("session_id").equals("-1")) {
            // If this is the first run of the app, load the default sensors
            setStringSetting("session_id", String.valueOf(new Random().nextInt(10000)));
            sensors = Utils.getJsonContent(this, sensorConfigFile, true);
            Utils.storeJsonContent(this, sensorConfigFile, sensors);
        } else {
            // Otherwise load the sensors the app has stored
            sensors = Utils.getJsonContent(this, sensorConfigFile, false);
        }
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(sensors.size() - 1);
        TabLayout tabs = findViewById(R.id.tabs);
        int smokeCounter = 0, gasCounter = 0, tempCounter = 0, uvCounter = 0;
        for (int sensor = 0; sensor < sensors.size(); sensor++) {
            Fragment fragment = null;
            Bundle bundle = new Bundle();
            bundle.putFloatArray("args", new float[]{sensors.get(sensor).getMin(), sensors.get(sensor).getMax(), sensors.get(sensor).getCurrent()});
            switch (sensors.get(sensor).getType()) {
                case "smoke":
                    fragment = new FragmentSmokeSensor();
                    adapter.addFragment(fragment, getResources().getString(R.string.tab_smoke_title) + " " + ++smokeCounter);
                    break;
                case "gas":
                    fragment = new FragmentGasSensor();
                    adapter.addFragment(fragment, getResources().getString(R.string.tab_gas_title) + " " + ++gasCounter);
                    break;
                case "temp":
                    fragment = new FragmentTempSensor();
                    adapter.addFragment(fragment, getResources().getString(R.string.tab_temp_title) + " " + ++tempCounter);
                    break;
                case "uv":
                    fragment = new FragmentUvSensor();
                    adapter.addFragment(fragment, getResources().getString(R.string.tab_uv_title) + " " + ++uvCounter);
                    break;
                default:
                    break;
            }
            if (fragment != null) fragment.setArguments(bundle);
        }
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
    }

    private void setupConnectionListener() {
        listener = new CallbackHandler.CallBackListener() {
            @Override
            public void onMessageArrived(String topic, MqttMessage message) {
                handler.post(() -> handleMessageArrived(topic, message));
            }

            @Override
            public void onConnectionLost() {
                handler.post(() -> handleConnectionLost());
            }

            @Override
            public void onReconnected() {
                handler.post(() -> handleReconnected());
            }
        };
    }

    private void toggleConnect() {
        if (connection.isConnected()) disconnect();
        else connect();
    }

    private void connect() {
        try {
            IMqttToken token = connection.connect(connection.getConnectOptions());
            token.waitForCompletion(1000);
            onConnectSuccess();
            // Subscribe by default to all available topic with QoS Exactly Once
            subscribe("#", 2);
        } catch (MqttException e) {
            onConnectFailure();
            e.printStackTrace();
        }
    }

    private void publish(String topic, String payload, int qos, boolean retain) {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        message.setRetained(retain);
        if (connection.isConnected()) {
            turnOnFAB();
            try {
                connection.publish(connection.getPubTopic(), message).waitForCompletion();
                System.out.println("Successfully published \"" + payload + "\" to " + connection.getPubTopic());
                Toast.makeText(this, "Published \"" + payload + "\" to " + topic, Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                System.err.println("Failed to publish \"" + payload + "\" to " + connection.getPubTopic());
                e.printStackTrace();
            }
        } else {
            turnOffFAB();
            Toast.makeText(this, "You need to connect first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void subscribe(String topic, int qos) {
        if (connection.isConnected()) {
            turnOnFAB();
            try {
                connection.setSubTopic(topic);
                connection.subscribe(connection.getSubTopic(), qos);
                System.out.println("Successfully subscribed to " + connection.getSubTopic());
                Toast.makeText(this, "Subscribed to " + topic, Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                System.err.println("Failed to subscribe to " + connection.getSubTopic());
                e.printStackTrace();
            }
        } else {
            turnOffFAB();
            Toast.makeText(this, "You need to connect first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void unsubscribe(String topic) {
        if (connection.isConnected()) {
            turnOnFAB();
            try {
                connection.unsubscribe(topic);
                System.out.println("Successfully unsubscribed from " + topic);
            } catch (MqttException e) {
                System.err.println("Failed to unsubscribe from " + topic);
                e.printStackTrace();
            }
        } else {
            turnOffFAB();
            Toast.makeText(this, "You need to connect first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void disconnect() {
        try {
            // Unsubscribe from all available topics
            unsubscribe("civil/server-sensors/" + connection.getSessionID() + "/#");
            IMqttToken token = connection.disconnect();
            token.waitForCompletion(1000);
            onDisconnectSuccess();
        } catch (MqttException e) {
            onDisconnectFailure();
            e.printStackTrace();
        }
    }

    // Returns a map of all sensors on the current fragments along with their status
    HashMap<Sensor, Boolean> getCurrentSensors() {
        HashMap<Sensor, Boolean> currentSensors = new HashMap<>();
        for (int sensor = 0; sensor < sensors.size(); sensor++) {
            CheckBox checkBox = null;
            Slider slider = null;
            boolean isActive = false;
            // Double-check if the fragment of this sensor is attached
            if (adapter.getItem(sensor).isAdded()) {
                switch (sensors.get(sensor).getType()) {
                    case "smoke":
                        checkBox = adapter.getItem(sensor).requireView().findViewById(R.id.smokeSensorActiveCheckBox);
                        slider = adapter.getItem(sensor).requireView().findViewById(R.id.smokeSensorSlider);
                        break;
                    case "gas":
                        checkBox = adapter.getItem(sensor).requireView().findViewById(R.id.gasSensorActiveCheckBox);
                        slider = adapter.getItem(sensor).requireView().findViewById(R.id.gasSensorSlider);
                        break;
                    case "temp":
                        checkBox = adapter.getItem(sensor).requireView().findViewById(R.id.tempSensorActiveCheckBox);
                        slider = adapter.getItem(sensor).requireView().findViewById(R.id.tempSensorSlider);
                        break;
                    case "uv":
                        checkBox = adapter.getItem(sensor).requireView().findViewById(R.id.uvSensorActiveCheckBox);
                        slider = adapter.getItem(sensor).requireView().findViewById(R.id.uvSensorSlider);
                        break;
                    default:
                        break;
                }
            }
            if (checkBox != null) isActive = checkBox.isChecked();
            if (slider != null) sensors.get(sensor).setCurrent(slider.getValue());
            if (checkBox != null && slider != null) currentSensors.put(sensors.get(sensor), isActive);
        }
        return currentSensors;
    }

    private void onConnectSuccess() {
        turnOnFAB();
        System.out.println("Successfully connected to " + connection.getServerUri());
        Toast.makeText(this, "Connected to " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
        // Check which location mode is selected
        if (readBooleanSetting("auto_coords")) {
            // Auto mode connection - Pick coordinates from GPS sensor
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PROVIDER_CODE);
            } else {
                setUpLocationProvider();
            }
        } else {
            // Manual mode connection - Pick coordinates from preferences
            switch (Integer.parseInt(readStringSetting("location"))) {
                case 0:
                    latitude = MANUAL_COORDS[0][0];
                    longitude = MANUAL_COORDS[0][1];
                    break;
                case 1:
                    latitude = MANUAL_COORDS[1][0];
                    longitude = MANUAL_COORDS[1][1];
                    break;
                case 2:
                    latitude = MANUAL_COORDS[2][0];
                    longitude = MANUAL_COORDS[2][1];
                    break;
                case 3:
                    latitude = MANUAL_COORDS[3][0];
                    longitude = MANUAL_COORDS[3][1];
                    break;
            }
            SensorRunnable runnable = new SensorRunnable();
            new Thread(runnable).start();
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpLocationProvider() {
        locationProvider.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                gpsReady.set(true);
                gpsPermissionGranted.set(true);
                SensorRunnable runnable = new SensorRunnable();
                new Thread(runnable).start();
            }
        });
    }

    private void onConnectFailure() {
        turnOffFAB();
        System.err.println("Failed to connect to " + connection.getServerUri());
        if (connection.isInternetServiceAvailable())
            Toast.makeText(this, "Failed to connect. Please check your IP/port settings and retry!", Toast.LENGTH_LONG).show();
        else Toast.makeText(this, "Failed to connect. Please check your internet connection and retry!", Toast.LENGTH_SHORT).show();
    }

    private void onDisconnectSuccess() {
        stopRunnable.set(true);
        turnOffFAB();
        System.out.println("Successfully disconnected from " + connection.getServerUri());
        Toast.makeText(this, "Disconnected from " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    private void onDisconnectFailure() {
        turnOnFAB();
        System.out.println("Failed to disconnect from " + connection.getServerUri());
        Toast.makeText(this, "Failed to disconnect from " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    private void handleMessageArrived(String topic, MqttMessage message) {
        System.out.println("Received " + message + " in " + topic);
        Toast.makeText(connection.getContext(), "Received " + message + " in " + topic, Toast.LENGTH_SHORT).show();
    }

    private void handleConnectionLost() {
        turnOffFAB();
        System.out.println("Connection was lost!");
        Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
    }

    private void handleReconnected() {
        turnOnFAB();
        System.out.println("Reconnected Successfully!");
        Toast.makeText(this, "Reconnected Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void setUpTheme() {
        if (readBooleanSetting("dark_theme")) {
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpConnectionFABListener() {
        fab = findViewById(R.id.fab);
        turnOffFAB();
        fab.setOnClickListener(v -> toggleConnect());
    }

    private void turnOnFAB() {
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal_700)));
        fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tethering_on, null));
    }

    private void turnOffFAB() {
        fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.yellow_200)));
        fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_tethering_off, null));
    }

    private void refreshUi() {
        Utils.storeJsonContent(this, sensorConfigFile, sensors);
        finish();
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    // Retrieves value of string-type setting "key"
    public String readStringSetting(String key) {
        return getDefaultSharedPreferences(this).getString(key, "-1");
    }

    // Retrieves value of int-type setting "key"
    public Integer readIntSetting(String key) {
        return getDefaultSharedPreferences(this).getInt(key, -1);
    }

    // Retrieves value of switch-type setting "key"
    public Boolean readBooleanSetting(String key) {
        return getDefaultSharedPreferences(this).getBoolean(key, false);
    }

    // Sets the "key" string setting to the desired "value"
    public void setStringSetting(String key, String value) {
        getDefaultSharedPreferences(this).edit().putString(key, value).apply();
    }

    // Sets the "key" int setting to the desired "value"
    public void setIntSetting(String key, Integer value) {
        getDefaultSharedPreferences(this).edit().putInt(key, value).apply();
    }

    // Sets the "key" switch setting to the desired "value"
    public void setBooleanSetting(String key, Boolean value) {
        getDefaultSharedPreferences(this).edit().putBoolean(key, value).apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String getBatteryLevel() {
        return String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
    }

    private void showExitDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to leave?");
        builder.setMessage("");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> finish());
        builder.setNegativeButton("Not really", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    class SensorRunnable implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            stopRunnable.set(false);
            ScheduledExecutorService service = Executors.newScheduledThreadPool(0);
            ScheduledFuture<?> serviceHandler = service.scheduleAtFixedRate(() -> {
                if (stopRunnable.get()) {
                    service.shutdownNow();
                } else {
                    if (!readBooleanSetting("auto_coords") || (gpsPermissionGranted.get() && gpsReady.get())) {
                        HashMap<Sensor, Boolean> sensors = getCurrentSensors();
                        StringBuilder payload = new StringBuilder();
                        payload.append(latitude).append(";").append(longitude).append(";").append(getBatteryLevel()).append(";");
                        for (Map.Entry<Sensor, Boolean> entry : sensors.entrySet()) {
                            if (entry.getValue().toString().equals("true"))
                                payload.append(entry.getKey().getType()).append(";").append(entry.getKey().getCurrent()).append(";");
                        }
                        connection.setPubTopic("data");
                        connection.setMessage(payload.toString().substring(0, payload.toString().length() - 1));
                        // Assign job to Main thread
                        handler.post(() -> publish(connection.getPubTopic(), connection.getMessage(), connection.getQos(), connection.isRetain()));
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        }

    }

}