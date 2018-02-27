package com.cs646.skyrev.bounceoff;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        Toast toast = Toast.makeText(this, "Tap and hold to draw circle.\n" +
                "Hold and swipe circle to set it in motion.", Toast.LENGTH_LONG);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if(textView != null)
            textView.setGravity(Gravity.CENTER);
        toast.show();
    }

    public void reset(View resetButton) {
        CollisionGroundView collisionGroundView = this.findViewById(R.id.collision_ground_view);
        collisionGroundView.reset();
    }
}
