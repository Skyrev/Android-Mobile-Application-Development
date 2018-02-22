package com.cs646.skyrev.personalinformation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalInformationActivity extends AppCompatActivity {

    EditText firstNameInput;
    EditText lastNameInput;
    EditText ageInput;
    EditText emailInput;
    EditText phoneInput;

    TextView majorInput;

    Button clearSelectedMajorButton;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);
        sharedPreferences = getPreferences(MODE_PRIVATE);
        firstNameInput = this.findViewById(R.id.first_name_input);
        lastNameInput = this.findViewById(R.id.last_name_input);
        ageInput = this.findViewById(R.id.age_input);
        emailInput = this.findViewById(R.id.email_input);
        phoneInput = this.findViewById(R.id.phone_input);
        majorInput = this.findViewById(R.id.major_input);
        clearSelectedMajorButton = this.findViewById(R.id.clear_selected_major);
        loadInformation();
    }

    // This methods starts the MajorSelection activity via an intent
    public void selectMajor(View view) {
        Intent majorSelectionIntent = new Intent(this, MajorSelectionActivity.class);
        final int result = 1;
        startActivityForResult(majorSelectionIntent, result);
    }

    // This method is called by the system when a response is received from the MajorSelection activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            majorInput.setText(data.getStringExtra("selectedMajor"));
            majorInput.setTextColor(Color.BLACK);
            clearSelectedMajorButton.setVisibility(View.VISIBLE);
        }
    }

    // This method clears the selected major
    public void clearSelectedMajor(View view) {
        majorInput.setText(getString(R.string.select_a_major));
        majorInput.setTextColor(Color.GRAY);
        clearSelectedMajorButton.setVisibility(View.INVISIBLE);
    }

    // This method checks for any errors and
    // stores information in the shared preferences
    public void saveInformation(View view) {
        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        String email = emailInput.getText().toString();
        String major = majorInput.getText().toString();

        int age = ageInput.getText().toString().equals("") ? 0 :
                Integer.parseInt(ageInput.getText().toString());

        long phone = phoneInput.getText().toString().equals("") ? 0L :
                Long.parseLong(phoneInput.getText().toString());

        boolean error = false;

        if(firstName.trim().equals("")) {
            firstNameInput.setError(getString(R.string.enter_first_name));
            firstNameInput.requestFocus();
            error = true;
        }

        if(lastName.trim().equals("")) {
            lastNameInput.setError(getString(R.string.enter_last_name));
            lastNameInput.requestFocus();
            error = true;
        }

        if(age < 1) {
            ageInput.setError(getString(R.string.enter_age));
            ageInput.requestFocus();
            error = true;
        }

        if(email.trim().equals("")) {
            emailInput.setError(getString(R.string.enter_email));
            emailInput.requestFocus();
            error = true;
        }

        if(phone < 1111111111L || phone > 9999999999L) {
            phoneInput.setError(getString(R.string.enter_valid_phone));
            phoneInput.requestFocus();
            error = true;
        }

        if(major.equals(getString(R.string.select_a_major))){
            majorInput.setTextColor(Color.RED);
            clearSelectedMajorButton.setVisibility(View.INVISIBLE);
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            majorInput.startAnimation(shake);
            error = true;
        }

        if(!error) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("first-name", firstName);
            editor.putString("last-name", lastName);
            editor.putInt("age", age);
            editor.putString("email", email);
            editor.putLong("phone", phone);
            editor.putString("major", major);
            editor.commit();
            majorInput.setTextColor(Color.BLACK);
            clearSelectedMajorButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.toast_information_saved, Toast.LENGTH_SHORT).show();
        } else {
            // In case of any errors, vibrate device for 300 milliseconds
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            try {
                vibrator.vibrate(300);
            }
            catch (NullPointerException exception) {
                // Well... do nothing
            }
        }
    }

    // This method clears the saved information from the shared preferences
    public void clearSharedPreferences(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        Toast.makeText(this, R.string.toast_information_cleared, Toast.LENGTH_SHORT).show();
        clearErrors();
        loadInformation();
    }

    // This method clears errors from the input fields
    public void clearErrors() {
        firstNameInput.setError(null);
        lastNameInput.setError(null);
        ageInput.setError(null);
        emailInput.setError(null);
        phoneInput.setError(null);
    }

    // This method loads the saved information from shared preferences to the activity
    public void loadInformation() {
        String firstName = sharedPreferences.getString("first-name", "");
        firstName = firstName.equals("") ? null : firstName;
        firstNameInput.setText(firstName);

        String lastName = sharedPreferences.getString("last-name", "");
        lastName = lastName.equals("") ? null : lastName;
        lastNameInput.setText(lastName);

        String age = sharedPreferences.getInt("age", 0) + "";
        age = age.equals("0") ? null : age;
        ageInput.setText(age);

        String email = sharedPreferences.getString("email", "");
        email = email.equals("") ? null : email;
        emailInput.setText(email);

        String phone = sharedPreferences.getLong("phone", 0) + "";
        phone = phone.equals("0") ? null : phone;
        phoneInput.setText(phone);

        String major = sharedPreferences.getString("major", "");
        major = major.equals("") ? getString(R.string.select_a_major) : major;
        majorInput.setText(major);

        if(major.equals(getString(R.string.select_a_major))) {
            clearSelectedMajorButton.setVisibility(View.INVISIBLE);
            majorInput.setTextColor(Color.GRAY);
        }
        else {
            clearSelectedMajorButton.setVisibility(View.VISIBLE);
            majorInput.setTextColor(Color.BLACK);
        }
    }
}
