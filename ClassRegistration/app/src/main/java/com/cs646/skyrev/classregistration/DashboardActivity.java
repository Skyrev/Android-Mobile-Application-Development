package com.cs646.skyrev.classregistration;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;

public class DashboardActivity extends AppCompatActivity {

    private static DashboardActivity instance;
    private static RequestQueue queue;
    private JSONObject studentClassIds;
    private JSONArray registeredClassIds;
    private JSONArray registeredClassDetails;
    private JSONArray waitlistedClassIds;
    private JSONArray waitlistedClassDetails;
    private SharedPreferences prefs;
    private LinearLayout progressBar;

    public static DashboardActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        instance = this;

        queue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ((TextView) this.findViewById(R.id.first_name))
                .setText(prefs.getString(getString(R.string.key_firstname), ""));

        ((TextView) this.findViewById(R.id.last_name))
                .setText(prefs.getString(getString(R.string.key_lastname), ""));

        ((TextView) this.findViewById(R.id.red_id))
                .setText(prefs.getString(getString(R.string.key_redid), ""));

        ((TextView) this.findViewById(R.id.email_id))
                .setText(prefs.getString(getString(R.string.key_email), ""));

        progressBar = this.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        loadDashboard();
    }

    public void reloadActivity() {
        finish();
        startActivity(getIntent());
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public JSONArray getRegisteredClassDetails() {
        return registeredClassDetails;
    }

    public void displayAllCourses(View view) {
        Intent browse = new Intent(getApplicationContext(), SubjectsActivity.class);
        startActivity(browse);
    }

    private void loadDashboard() {

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                
                studentClassIds = response;
                try {
                    registeredClassIds = studentClassIds.getJSONArray(getString(R.string.key_classes));
                    fetchRegisteredClassDetails();

                    waitlistedClassIds = studentClassIds.getJSONArray(getString(R.string.key_waitlist));
                    fetchWaitlistedClassDetails();

                } catch(JSONException exception) {
                    exception.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_student_classes);
        JSONObject params = new JSONObject();
        try {
            String keyRedId = getString(R.string.key_redid);
            String keyPassword = getString(R.string.key_password);
            String valueRedId = prefs.getString(keyRedId, "");
            String valuePassword = prefs.getString(keyPassword, "");
            params.put(keyRedId, valueRedId);
            params.put(keyPassword, valuePassword);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        JsonObjectRequest studentCourses = new JsonObjectRequest(url, params, success, failure);
        queue.add(studentCourses);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void fetchRegisteredClassDetails() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                registeredClassDetails = response;
                displayStudentClasses(registeredClassDetails, false);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_class_details);
        JSONObject classIds = new JSONObject();
        try {
            classIds.put(getString(R.string.key_class_ids), registeredClassIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomJsonArrayRequest classDetails = new CustomJsonArrayRequest(POST, url, classIds, success, failure);
        queue.add(classDetails);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void fetchWaitlistedClassDetails() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                waitlistedClassDetails = response;
                displayStudentClasses(waitlistedClassDetails, true);
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_class_details);
        JSONObject classIds = new JSONObject();
        try {
            classIds.put(getString(R.string.key_class_ids), waitlistedClassIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CustomJsonArrayRequest classDetails = new CustomJsonArrayRequest(POST, url, classIds, success, failure);
        queue.add(classDetails);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void displayStudentClasses(JSONArray studentClasses, boolean isWaitlisted) {
        LinearLayout container = (LinearLayout) (isWaitlisted ? this.findViewById(R.id.waitlisted_courses_container)
                                : this.findViewById(R.id.registered_courses_container));

        TextView courseCount = (TextView) (isWaitlisted ? this.findViewById(R.id.waitlisted_courses_count)
                                :  this.findViewById(R.id.registered_courses_count));
        String count = studentClasses.length() + "";
        courseCount.setText(count);

        if(count.equals("0"))
            container.getChildAt(1).setVisibility(View.GONE);
        else
            container.getChildAt(1).setVisibility(View.VISIBLE);

        try {
            for(int i = 0; i < studentClasses.length(); i++) {
                JSONObject detail = studentClasses.getJSONObject(i);
                LinearLayout entry = new LinearLayout(getApplicationContext());
                LinearLayout.LayoutParams entryParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                entryParams.setMargins(50, 0, 50, 20);
                entry.setOrientation(LinearLayout.VERTICAL);
                entry.setLayoutParams(entryParams);

                entry.addView(UtilityClass.getRow(getApplicationContext(),
                        getString(R.string.label_id),
                        detail.getString(getString(R.string.key_id))));

                entry.addView(UtilityClass.getRow(getApplicationContext(),
                        getString(R.string.label_title),
                        detail.getString(getString(R.string.key_title))));

                String time = detail.getString(getString(R.string.key_start_time))
                            + getString(R.string.spaced_hyphen)
                            + detail.getString(getString(R.string.key_end_time))
                            + getString(R.string.time_suffix);
                entry.addView(UtilityClass.getRow(getApplicationContext(),
                        getString(R.string.label_time), time));

                entry.addView(UtilityClass.getRow(getApplicationContext(),
                        getString(R.string.label_days),
                        detail.getString(getString(R.string.key_days))));

                entry.addView(UtilityClass.getRow(getApplicationContext(),
                        getString(R.string.label_instructor),
                        detail.getString(getString(R.string.key_instructor))));

                entry.getChildAt(0).setVisibility(View.GONE);

                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(200, 100);
                buttonParams.gravity = Gravity.END;
                buttonParams.setMargins(0, -100, 0, 0);
                Button dropButton = new Button(getApplicationContext());
                dropButton.setLayoutParams(buttonParams);
                dropButton.setPadding(10, 10, 10, 10);
                dropButton.setGravity(Gravity.CENTER);
                dropButton.setBackgroundColor(Color.parseColor(
                        getString(R.string.drop_button_background_color)));
                dropButton.setText(getString(R.string.label_drop));
                dropButton.setTextColor(Color.WHITE);
                dropButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout container = (LinearLayout)((Button)view).getParent();
                        final String courseId = ((TextView)((LinearLayout)(container.getChildAt(0)))
                                            .getChildAt(1)).getText().toString();
                        LinearLayout parent = (LinearLayout)container.getParent();
                        String heading = ((TextView)((LinearLayout) parent.getChildAt(0)).getChildAt(0))
                                .getText().toString();
                        final boolean isWaitlisted = heading.equals(
                                        getString(R.string.label_waitlisted_courses));

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardActivity.this);
                        String message = isWaitlisted ? getString(R.string.drop_waitlisted_course_msg)
                                                        : getString(R.string.drop_course_msg) ;
                        alertDialog.setMessage(message);
                        alertDialog.setCancelable(true);
                        int choice = isWaitlisted ? R.string.label_remove : R.string.label_drop;
                        alertDialog.setPositiveButton(choice, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DashboardActivity.getInstance().dropCourse(courseId, isWaitlisted);
                            }
                        });
                        alertDialog.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alert = alertDialog.create();
                        int title = isWaitlisted ? R.string.label_remove_waitlisted_course
                                                : R.string.label_drop_course;
                        alert.setTitle(title);
                        alert.show();
                    }
                });
                entry.addView(dropButton);
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

                container.addView(entry);
            }
        } catch(JSONException exception) {
            exception.printStackTrace();
        }

        if(registeredClassIds.length() == 0 && waitlistedClassIds.length() == 0)
            this.findViewById(R.id.button_reset_student).setVisibility(View.GONE);
        else
            this.findViewById(R.id.button_reset_student).setVisibility(View.VISIBLE);

    }

    public void dropCourse(String courseId, boolean isWaitlisted) {
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    String message = response.has(getString(R.string.key_ok)) ?
                                        response.getString(getString(R.string.key_ok))
                                        : response.getString(getString(R.string.key_error));
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    reloadActivity();

                } catch(JSONException exception) {
                    exception.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_drop_course_failed),
                        Toast.LENGTH_LONG).show();
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = isWaitlisted ? getString(R.string.url_unwaitlist_class)
                    : getString(R.string.url_unregister_class);

        String keyRedId = getString(R.string.key_redid);
        String keyPassword = getString(R.string.key_password);
        String keyCourseId = getString(R.string.key_course_id);
        String valueRedId = prefs.getString(keyRedId, "");
        String valuePassword = prefs.getString(keyPassword, "");
        url += UtilityClass.serializeParams(
                keyRedId, valueRedId,
                keyPassword, valuePassword,
                keyCourseId, courseId);

        JsonObjectRequest dropCourse = new JsonObjectRequest(GET, url, null, success, failure);
        queue.add(dropCourse);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void dropAllCoursesDialog(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardActivity.this);
        alertDialog.setMessage(getString(R.string.drop_all_courses_msg));
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton(R.string.label_drop_all_courses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DashboardActivity.getInstance().resetStudent();
            }
        });
        alertDialog.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.setTitle(R.string.label_drop_all_courses);
        alert.show();
    }

    private void resetStudent() {
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    String message = response.has(getString(R.string.key_ok)) ?
                                    response.getString(getString(R.string.key_ok))
                                    : response.getString(getString(R.string.key_error));
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    DashboardActivity.getInstance().reloadActivity();
                } catch(JSONException exception) {
                    exception.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.response_error),
                        Toast.LENGTH_LONG).show();
            }
        };

        String url = getString(R.string.url_reset_student);

        String keyRedId = getString(R.string.key_redid);
        String keyPassword = getString(R.string.key_password);
        String valueRedId = prefs.getString(keyRedId, "");
        String valuePassword = prefs.getString(keyPassword, "");
        url += UtilityClass.serializeParams(keyRedId, valueRedId, keyPassword, valuePassword);

        JsonObjectRequest resetStudent = new JsonObjectRequest(GET, url, null, success, failure);
        queue.add(resetStudent);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void logout(View view) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardActivity.this);
        alertDialog.setMessage(getString(R.string.logout_msg));
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton(R.string.label_logout, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent logout = new Intent(DashboardActivity.getInstance(), LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
                finish();
            }
        });
        alertDialog.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.setTitle(R.string.label_logout);
        alert.show();
    }

    /*
    * Source: https://stackoverflow.com/questions/41264786/android-volley-jsonarrayrequest-join-stringrequest-post
    * This is a customized JSONArrayRequest class that lets create a JSONArrayRequest
    * object with JSONObject as the body of the request.
    *
    * */
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
