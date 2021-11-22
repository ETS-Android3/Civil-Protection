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

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.civilprotection.R;
import com.civilprotection.ui.viewmodels.FragmentViewModel;

import org.jetbrains.annotations.NotNull;

public class FragmentPublish extends Fragment implements View.OnClickListener {

    public interface OnPublishListener {
        void onPublishPressed();
    }

    private OnPublishListener publishListener;
    private FragmentViewModel viewModel;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                this.publishListener = (OnPublishListener) activity;
            } catch (final ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnPublishListener");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, parent, false);
        AppCompatButton publishBtn = view.findViewById(R.id.publishButton);
        publishBtn.setOnClickListener(this);
        // Setup viewModel to share data with parent activity
        viewModel = new ViewModelProvider(requireActivity()).get(FragmentViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.publishButton) {
            updateValues();
            publishListener.onPublishPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void updateValues() {
        EditText topic = requireView().findViewById(R.id.publishTopicEditText);
        viewModel.setPublishTopic(topic.getText().toString());
        EditText message = requireView().findViewById(R.id.publishMessageEditText);
        viewModel.setPublishMessage(message.getText().toString());
        RadioGroup qos = requireView().findViewById(R.id.publishQosRadio);
        switch (qos.getCheckedRadioButtonId()) {
            case R.id.qos0:
                viewModel.setPublishQos(0);
                break;
            case R.id.qos1:
                viewModel.setPublishQos(1);
                break;
            case R.id.qos2:
                viewModel.setPublishQos(2);
                break;
        }
        CheckBox retain = requireView().findViewById(R.id.publishRetainedCheckBox);
        viewModel.setPublishRetain(retain.isChecked());
    }

}