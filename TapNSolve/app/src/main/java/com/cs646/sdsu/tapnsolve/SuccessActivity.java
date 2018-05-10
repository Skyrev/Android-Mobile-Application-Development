package com.cs646.sdsu.tapnsolve;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SuccessActivity extends AppCompatActivity {

    private Button signIn;
    private Button logOut;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(SuccessActivity.this, gso);

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
                googleSignInClient.signOut().addOnCompleteListener(SuccessActivity.this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(SuccessActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                                toggleSignInButton(false);
                            }
                        });
            }
        });

        TextView timeTakenView = this.findViewById(R.id.id_time_taken);
        TextView movesView = this.findViewById(R.id.id_moves_taken);
        TextView scoreView = this.findViewById(R.id.id_score);

        Intent intentFromGameBoard = getIntent();
        final String difficulty = intentFromGameBoard.getStringExtra(getString(R.string.key_difficulty));
        final String mode = intentFromGameBoard.getStringExtra(getString(R.string.key_mode));
        final String time = intentFromGameBoard.getStringExtra(getString(R.string.key_time));
        final int moves = intentFromGameBoard.getIntExtra(getString(R.string.key_moves), 0);
        final int score = intentFromGameBoard.getIntExtra(getString(R.string.key_score), 0);
        final String username = currentUser != null ? currentUser.getDisplayName() : getString(R.string.player);

        timeTakenView.setText(time);
        movesView.setText(moves);
        scoreView.setText(score);

        final DatabaseReference leaderBoardReference = FirebaseDatabase.getInstance().getReference(getString(R.string.key_leaderboard));

        Button mainMenu = this.findViewById(R.id.id_main_menu_button);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMainMenu = new Intent(SuccessActivity.this, MainMenuActivity.class);
                startActivity(goToMainMenu);
                finish();
            }
        });

        final Button publishScore = this.findViewById(R.id.id_publish_score_button);
        publishScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null) {
                    String userId = currentUser.getUid();
                    LeaderBoardEntry entry = new LeaderBoardEntry(userId, difficulty, mode, username, time, moves, score);
                    String id = leaderBoardReference.push().getKey();
                    leaderBoardReference.child(id).setValue(entry);
                    Toast.makeText(SuccessActivity.this, R.string.score_published, Toast.LENGTH_SHORT).show();
                    publishScore.setEnabled(false);
                }
                else
                    Toast.makeText(SuccessActivity.this, R.string.sign_in_to_publish_score, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        toggleSignInButton(currentUser != null);
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
                            Toast.makeText(SuccessActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                            toggleSignInButton(true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SuccessActivity.this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
                            toggleSignInButton(false);

                        }
                    }
                });
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
