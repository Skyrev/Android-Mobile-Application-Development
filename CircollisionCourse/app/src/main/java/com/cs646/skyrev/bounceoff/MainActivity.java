package com.cs646.skyrev.bounceoff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    CollisionGroundView collisionGroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        collisionGroundView = this.findViewById(R.id.collision_ground_view);

        // Make view full screen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Show a toast with instructions on start up
        Toast toast = Toast.makeText(this, R.string.msg, Toast.LENGTH_LONG);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if(textView != null)
            textView.setGravity(Gravity.CENTER);
        toast.show();
    }

    // reset view when user taps Reset button
    public void reset(View resetButton) {
        collisionGroundView.reset();
    }
}
