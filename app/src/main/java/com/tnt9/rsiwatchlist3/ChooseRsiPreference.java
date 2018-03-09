package com.tnt9.rsiwatchlist3;


import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

public class ChooseRsiPreference extends Preference{

    public ChooseRsiPreference(Context context) {
        super(context);
    }

    public ChooseRsiPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        return layoutInflater.inflate(R.layout.choose_rsi, parent, false);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
       return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        NumberPicker numberPicker = view.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(0);
    }
}
