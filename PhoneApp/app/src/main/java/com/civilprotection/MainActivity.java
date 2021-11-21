package com.civilprotection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.civilprotection.ui.viewmodels.FragmentViewModel;
import com.civilprotection.ui.SectionsPagerAdapter;
import com.civilprotection.ui.fragments.FragmentData;
import com.civilprotection.ui.fragments.FragmentPublish;
import com.civilprotection.ui.fragments.FragmentSubscribe;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.os.Vibrator;
import android.media.MediaPlayer;
import android.media.AudioManager;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements FragmentPublish.OnPublishListener, FragmentSubscribe.OnSubscribeListener, FragmentData.OnSimulationListener {

    private FragmentViewModel viewModel;
    ViewPager viewPager;
    Handler handler;
    CallbackHandler.CallBackListener listener;

    String serverUri = "";
    Connection connection;

    FragmentPublish publishFragment;
    FragmentSubscribe subscribeFragment;
    FragmentData simulationFragment;

    private String simulationFilePath;
    private int lineNumber = 0;
    private final AtomicBoolean stopSimulation = new AtomicBoolean(true);
    private final AtomicBoolean gpsPermissionGranted = new AtomicBoolean(false);
    private final AtomicBoolean gpsReady = new AtomicBoolean(false);
    private String latitude = "";
    private String longitude = "";
    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private static final int LOCATION_PROVIDER_CODE = 2;

    FloatingActionButton fab;
    private FusedLocationProviderClient locationProvider;

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

        // Create the connection and register the callback listener
        this.serverUri = "tcp://" + getResources().getString(R.string.defaultServerIp) + ":" + getResources().getString(R.string.defaultServerPort);
        setupConnectionListener();
        try {
            connection = new Connection(this.serverUri, readStringSetting("session_id"), new MemoryPersistence(), this, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
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
            case R.id.resetSettingsMenu:
                String sessionId = readStringSetting("session_id");
                getDefaultSharedPreferences(this).edit().clear().apply();
                // Restore the session ID
                setStringSetting("session_id", String.valueOf(sessionId));
                return true;
            case R.id.showAlertMenu:
                showAlertDialog(true, "You are in danger!");
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
        switch (requestCode) {
            case FILE_PICKER_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) showFilePickerDialog();
                else Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                break;
            case LOCATION_PROVIDER_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsPermissionGranted.set(true);
                    setUpLocationProvider();
                } else {
                    stopSimulation.set(true);
                    gpsPermissionGranted.set(false);
                    Toast.makeText(this, "You need to give location permission to enable Auto Coordinates mode", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            simulationFilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            assert simulationFilePath != null;
            viewModel.setSimulationFilePath(simulationFilePath.substring(simulationFilePath.lastIndexOf("/") + 1));
        }
    }

    private void createTabs() {
        viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentPublish(), getResources().getString(R.string.tab_1_title));
        adapter.addFragment(new FragmentSubscribe(), getResources().getString(R.string.tab_2_title));
        adapter.addFragment(new FragmentData(), getResources().getString(R.string.tab_3_title));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        // Store the new fragments for future use
        publishFragment = (FragmentPublish) getSupportFragmentManager().findFragmentById(R.id.publishFragment);
        subscribeFragment = (FragmentSubscribe) getSupportFragmentManager().findFragmentById(R.id.subscribeFragment);
        simulationFragment = (FragmentData) getSupportFragmentManager().findFragmentById(R.id.simulationFragment);
        // Prepare the viewModel for communication between this activity and its child-fragments
        viewModel = new ViewModelProvider(this).get(FragmentViewModel.class);
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
        viewModel.getSimulationAutoMode().observe(this, autoMode -> connection.setAutoMode(autoMode));
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
                connection.setSubTopic("#");
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
        if (ContextCompat.checkSelfPermission(com.civilprotection.MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(com.civilprotection.MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, FILE_PICKER_REQUEST_CODE);
        } else showFilePickerDialog();
    }

    @Override
    public void onStartPressed() {
        if (connection.isConnected()) {
            if (stopSimulation.get()) {
                if (connection.isAutoMode()) {
                    // Auto mode connection - Pick coordinates from GPS sensor
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PROVIDER_CODE);
                    } else {
                        setUpLocationProvider();
                    }
                } else {
                    if (simulationFilePath.isEmpty()) {
                        Toast.makeText(this, "You haven't chosen any file!", Toast.LENGTH_SHORT).show();
                    } else {
                        SimulationRunnable runnable = new SimulationRunnable(simulationFilePath, connection.getMaxSimulationTime());
                        new Thread(runnable).start();
                    }
                }
            } else {
                Toast.makeText(this, "You need to wait until the previous simulation ends to press the Start Button again!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You need to connect first!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStopPressed() {
        stopSimulation.set(true);
        Toast.makeText(this, "Stopping simulation", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void setUpLocationProvider() {
        locationProvider.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                gpsReady.set(true);
                gpsPermissionGranted.set(true);
                SimulationRunnable runnable = new SimulationRunnable(simulationFilePath, connection.getMaxSimulationTime());
                new Thread(runnable).start();
            }
        });
    }

    private void onConnectSuccess() {
        turnOnFAB();
        System.out.println("Successfully connected to " + connection.getServerUri());
        Toast.makeText(this, "Connected to " + connection.getServerIp(), Toast.LENGTH_SHORT).show();
    }

    private void onConnectFailure() {
        turnOffFAB();
        System.err.println("Failed to connect to " + connection.getServerUri());
        if (connection.isInternetServiceAvailable())
            Toast.makeText(this, "Failed to connect. Please check your IP/port settings and retry!", Toast.LENGTH_LONG).show();
        else Toast.makeText(this, "Failed to connect. Please check your internet connection and retry!", Toast.LENGTH_SHORT).show();
    }

    private void onDisconnectSuccess() {
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
        // Parse topic to find out what type of danger it is
        showAlertDialog(true, "You are in danger!");
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

    private void showAlertDialog(boolean alertFlag, String message) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage(message);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        MediaPlayer mediaPlayer;
        boolean dismiss = false;
        if (alertFlag) {
            // High level danger : server returns 1
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_high);
        } else {
            // Medium level danger : server returns 0
            mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            mediaPlayer.stop();
            dialogInterface.dismiss();
        });
        builder.setOnDismissListener(dialogInterface -> {
            mediaPlayer.stop();
            dialogInterface.dismiss();
        });
        builder.show();
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
        String line;
        int timeout;

        SimulationRunnable(String file, int timeout) {
            this.file = file;
            this.line = "";
            this.timeout = timeout;
        }

        @Override
        public void run() {
            stopSimulation.set(false);
            handler.post(() -> Toast.makeText(getApplicationContext(), "Starting simulation", Toast.LENGTH_SHORT).show());
            ScheduledExecutorService service = Executors.newScheduledThreadPool(0);
            if (connection.isAutoMode()) {
                handler.post(() -> Toast.makeText(getApplicationContext(), "Using Auto Mode", Toast.LENGTH_SHORT).show());
                ScheduledFuture<?> serviceHandler = service.scheduleAtFixedRate(() -> {
                    if (stopSimulation.get()) {
                        service.shutdownNow();
                    } else {
                        if (gpsPermissionGranted.get() && gpsReady.get()) {
                            // Set the publishing topic
                            connection.setPubTopic("simulation");
                            connection.setMessage(latitude + ";" + longitude);
                            // Assign job to Main thread
                            handler.post(() -> publish(connection.getPubTopic(), connection.getMessage(), connection.getQos(), connection.isRetain()));
                        }
                    }
                }, 0, 1, TimeUnit.SECONDS);
            } else {
                handler.post(() -> Toast.makeText(getApplicationContext(), "Using Manual Mode", Toast.LENGTH_SHORT).show());
                ArrayList<String> lines = new ArrayList<>();
                // Read the whole file and store its rows into "lines" list
                try {
                    Scanner scanner = new Scanner(new File(file)).useDelimiter(System.lineSeparator());
                    while (scanner.hasNextLine()) lines.add(scanner.nextLine());
                    scanner.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (timeout == 0) {
                    handler.post(() -> Toast.makeText(getApplicationContext(), "Timeout was not set. Default behavior will be applied", Toast.LENGTH_SHORT).show());
                    timeout = lines.size();
                }
                ScheduledFuture<?> serviceHandler = service.scheduleAtFixedRate(() -> {
                    if (lineNumber >= timeout || stopSimulation.get()) {
                        // If we stopped due to timeout, update the stopSimulation flag
                        stopSimulation.set(true);
                        service.shutdownNow();
                    } else {
                        // Loop over the input file
                        if (lineNumber >= lines.size()) lineNumber = 0;
                        // Save current data to pass to main thread, or they might be updated before being consumed
                        String currentLine = lines.get(lineNumber);
                        // Set the publishing topic
                        connection.setPubTopic("simulation");
                        connection.setMessage(currentLine);
                        // Assign job to Main thread
                        handler.post(() -> publish(connection.getPubTopic(), connection.getMessage(), connection.getQos(), connection.isRetain()));
                        lineNumber++;
                    }
                }, 0, 1, TimeUnit.SECONDS);
            }
        }

    }

}