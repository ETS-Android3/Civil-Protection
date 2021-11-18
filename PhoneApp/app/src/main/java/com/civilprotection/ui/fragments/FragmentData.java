package com.civilprotection.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.civilprotection.R;
import com.civilprotection.ui.viewmodels.FragmentViewModel;

import org.jetbrains.annotations.NotNull;

public class FragmentData extends Fragment implements View.OnClickListener {

    public interface OnSimulationListener {
        void onSelectFilePressed();

        void onStartPressed();

        void onStopPressed();
    }

    private OnSimulationListener simulationListener;
    private FragmentViewModel viewModel;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                this.simulationListener = (OnSimulationListener) activity;
            } catch (final ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnSimulationListener");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, parent, false);
        AppCompatButton selectFileBtn = view.findViewById(R.id.simulationFileButton);
        AppCompatButton startBtn = view.findViewById(R.id.simulationStartButton);
        AppCompatButton stopBtn = view.findViewById(R.id.simulationStopButton);
        selectFileBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        // Setup viewModel to share data with parent activity
        viewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView filePathTextView = requireView().findViewById(R.id.simulationPathTextView);
        viewModel.getSimulationFilePath().observe(this, filePathTextView::setText);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        updateValues();
        switch (v.getId()) {
            case R.id.simulationFileButton:
                simulationListener.onSelectFilePressed();
                break;
            case R.id.simulationStartButton:
                simulationListener.onStartPressed();
                break;
            case R.id.simulationStopButton:
                simulationListener.onStopPressed();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void updateValues() {
        RadioGroup qos = requireView().findViewById(R.id.simulationQosRadio);
        switch (qos.getCheckedRadioButtonId()) {
            case R.id.qos0:
                viewModel.setSimulationQos(0);
                break;
            case R.id.qos1:
                viewModel.setSimulationQos(1);
                break;
            case R.id.qos2:
                viewModel.setSimulationQos(2);
                break;
        }
        EditText timeOut = requireView().findViewById(R.id.simulationTimeOutEditText);
        if (timeOut.getText().toString().length() > 0)
            viewModel.setSimulationTimeOut(timeOut.getText().toString());
            // 0 timeout will lead the main activity to use the default timeout from the preferences
        else viewModel.setSimulationTimeOut("0");
        CheckBox retain = requireView().findViewById(R.id.simulationRetainedCheckBox);
        viewModel.setSimulationRetain(retain.isChecked());
    }

}
