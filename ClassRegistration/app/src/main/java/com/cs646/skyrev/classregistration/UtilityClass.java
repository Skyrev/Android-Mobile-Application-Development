package com.cs646.skyrev.classregistration;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.graphics.Typeface.BOLD;

/**
 * Created by skyrev on 4/1/18.
 */

public class UtilityClass {

    public UtilityClass() {
        //
    }

    public static LinearLayout getRow(Context context, String label, String value) {
        LinearLayout row = new LinearLayout(context);
        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams textViewParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        rowParams.setMarginStart(50);
        rowParams.setMarginEnd(50);
        row.setLayoutParams(rowParams);

        TextView labelView = new TextView(context);
        labelView.setTypeface(null, BOLD);
        labelView.setTextSize(18);
        labelView.setText(label);
        labelView.setLayoutParams(textViewParams);
        row.addView(labelView);

        TextView valueView = new TextView(context);
        valueView.setTextSize(18);
        valueView.setText(value);
        valueView.setLayoutParams(textViewParams);
        row.addView(valueView);

        return row;
    }

    public static String serializeParams(String... params) {
        String serializedParams = "?";
        for(int i = 0; i < params.length-1; i++) {
            serializedParams += (params[i] + "=" + params[i+1] + "&");
        }
        return serializedParams.substring(0, serializedParams.length()-1);
    }
}
