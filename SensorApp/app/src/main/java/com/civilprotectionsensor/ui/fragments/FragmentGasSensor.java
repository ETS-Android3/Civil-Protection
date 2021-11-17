package com.civilprotectionsensor.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.civilprotectionsensor.R;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class FragmentGasSensor extends Fragment {

    private Float min;
    private Float max;
    private Float current;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        assert getArguments() != null;
        setMin(getArguments().getFloatArray("args")[0]);
        setMax(getArguments().getFloatArray("args")[1]);
        setCurrent(getArguments().getFloatArray("args")[2]);
        return inflater.inflate(R.layout.fragment_gas_sensor, parent, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        Slider slider = view.findViewById(R.id.gasSensorSlider);
        slider.setValueFrom(getMin());
        slider.setValueTo(getMax());
        slider.setValue(getCurrent());
    }

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Float getCurrent() {
        return current;
    }

    public void setCurrent(Float current) {
        this.current = current;
    }

}