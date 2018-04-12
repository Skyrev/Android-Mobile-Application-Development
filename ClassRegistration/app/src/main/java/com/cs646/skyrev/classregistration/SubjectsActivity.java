package com.cs646.skyrev.classregistration;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SubjectsActivity extends AppCompatActivity {

    private JSONArray subjectList;
    private LinearLayout subjectsListLayout;
    private RequestQueue queue;
    private LinearLayout progressBar;
    private String[] subjectTitles;
    private boolean[] selectedSubjects;
    private ArrayList<String> selectedSubjectIds;
    private HashMap<Integer, String> indexIdMap;
    private ArrayList<String> ids;
    private String startTime;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        subjectList = new JSONArray();
        selectedSubjectIds = new ArrayList<>();
        indexIdMap = new HashMap<>();
        ids = new ArrayList<>();
        startTime = "";
        level = "";

        subjectsListLayout = this.findViewById(R.id.subjects_list_container);
        queue = Volley.newRequestQueue(this);

        progressBar = this.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        getSubjectList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DashboardActivity.getInstance().reloadActivity();
    }

    private void getSubjectList() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                subjectList = response;
                subjectTitles = new String[response.length()];
                selectedSubjects = new boolean[response.length()];
                try {
                    for(int i = 0; i < subjectList.length(); i++) {
                        JSONObject subject = subjectList.getJSONObject(i);
                        indexIdMap.put(i, subject.getString(getString(R.string.key_id)));
                        subjectTitles[i] = subject.getString(getString(R.string.key_title));

                        LinearLayout entry = new LinearLayout(getApplicationContext());
                        entry.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(50, 0, 50, 20);
                        entry.setLayoutParams(params);

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_id), subject.getString(getString(R.string.key_id))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_title), subject.getString(getString(R.string.key_title))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_college), subject.getString(getString(R.string.key_college))));

                        entry.addView(UtilityClass.getRow(getApplicationContext(),
                                getString(R.string.label_classes), subject.getString(getString(R.string.key_classes))));

                        entry.getChildAt(0).setVisibility(View.GONE);

                        entry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String subjectId = ((TextView)((LinearLayout)((LinearLayout)view)
                                        .getChildAt(0)).getChildAt(1)).getText().toString();
                                Intent classesIntent = new Intent(getApplicationContext(),
                                        ClassesActivity.class);
                                classesIntent.putExtra(getString(R.string.key_subject_id), subjectId);
                                startActivity(classesIntent);
                            }
                        });
                        entry.setBackgroundColor(Color.parseColor(getString(R.string.list_background_color)));

                        subjectsListLayout.addView(entry);
                    }
                } catch(JSONException exception) {
                    exception.printStackTrace();
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

        String url = getString(R.string.url_subject_list);
        JsonArrayRequest request = new JsonArrayRequest(url, success, failure);
        queue.add(request);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void filterSubject(View view) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SubjectsActivity.this);
        mBuilder.setTitle(R.string.label_subject);
        mBuilder.setCancelable(false);

        mBuilder.setMultiChoiceItems(subjectTitles, selectedSubjects, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {

                String value = indexIdMap.get(position);
                if(isChecked && !selectedSubjectIds.contains(value)) {
                    selectedSubjectIds.add(value);
                }
                else if(selectedSubjectIds.contains(value)){
                    selectedSubjectIds.remove(value);
                }
            }
        });

        mBuilder.setPositiveButton(R.string.label_set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                    ids = selectedSubjectIds;
            }
        });

        mBuilder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        mBuilder.setNeutralButton(R.string.clear_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                clearSelectedSubjects();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    public void filterStartTime(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SubjectsActivity.this);
        builder.setTitle(getString(R.string.label_start_time));
        builder.setCancelable(false);

        View prompt = LayoutInflater.from(this).inflate(R.layout.layout_start_time, null);
        builder.setView(prompt);

        final EditText startTimeInput = prompt.findViewById(R.id.start_time_input);
        startTimeInput.setText(startTime);

        builder.setPositiveButton(getString(R.string.label_set), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startTime = startTimeInput.getText().toString();
            }
        });
        builder.setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void filterLevel(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SubjectsActivity.this);
        builder.setTitle(getString(R.string.label_level));
        builder.setCancelable(false);

        final View prompt = LayoutInflater.from(this).inflate(R.layout.layout_level, null);
        final RadioGroup levelGroup = prompt.findViewById(R.id.level_group);

        if(level.equals(getString(R.string.value_lower)))
            ((RadioButton)levelGroup.getChildAt(0)).setChecked(true);
        else if(level.equals(getString(R.string.value_upper)))
            ((RadioButton)levelGroup.getChildAt(1)).setChecked(true);
        else if(level.equals(getString(R.string.value_graduate)))
            ((RadioButton)levelGroup.getChildAt(2)).setChecked(true);
        else
            ((RadioButton)levelGroup.getChildAt(3)).setChecked(true);

        builder.setView(prompt);

        builder.setPositiveButton(getString(R.string.label_set), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RadioButton selectedLevel = prompt.findViewById(levelGroup.getCheckedRadioButtonId());
                selectedLevel.setChecked(true);
                level = selectedLevel.getText().toString().toLowerCase();
                if(level.equals(getString(R.string.value_all)))
                    level = "";
            }
        });
        builder.setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void clearSelectedSubjects() {
        for (int i = 0; i < selectedSubjects.length; i++) {
            selectedSubjects[i] = false;
            selectedSubjectIds.clear();
        }
        ids.clear();
    }

    public void clearFilters(View view) {
        clearSelectedSubjects();
        level = "";
        startTime = "";
    }

    public void fireFilteredIntent(View view) {
        if(ids.size() == 0 && startTime.isEmpty() && level.isEmpty())
            Toast.makeText(this, getString(R.string.no_filters), Toast.LENGTH_LONG).show();
        else if(ids.size() != 0) {
            int[] subjectIds = new int[ids.size()];
            for(int i=0; i<ids.size(); i++)
                subjectIds[i] = Integer.parseInt(ids.get(i));

            Intent classesIntent = new Intent(getApplicationContext(),
                    ClassesActivity.class);
            classesIntent.putExtra(getString(R.string.key_subject_ids), subjectIds);
            classesIntent.putExtra(getString(R.string.key_starttime), startTime);
            classesIntent.putExtra(getString(R.string.key_level), level);
            startActivity(classesIntent);
        }
        else {
            Toast.makeText(this, getString(R.string.no_subject_selected), Toast.LENGTH_LONG).show();
        }
    }
}
