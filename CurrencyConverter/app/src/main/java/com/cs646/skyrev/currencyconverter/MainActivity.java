package com.cs646.skyrev.currencyconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.text.TextWatcher;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    EditText usdInput;
    EditText inrInput;
    double usdToInr = 63.89;
    double inrToUsd = 0.016;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usdInput = this.findViewById(R.id.usd_input);
        inrInput = this.findViewById(R.id.inr_input);

        usdInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String usd = usdInput.getText().toString();
                try {
                    if(!usd.isEmpty()) {
                        Double.parseDouble(usd);
                    }
                    usdInput.setError(null);
                } catch (NumberFormatException exception) {
                    usdInput.setError("Enter only numbers");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        inrInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String inr = inrInput.getText().toString();
                try {
                    if(!inr.isEmpty()) {
                        Double.parseDouble(inr);
                    }
                    inrInput.setError(null);
                } catch (NumberFormatException exception) {
                    inrInput.setError("Enter only numbers");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    public void convertCurrencies(View convertButton) {
        String usd = usdInput.getText().toString().trim();
        String inr = inrInput.getText().toString().trim();
        Double usdValue;
        Double inrValue;

        try {
            usdValue = usd.equals("") ? null : Double.parseDouble(usd);
        } catch (NumberFormatException exception) {
            usdInput.setError("Enter only numbers");
            return;
        }

        try {
            inrValue = inr.equals("") ? null : Double.parseDouble(inr);
        } catch (NumberFormatException exception) {
            inrInput.setError("Enter only numbers");
            return;
        }

        if(usdValue == null && inrValue == null) {
            if(usdInput.hasFocus()) {
                usdInput.setError("Enter value to convert");
            }
            else if(inrInput.hasFocus()) {
                inrInput.setError("Enter value to convert");
            }
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        if(usdValue != null && inrValue != null) {
            if(usdInput.hasFocus()) {
                inrInput.setText(String.valueOf(decimalFormat.format(usdValue * usdToInr)));
            }
            if(inrInput.hasFocus()) {
                usdInput.setText(String.valueOf(decimalFormat.format(inrValue * inrToUsd)));
            }
        }
        else if(usdValue != null) {
            inrInput.setText(String.valueOf(decimalFormat.format(usdValue * usdToInr)));
        }
        else if(inrValue != null) {
            usdInput.setText(String.valueOf(decimalFormat.format(inrValue * inrToUsd)));
        }
    }
}
