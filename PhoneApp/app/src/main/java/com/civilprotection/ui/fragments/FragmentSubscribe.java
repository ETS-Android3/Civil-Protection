package com.civilprotection.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.civilprotection.R;
import com.civilprotection.ui.ItemViewModel;

import org.jetbrains.annotations.NotNull;

public class FragmentSubscribe extends Fragment implements View.OnClickListener {

    public interface OnSubscribeListener {
        void onSubscribePressed();
    }

    private OnSubscribeListener subscribeListener;
    private ItemViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribe, parent, false);
        AppCompatButton subscribeBtn = view.findViewById(R.id.subscribeButton);
        subscribeBtn.setOnClickListener(this);
        // Setup viewModel to share data with parent activity
        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.subscribeButton) {
            updateValues();
            subscribeListener.onSubscribePressed();
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                this.subscribeListener = (OnSubscribeListener) activity;
            } catch (final ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnSubscribeListener");
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void updateValues() {
        EditText topic = requireView().findViewById(R.id.subscribeTopicEditText);
        viewModel.setSubscribeTopic(topic.getText().toString());
        RadioGroup qos = requireView().findViewById(R.id.subscribeQosRadio);
        switch (qos.getCheckedRadioButtonId()) {
            case R.id.qos0:
                viewModel.setSubscribeQos(0);
                break;
            case R.id.qos1:
                viewModel.setSubscribeQos(1);
                break;
            case R.id.qos2:
                viewModel.setSubscribeQos(2);
                break;
        }
    }

}
