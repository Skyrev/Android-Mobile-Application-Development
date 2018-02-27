package com.cs646.skyrev.bounceoff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    public void reset(View resetButton) {
        CollisionGroundView collisionGroundView = this.findViewById(R.id.collision_ground_view);
        collisionGroundView.reset();
    }
}
