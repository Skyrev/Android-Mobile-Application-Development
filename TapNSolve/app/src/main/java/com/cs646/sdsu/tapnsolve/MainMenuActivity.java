package com.cs646.sdsu.tapnsolve;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainMenuActivity extends AppCompatActivity {

    private RadioGroup difficultyGroup;
    private RadioGroup modeGroup;
    private RadioButton selectedDifficulty;
    private RadioButton selectedMode;
    private Button signIn;
    private Button logOut;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(MainMenuActivity.this, gso);

        firebaseAuth = FirebaseAuth.getInstance();

        Button about = this.findViewById(R.id.id_about_button);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(MainMenuActivity.this, AboutActivity.class);
                startActivity(about);
            }
        });

        signIn = this.findViewById(R.id.id_sign_in);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

        logOut = this.findViewById(R.id.id_logout);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                googleSignInClient.signOut().addOnCompleteListener(MainMenuActivity.this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainMenuActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                                toggleSignInButton(false);
                            }
                        });
            }
        });


        difficultyGroup = this.findViewById(R.id.id_difficulty_chooser_group);
        difficultyGroup.check(difficultyGroup.getChildAt(0).getId());

        modeGroup = this.findViewById(R.id.id_mode_chooser_group);
        modeGroup.check(modeGroup.getChildAt(0).getId());

        Button start = this.findViewById(R.id.id_start);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                selectedDifficulty = findViewById(difficultyGroup.getCheckedRadioButtonId());
                selectedMode = findViewById(modeGroup.getCheckedRadioButtonId());

                if(selectedDifficulty == null || selectedMode == null) {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.select_difficulty_mode),
                                    Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent gameBoard = new Intent(MainMenuActivity.this, GameBoardActivity.class);
                    gameBoard.putExtra(getString(R.string.key_difficulty), selectedDifficulty.getText());
                    gameBoard.putExtra(getString(R.string.key_mode), selectedMode.getText());
                    startActivity(gameBoard);
                }
            }
        });

        Button leaderBoard = this.findViewById(R.id.id_leaderboard_button);
        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameBoard = new Intent(MainMenuActivity.this, LeaderBoardActivity.class);
                startActivity(gameBoard);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            toggleSignInButton(true);
        }
        else {
            toggleSignInButton(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                toggleSignInButton(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainMenuActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                            toggleSignInButton(true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainMenuActivity.this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
                            toggleSignInButton(false);

                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainMenuActivity.this);
        alertDialog.setMessage(getString(R.string.msg_quit_game));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
                System.exit(0);
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.setTitle(R.string.title_quit_game);
        alert.show();
    }

    private void toggleSignInButton(boolean signedIn) {
        if(signedIn) {
            signIn.setVisibility(View.GONE);
            logOut.setVisibility(View.VISIBLE);
        }
        else {
            logOut.setVisibility(View.GONE);
            signIn.setVisibility(View.VISIBLE);
        }
    }
}
