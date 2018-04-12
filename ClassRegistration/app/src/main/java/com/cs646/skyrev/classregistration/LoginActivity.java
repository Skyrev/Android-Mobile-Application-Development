package com.cs646.skyrev.classregistration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputSdsuRedId;
    private EditText inputEmailId;
    private EditText inputPassword;
    private JSONObject parameters;
    private LinearLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputFirstName = this.findViewById(R.id.input_first_name);
        inputLastName = this.findViewById(R.id.input_last_name);
        inputSdsuRedId = this.findViewById(R.id.input_sdsu_red_id);
        inputEmailId = this.findViewById(R.id.input_email_id);
        inputPassword = this.findViewById(R.id.input_password);

        progressBar = this.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadInformation();
    }

    private void loadInformation() {
        String firstName = sharedPreferences.getString(getString(R.string.key_firstname), "");
        firstName = firstName.equals("") ? null : firstName;
        inputFirstName.setText(firstName);

        String lastName = sharedPreferences.getString(getString(R.string.key_lastname), "");
        lastName = lastName.equals("") ? null : lastName;
        inputLastName.setText(lastName);

        String redId = sharedPreferences.getString(getString(R.string.key_redid), "");
        redId = redId.equals("") ? null : redId;
        inputSdsuRedId.setText(redId);

        String email = sharedPreferences.getString(getString(R.string.key_email), "");
        email = email.equals("") ? null : email;
        inputEmailId.setText(email);

        String password = sharedPreferences.getString(getString(R.string.key_password), "");
        password = password.equals("") ? null : password;
        inputPassword.setText(password);
    }

    private boolean isInputValid() {
        String firstName = inputFirstName.getText().toString();
        String lastName = inputLastName.getText().toString();
        String sdsuRedId = inputSdsuRedId.getText().toString();
        long sdsuRedIdLong = sdsuRedId.equals("") ? 0 : Long.parseLong(sdsuRedId);
        String emailId = inputEmailId.getText().toString();
        String password = inputPassword.getText().toString();

        boolean error = false;

        if(firstName.isEmpty()) {
            inputFirstName.setError(getString(R.string.error_first_name));
            inputFirstName.requestFocus();
            error = true;
        }
        if(lastName.isEmpty()) {
            inputLastName.setError(getString(R.string.error_last_name));
            inputLastName.requestFocus();
            error = true;
        }
        if(sdsuRedId.isEmpty() || sdsuRedId.length() != 9
                || sdsuRedIdLong < 0L || sdsuRedIdLong > 999999999L) {
            inputSdsuRedId.setError(getString(R.string.error_sdsu_red_id));
            inputSdsuRedId.requestFocus();
            error = true;
        }
        if(emailId.isEmpty() || !emailId.contains("@")) {
            inputEmailId.setError(getString(R.string.error_email_id));
            inputEmailId.requestFocus();
            error = true;
        }

        Set<Character> uniqueChars = new HashSet<Character>();
        for(char c : password.toCharArray()) {
            uniqueChars.add(c);
        }
        if(password.isEmpty() || password.length() < 8 || uniqueChars.size() < 7) {
            inputPassword.setError(getString(R.string.error_password));
            inputPassword.requestFocus();
            error = true;
        }

        if(error) {
            // In case of any errors, vibrate device for 300 milliseconds
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            try {
                vibrator.vibrate(300);
            }
            catch (NullPointerException exception) {
                // Well... do nothing
            }
            return false;
        }

        return true;
    }

    public void login(View view) {
        if(isInputValid()) {
            Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if(response.has(getString(R.string.key_ok)) ||
                                (response.has(getString(R.string.key_error))
                                        && response.getString(getString(R.string.key_error))
                                        .equals(getString(R.string.response_duplicate_red_id)))) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.key_firstname),
                                    inputFirstName.getText().toString());
                            editor.putString(getString(R.string.key_lastname),
                                    inputLastName.getText().toString());
                            editor.putString(getString(R.string.key_redid),
                                    inputSdsuRedId.getText().toString());
                            editor.putString(getString(R.string.key_email),
                                    inputEmailId.getText().toString());
                            editor.putString(getString(R.string.key_password),
                                    inputPassword.getText().toString());
                            editor.commit();

                            Intent openDashboard = new Intent(getApplicationContext(),
                                                                DashboardActivity.class);
                            startActivity(openDashboard);

                        } else if(response.has(getString(R.string.key_error))) {
                            if(response.getString(getString(R.string.key_error))
                                    .equals(getString(R.string.response_invalid_red_id))) {
                                inputSdsuRedId.setError(getString(R.string.error_sdsu_red_id));
                            } else if(response.getString(getString(R.string.key_error))
                                    .equals(getString(R.string.response_invalid_email))) {
                                inputSdsuRedId.setError(getString(R.string.error_email_id));
                            } else if(response.getString(getString(R.string.key_error))
                                    .equals(getString(R.string.response_invalid_password))) {
                                inputSdsuRedId.setError(getString(R.string.error_password));
                            }
                        }
                    } catch (JSONException exception) {
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

            String url = getString(R.string.url_add_student);
            parameters = new JSONObject();
            try {
                parameters.put(getString(R.string.key_firstname),
                        inputFirstName.getText().toString());
                parameters.put(getString(R.string.key_lastname),
                        inputLastName.getText().toString());
                parameters.put(getString(R.string.key_redid),
                        inputSdsuRedId.getText().toString());
                parameters.put(getString(R.string.key_password),
                        inputPassword.getText().toString());
                parameters.put(getString(R.string.key_email),
                        inputEmailId.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest registerStudent =
                    new JsonObjectRequest(url, parameters, success, failure);
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(registerStudent);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void reset(View view) {
        inputFirstName.setText(null);
        inputLastName.setText(null);
        inputSdsuRedId.setText(null);
        inputEmailId.setText(null);
        inputPassword.setText(null);
        clearErrors();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    private void clearErrors() {
        inputFirstName.setError(null);
        inputLastName.setError(null);
        inputSdsuRedId.setError(null);
        inputEmailId.setError(null);
        inputPassword.setError(null);
    }
}
