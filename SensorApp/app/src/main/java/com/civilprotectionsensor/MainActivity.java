package com.civilprotectionsensor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import com.civilprotectionsensor.ui.viewmodels.ItemViewModel;
import com.civilprotectionsensor.ui.fragments.FragmentData;
import com.civilprotectionsensor.ui.fragments.FragmentPublish;
import com.civilprotectionsensor.ui.fragments.FragmentSubscribe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.civilprotectionsensor.ui.SectionsPagerAdapter;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements FragmentPublish.OnPublishListener, FragmentSubscribe.OnSubscribeListener, FragmentData.OnSimulationListener {

    private ItemViewModel viewModel;
    private Handler handler;
    private CallbackHandler.CallBackListener listener;

    int sessionID = -1;
    private Connection connection;

    String simulationFilePath;
    private volatile boolean stopSimulation;

    private FloatingActionButton fab;

    List<Sensor> sensors = null;
    private final static String sensorConfigFile = "sensors.json";

    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int NEW_SENSOR_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpTheme();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createTabs();
        registerFragmentObservers();

        setUpConnectionFABListener();

        // If this is the first run of the app
        if (this.sessionID == -1) {
            this.sessionID = new Random().nextInt(10000);
            setStringSetting("session_id", String.valueOf(sessionID));
        }

        // Create the connection and register the callback listener
        String serverUri = "tcp://" + getResources().getString(R.string.defaultServerIp) + ":" + getResources().getString(R.string.defaultServerPort);
        setupConnectionListener();
        try {
            connection = new Connection(serverUri, String.valueOf(this.sessionID), new MemoryPersistence(), this, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.simulationFilePath = "";
        viewModel.setSimulationFilePath(getResources().getString(R.string.simulationPathTextView));

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
                getDefaultSharedPreferences(this).edit().clear().apply();
                // Keep the same session ID
                setStringSetting("session_id", String.valueOf(this.sessionID));
                // Delete all extra sensors
                if (sensors.size() > 2) sensors.subList(2, sensors.size()).clear();
                return true;
            case R.id.exitMenu:
                showExitDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFilePickerDialog();
            } else {
                Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case FILE_PICKER_REQUEST_CODE:
                simulationFilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                assert simulationFilePath != null;
                viewModel.setSimulationFilePath(simulationFilePath.substring(simulationFilePath.lastIndexOf("/") + 1));
                break;
            case NEW_SENSOR_REQUEST_CODE:
                Sensor s = new Sensor(data.getStringExtra("type"), Float.parseFloat(data.getStringExtra("min")),
                        Float.parseFloat(data.getStringExtra("max")), Float.parseFloat(data.getStringExtra("current")));
                sensors.add(s);
                for (int i = 0; i < sensors.size(); i++) System.out.println(sensors.get(i));
                Utils.storeJsonContent(this, sensorConfigFile, sensors);
                sensors = Utils.getJsonContent(this, sensorConfigFile);
                for (int i = 0; i < sensors.size(); i++) System.out.println(sensors.get(i));
                break;
            default:
                break;
        }
    }

    private void createTabs() {
        // Read the configuration file to create all sensor fragments
        sensors = Utils.getJsonContent(this, sensorConfigFile);
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabs = findViewById(R.id.tabs);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        adapter.addFragment(new FragmentPublish(), getResources().getString(R.string.tab_1_title));
        adapter.addFragment(new FragmentSubscribe(), getResources().getString(R.string.tab_2_title));
        adapter.addFragment(new FragmentData(), getResources().getString(R.string.tab_3_title));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
    }

    // Sets up observers to inspect all fragment information to make any request to the server
    private void registerFragmentObservers() {
        // Publish fields
        viewModel.getPublishTopic().observe(this, topic -> connection.setPubTopic(topic));
        viewModel.getPublishMessage().observe(this, message -> connection.setMessage(message));
        viewModel.getPublishQos().observe(this, qos -> connection.setQos(qos));
        viewModel.getPublishRetain().observe(this, retain -> connection.setRetain(retain));
        // Subscribe fields
        viewModel.getSubscribeTopic().observe(this, topic -> connection.setSubTopic(topic));
        viewModel.getSubscribeQos().observe(this, qos -> connection.setQos(qos));
        // Simulation fields
        viewModel.getSimulationQos().observe(this, qos -> connection.setQos(qos));
        viewModel.getSimulationTimeOut().observe(this, time -> connection.setMaxSimulationTime(Integer.parseInt(time)));
        viewModel.getSimulationRetain().observe(this, retain -> connection.setRetain(retain));
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
                connection.publish(topic, message).waitForCompletion();
                System.out.println("Successfully published \"" + payload + "\" to " + topic);
                Toast.makeText(this, "Published \"" + payload + "\" to " + topic, Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                System.err.println("Failed to publish \"" + payload + "\" to " + topic);
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
                connection.subscribe(topic, qos);
                System.out.println("Successfully subscribed to " + topic);
                Toast.makeText(this, "Subscribed to " + topic, Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                System.err.println("Failed to subscribe to " + topic);
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
            unsubscribe("civil/server/" + connection.getSessionID() + "/#");
            IMqttToken token = connection.disconnect();
            token.waitForCompletion(1000);
            onDisconnectSuccess();
        } catch (MqttException e) {
            onDisconnectFailure();
            e.printStackTrace();
        }
    }

    @Override
    public void onPublishPressed() {
        if (connection.getPubTopic().substring(connection.getPubTopic().lastIndexOf("/") + 1).isEmpty())
            Toast.makeText(this, "You need to specify the topic!", Toast.LENGTH_SHORT).show();
        else if (connection.getMessage().isEmpty())
            Toast.makeText(this, "Fill out a message first!", Toast.LENGTH_SHORT).show();
        else publish(connection.getPubTopic(), connection.getMessage(), connection.getQos(), connection.isRetain());
    }

    @Override
    public void onSubscribePressed() {
        if (connection.getSubTopic().substring(connection.getSubTopic().lastIndexOf("/") + 1).isEmpty())
            Toast.makeText(this, "You need to specify the topic!", Toast.LENGTH_SHORT).show();
        else subscribe(connection.getSubTopic(), connection.getQos());
    }

    @Override
    public void onSelectFilePressed() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_PICKER_REQUEST_CODE);
        } else showFilePickerDialog();
    }

    @Override
    public void onStartPressed() {
        if (connection.isConnected()) {
            if (simulationFilePath.isEmpty()) {
                Toast.makeText(this, "You haven't chosen any file!", Toast.LENGTH_SHORT).show();
            } else {
                if (connection.getMaxSimulationTime() == 0) {
                    Toast.makeText(this, "Timeout was not set. Default behavior will be applied", Toast.LENGTH_SHORT).show();
                    connection.setMaxSimulationTime(0);
                }
                Toast.makeText(this, "Starting simulation", Toast.LENGTH_SHORT).show();
                SimulationRunnable runnable = new SimulationRunnable(simulationFilePath, connection.getMaxSimulationTime(), connection.getQos(), connection.isRetain());
                stopSimulation = false;
                new Thread(runnable).start();
            }
        } else {
            Toast.makeText(this, "You need to connect first!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStopPressed() {
        stopSimulation = true;
        Toast.makeText(this, "Stopping simulation", Toast.LENGTH_SHORT).show();
    }

    public void onConnectSuccess() {
        turnOnFAB();
        System.out.println("Successfully connected to " + connection.getServerUri());
        Toast.makeText(this, "Connected to " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    public void onConnectFailure() {
        turnOffFAB();
        System.err.println("Failed to connect to " + connection.getServerUri());
        if (connection.isInternetServiceAvailable())
            Toast.makeText(this, "Failed to connect. Please check your IP/port settings and retry!", Toast.LENGTH_LONG).show();
        else Toast.makeText(this, "Failed to connect. Please check your internet connection and retry!", Toast.LENGTH_SHORT).show();
    }

    public void onDisconnectSuccess() {
        turnOffFAB();
        System.out.println("Successfully disconnected from " + connection.getServerUri());
        Toast.makeText(this, "Disconnected from " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    public void onDisconnectFailure() {
        turnOnFAB();
        System.out.println("Failed to disconnect from " + connection.getServerUri());
        Toast.makeText(this, "Failed to disconnect from " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    public void handleMessageArrived(String topic, MqttMessage message) {
        System.out.println("Received " + message + " in " + topic);
        Toast.makeText(connection.getContext(), "Received " + message + " in " + topic, Toast.LENGTH_SHORT).show();
    }

    public void handleConnectionLost() {
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

    private void showFilePickerDialog() {
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .withRootPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.(csv)$"))
                .withFilterDirectories(false)
                .withTitle("Select a file")
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .start();
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

    class SimulationRunnable implements Runnable {

        String file;
        int lineNumber;
        String line;
        int timeout;
        int qos;
        boolean retain;

        SimulationRunnable(String file, int timeout, int qos, boolean retain) {
            this.file = file;
            this.lineNumber = 0;
            this.line = "";
            this.timeout = timeout;
            this.qos = qos;
            this.retain = retain;
        }

        @Override
        public void run() {
            ArrayList<String> lines = new ArrayList<>();
            // Read the whole file and store its rows into "lines" list
            try {
                Scanner scanner = new Scanner(new File(file)).useDelimiter(System.lineSeparator());
                while (scanner.hasNextLine()) lines.add(scanner.nextLine());
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (timeout == 0) timeout = lines.size();
            ScheduledExecutorService service = Executors.newScheduledThreadPool(0);
            ScheduledFuture<?> serviceHandler = service.scheduleAtFixedRate(() -> {
                if (lineNumber >= lines.size() || lineNumber >= timeout || stopSimulation) {
                    service.shutdownNow();
                } else {
                    // Save current data to pass to main thread, or they might be updated before being consumed
                    String currentLine = lines.get(lineNumber);
                    // Reset the publishing topic
                    connection.setPubTopic("");
                    // Assign job to Main thread
                    handler.post(() -> publish(connection.getPubTopic() + "simulation", currentLine, connection.getQos(), connection.isRetain()));
                    lineNumber++;
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

}