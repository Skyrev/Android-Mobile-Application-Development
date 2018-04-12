package com.cs646.skyrev.classregistration;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.Request.Method.POST;

public class ClassesActivity extends AppCompatActivity {

    private Intent subjectsIntent;

    private JSONArray classIdsList;
    private JSONArray classes;
    private LinearLayout classListLayout;
    private RequestQueue queue;
    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        classIdsList = new JSONArray();
        classes = new JSONArray();
        subjectsIntent = getIntent();
        classListLayout = this.findViewById(R.id.class_list_layout);
        queue = Volley.newRequestQueue(this);

        progressBar = this.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        getClassIdsList();
    }

    private void getClassIdsList() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                classIdsList = response;
                getClasses();
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_class_ids_list);

        String keySubjectId = getString(R.string.key_subject_id);
        String valueSubjectId = subjectsIntent.getStringExtra(keySubjectId);

        if(valueSubjectId == null) {
            String keySubjectIds = getString(R.string.key_subject_ids);
            JSONArray valueSubjectIds = new JSONArray();
            for(int i : subjectsIntent.getIntArrayExtra(keySubjectIds))
                valueSubjectIds.put(i);

            String startTime = subjectsIntent.getStringExtra(getString(R.string.key_starttime));
            String level = subjectsIntent.getStringExtra(getString(R.string.key_level));

            JSONObject params = new JSONObject();
            try {
                params.put(getString(R.string.key_subject_ids), valueSubjectIds);
                params.put(getString(R.string.key_starttime), startTime);
                params.put(getString(R.string.key_level), level);
            } catch(JSONException exception) {
                exception.printStackTrace();
            }
            CustomJsonArrayRequest request = new CustomJsonArrayRequest(POST, url, params, success, failure);
            queue.add(request);
        }
        else {
            url += UtilityClass.serializeParams(keySubjectId, valueSubjectId);
            JsonArrayRequest request = new JsonArrayRequest(url, success, failure);
            queue.add(request);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void getClasses() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                classes = response;

                try {
                    for(int i = 0; i < classes.length(); i++) {
                        JSONObject detail = classes.getJSONObject(i);

                        LinearLayout entry = new LinearLayout(getApplicationContext());
                        entry.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(50, 0, 50, 20);
                        entry.setLayoutParams(params);

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_id), detail.getString(getString(R.string.key_id))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_schedule_number), detail.getString(getString(R.string.key_schedule_no))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_title), detail.getString(getString(R.string.key_title))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_time), detail.getString(getString(R.string.key_start_time))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_days), detail.getString(getString(R.string.key_days))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_instructor), detail.getString(getString(R.string.key_instructor))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_seats), detail.getInt(getString(R.string.key_seats))+""));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_waitlist), detail.getInt(getString(R.string.key_waitlist))+""));

                        entry.getChildAt(0).setVisibility(View.GONE);

                        entry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String classId = ((TextView)((LinearLayout)((LinearLayout)view)
                                        .getChildAt(0)).getChildAt(1)).getText().toString();
                                Intent classDetailIntent = new Intent(getApplicationContext(),
                                        ClassDetailsActivity.class);
                                classDetailIntent.putExtra(getString(R.string.key_class_id), classId);
                                startActivity(classDetailIntent);
                            }
                        });
                        entry.setBackgroundColor(Color.parseColor(getString(R.string.list_background_color)));

                        classListLayout.addView(entry);
                    }
                } catch(JSONException exception) {
                    Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                            Toast.LENGTH_LONG);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_class_details);
        JSONObject classIds = new JSONObject();
        try {
            classIds.put(getString(R.string.key_class_ids), classIdsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomJsonArrayRequest request = new CustomJsonArrayRequest(POST, url, classIds,success, failure);
        queue.add(request);
        progressBar.setVisibility(View.VISIBLE);
    }

    private class CustomJsonArrayRequest extends JsonRequest<JSONArray> {

        public CustomJsonArrayRequest(int method, String url, JSONObject jsonRequest,
                                      Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
            super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                    errorListener);
        }

        @Override
        protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                return Response.success(new JSONArray(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException exception) {
                return Response.error(new ParseError(exception));
            } catch (JSONException exception) {
                return Response.error(new ParseError(exception));
            }
        }
    }
}
