package com.civilprotectionsensor;

import android.content.Intent;
import android.os.Bundle;

import com.civilprotectionsensor.ui.SectionsPagerAdapter;
import com.civilprotectionsensor.ui.fragments.FragmentCreateGasSensor;
import com.civilprotectionsensor.ui.fragments.FragmentCreateSmokeSensor;
import com.civilprotectionsensor.ui.fragments.FragmentCreateTempSensor;
import com.civilprotectionsensor.ui.fragments.FragmentCreateUvSensor;
import com.civilprotectionsensor.ui.viewmodels.CreateSensorViewModel;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

public class SensorCreateActivity extends AppCompatActivity
        implements FragmentCreateSmokeSensor.SmokeSensorCreateListener, FragmentCreateGasSensor.GasSensorCreateListener,
        FragmentCreateTempSensor.TempSensorCreateListener, FragmentCreateUvSensor.UvSensorCreateListener {

    private CreateSensorViewModel viewModel;
    private String sensorType = "";
    private String sensorMin = "";
    private String sensorMax = "";
    private static final String[] SENSOR_TYPES = {"smoke", "gas", "temp", "uv"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_create);
        createTabs();
    }


    @Override
    public void onCreateSmokeSensor() {
        sensorType = SENSOR_TYPES[0];
        viewModel.getSmokeMin().observe(this, value -> sensorMin = String.valueOf(value));
        viewModel.getSmokeMax().observe(this, value -> sensorMax = String.valueOf(value));
        createSensor();
    }

    @Override
    public void onCreateGasSensor() {
        sensorType = SENSOR_TYPES[1];
        viewModel.getGasMin().observe(this, value -> sensorMin = String.valueOf(value));
        viewModel.getGasMax().observe(this, value -> sensorMax = String.valueOf(value));
        createSensor();
    }

    @Override
    public void onCreateTempSensor() {
        sensorType = SENSOR_TYPES[2];
        viewModel.getTempMin().observe(this, value -> sensorMin = String.valueOf(value));
        viewModel.getTempMax().observe(this, value -> sensorMax = String.valueOf(value));
        createSensor();
    }

    @Override
    public void onCreateUvSensor() {
        sensorType = SENSOR_TYPES[3];
        viewModel.getUvMin().observe(this, value -> sensorMin = String.valueOf(value));
        viewModel.getUvMax().observe(this, value -> sensorMax = String.valueOf(value));
        createSensor();
    }

    private void createTabs() {
        ViewPager viewPager = findViewById(R.id.createSensorViewPager);
        TabLayout tabs = findViewById(R.id.createSensorTabs);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        adapter.addFragment(new FragmentCreateSmokeSensor(), getResources().getString(R.string.title_smoke_create));
        adapter.addFragment(new FragmentCreateGasSensor(), getResources().getString(R.string.title_gas_create));
        adapter.addFragment(new FragmentCreateTempSensor(), getResources().getString(R.string.title_temp_create));
        adapter.addFragment(new FragmentCreateUvSensor(), getResources().getString(R.string.title_uv_create));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        viewModel = new ViewModelProvider(this).get(CreateSensorViewModel.class);
    }

    private void createSensor() {
        Intent intent = new Intent();
        intent.putExtra("type", sensorType);
        intent.putExtra("min", sensorMin);
        intent.putExtra("max", sensorMax);
        intent.putExtra("current", String.valueOf((Float.parseFloat(sensorMax) + Float.parseFloat(sensorMin)) / 2));
        setResult(RESULT_OK, intent);
        finish();
    }

}