package com.cs646.skyrev.personalinformation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MajorSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_major_selection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Set action bar title
        setTitle(getString(R.string.sdsu_advanced_degrees));
    }
}
