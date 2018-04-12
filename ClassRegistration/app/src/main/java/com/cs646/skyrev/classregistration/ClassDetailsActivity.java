package com.cs646.skyrev.classregistration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class ClassDetailsActivity extends AppCompatActivity {

    private Intent classesIntent;
    private LinearLayout classDetailsLayout;
    private RequestQueue queue;
    private JSONObject classDetails;
    private SharedPreferences prefs;
    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        classesIntent = getIntent();
        classDetailsLayout = this.findViewById(R.id.class_details_layout);
        classDetails = new JSONObject();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        queue = Volley.newRequestQueue(this);

        progressBar = this.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        getClassDetails();
    }

    private void getClassDetails() {

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                String time;
                classDetails = response;
                try {
                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_schedule_number),
                            response.getString(getString(R.string.key_schedule_no))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_course_number),
                            response.getString(getString(R.string.key_course_no))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_title),
                            response.getString(getString(R.string.key_title))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_department),
                            response.getString(getString(R.string.key_department))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_description),
                            response.getString(getString(R.string.key_description))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_meeting_type),
                            response.getString(getString(R.string.key_meeting_type))));

                    time = response.getString(getString(R.string.key_start_time)) + getString(R.string.spaced_hyphen)
                            + response.getString(getString(R.string.key_end_time)) + getString(R.string.time_suffix);
                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_time), time));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_days),
                            response.getString(getString(R.string.key_days))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_building),
                            response.getString(getString(R.string.key_building))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_prerequisite),
                            response.getString(getString(R.string.key_prerequisite))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_units),
                            response.getString(getString(R.string.key_units))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_instructor),
                            response.getString(getString(R.string.key_instructor))));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_seats),
                            response.getInt(getString(R.string.key_seats))+""));

                    classDetailsLayout.addView(UtilityClass.getRow(getApplicationContext(),
                            getString(R.string.label_waitlist),
                            response.getInt(getString(R.string.key_waitlist))+""));

                } catch(JSONException exception) {
                    exception.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_class_details_failure),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_class_details);

        String keyClassId = getString(R.string.key_class_id);
        String valueClassId = classesIntent.getStringExtra(keyClassId);
        url += UtilityClass.serializeParams(keyClassId, valueClassId);

        JsonObjectRequest request = new JsonObjectRequest(url, null, success, failure);
        queue.add(request);
        progressBar.setVisibility(View.VISIBLE);
    }

//    private boolean timeClash() {
//        boolean timeClash = false;
//        boolean daysClash = false;
//        try {
//            String currentDays = classDetails.getString(getString(R.string.key_days));
//            if(currentDays.equals("ARR"))
//                return false;
//            currentDays = currentDays.replace("TH", "X");
//            Set<Character> currentDaysSet = new HashSet<Character>();
//            for(char c : currentDays.toCharArray()) {
//                currentDaysSet.add(c);
//            }
//
//            String currentStartTime = classDetails.getString(getString(R.string.key_start_time));
//            if(currentStartTime.isEmpty())
//                return false;
//            String currentEndTime = classDetails.getString(getString(R.string.key_end_time));
//            if(currentEndTime.isEmpty())
//                return false;
//            Integer startTime = Integer.parseInt(currentStartTime);
//            Integer endTime = Integer.parseInt(currentEndTime);
//
//
//            JSONArray registeredClasses = DashboardActivity.getInstance()
//                                                    .getRegisteredClassDetails();
//
//            for(int i = 0; i < registeredClasses.length(); i++) {
//                JSONObject registeredClass = registeredClasses.getJSONObject(i);
//
//                String studentDays = registeredClass.getString(getString(R.string.key_days));
//                if(studentDays.equals("ARR"))
//                    return false;
//                studentDays = studentDays.replace("TH", "X");
//                Set<Character> studentDaysSet = new HashSet<Character>();
//                for(char c : studentDays.toCharArray()) {
//                    studentDaysSet.add(c);
//                }
//                studentDaysSet.retainAll(currentDaysSet);
//                daysClash = studentDaysSet.size() != 0;
//
//
//                String studentStartTime = registeredClass.getString(getString(R.string.key_start_time));
//                if(studentStartTime.isEmpty())
//                    return false;
//                String studentEndTime = registeredClass.getString(getString(R.string.key_end_time));
//                if(studentEndTime.isEmpty())
//                    return false;
//                Integer registeredStartTime = Integer.parseInt(studentStartTime);
//                Integer registeredEndTime = Integer.parseInt(studentEndTime);
//
//                if(startTime == registeredStartTime
//                        || (startTime > registeredStartTime && startTime < registeredEndTime)
//                        || (registeredStartTime > startTime && registeredStartTime < endTime))
//                    timeClash = true;
//
//                if(timeClash && daysClash)
//                    break;
//
//            }
//
//        } catch(JSONException exception) {
//            exception.printStackTrace();
//        }
//
//        return timeClash && daysClash;
//    }

    public void addCourse(View view) {

//        if(!timeClash()) {
            boolean addToWaitlist = ((Button)findViewById(view.getId())).getText().toString()
                    .equals(getString(R.string.label_waitlist_course));
            try {
                addCourse(classDetails.getString(getString(R.string.key_id)), addToWaitlist);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//        }
//        else {
//            Toast.makeText(getApplicationContext(), getString(R.string.time_clash),
//                    Toast.LENGTH_LONG).show();
//        }
    }

    private void addCourse(String courseId, final boolean addToWaitlist) {

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                String message;
                try {
                    if(response.has(getString(R.string.key_ok)))
                        message = addToWaitlist ? getString(R.string.response_add_waitlist_success)
                                    : getString(R.string.response_add_success);
                    else
                        message = response.getString(getString(R.string.key_error));
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_LONG).show();
                } catch(JSONException exception) {
                    exception.printStackTrace();
                }

            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_add_failed),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = addToWaitlist ? getString(R.string.url_waitlist_class)
                : getString(R.string.url_register_class);

        String keyRedId = getString(R.string.key_redid);
        String keyPassword = getString(R.string.key_password);
        String keyCourseId = getString(R.string.key_course_id);
        String valueRedId = prefs.getString(keyRedId, "");
        String valuePassword = prefs.getString(keyPassword, "");
        url += UtilityClass.serializeParams(
                keyRedId, valueRedId,
                keyPassword, valuePassword,
                keyCourseId, courseId);

        JsonObjectRequest addCourse = new JsonObjectRequest(Request.Method.GET, url, null, success, failure);
        queue.add(addCourse);
        progressBar.setVisibility(View.VISIBLE);
    }
}
