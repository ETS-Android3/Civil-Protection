package com.civilprotectionsensor.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.civilprotectionsensor.R;
import com.civilprotectionsensor.ui.viewmodels.CreateSensorViewModel;
import com.google.android.material.slider.RangeSlider;

import org.jetbrains.annotations.NotNull;

public class FragmentCreateSmokeSensor extends Fragment implements View.OnClickListener {

    public interface SmokeSensorCreateListener {
        void onCreateSmokeSensor();
    }

    private SmokeSensorCreateListener listener;
    private CreateSensorViewModel viewModel;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                this.listener = (SmokeSensorCreateListener) activity;
            } catch (final ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement onCreateSmokeSensor");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_smoke_sensor, parent, false);
        AppCompatButton createBtn = view.findViewById(R.id.createSmokeSensorButton);
        createBtn.setOnClickListener(this);
        // Setup viewModel to share data with parent activity
        viewModel = new ViewModelProvider(requireActivity()).get(CreateSensorViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createSmokeSensorButton) {
            updateValues();
            listener.onCreateSmokeSensor();
        }
    }

    private void updateValues() {
        RangeSlider slider = requireView().findViewById(R.id.createSmokeSensorSlider);
        viewModel.setSmokeMin(slider.getValues().get(0));
        viewModel.setSmokeMax(slider.getValues().get(1));
    }

}