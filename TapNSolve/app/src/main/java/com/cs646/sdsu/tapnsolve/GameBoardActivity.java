package com.cs646.sdsu.tapnsolve;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.Switch;
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

import java.util.Locale;
import java.util.Random;

import java.lang.Math;

public class GameBoardActivity extends AppCompatActivity {

    private String difficulty;
    private String mode;
    private TextView moves;
    private GridLayout puzzleContainer;
    private Button startPause;
    private Button reset;
    private Chronometer elapsedTime;
    private CountDownTimer countDownTimer;
    private TextView countDownTimerText;
    private Vibrator vibrator;
    private MediaPlayer swapSound;
    private MediaPlayer resetSound;
    private Switch toggleSound;
    private Switch toggleVibration;
    private Button signIn;
    private Button logOut;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private boolean isPaused;
    private boolean timed;
    private long pausedTime;
    private int gridDimension;
    private int buttonDimensionInDp;
    private long timeLeftInMillis;

    private static final long START_TIME_IN_MILLIS_EASY = 300000;
    private static final long START_TIME_IN_MILLIS_MEDIUM = 600000;
    private static final long START_TIME_IN_MILLIS_HARD = 1200000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(GameBoardActivity.this, gso);

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
                googleSignInClient.signOut().addOnCompleteListener(GameBoardActivity.this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(GameBoardActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                                toggleSignInButton(false);
                            }
                        });
            }
        });

        Intent intentFromMainMenu = getIntent();
        difficulty = intentFromMainMenu.getStringExtra(getString(R.string.key_difficulty));
        mode = intentFromMainMenu.getStringExtra(getString(R.string.key_mode));

        toggleSound = this.findViewById(R.id.id_toggle_sound);
        toggleSound.setChecked(true);

        toggleVibration = this.findViewById(R.id.id_toggle_vibration);
        toggleVibration.setChecked(true);

        swapSound = MediaPlayer.create(GameBoardActivity.this, R.raw.swap);
        resetSound = MediaPlayer.create(GameBoardActivity.this, R.raw.reset);

        moves = this.findViewById(R.id.id_moves_taken);
        elapsedTime = this.findViewById(R.id.id_elapsed_time);
        countDownTimerText = this.findViewById(R.id.id_count_down_timer);

        timed = mode.equals(getString(R.string.timed));

        initializePuzzle();

        startPause = this.findViewById(R.id.id_start_pause_button);
        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((Button)view).getText().toString().equals(getString(R.string.start))) {
                    ((Button)view).setText(R.string.pause);
                    isPaused = false;
                    if(timed) {
                        startCountDownTimer();
                    }
                    else {
                        if(pausedTime != 0) {
                            elapsedTime.setBase(elapsedTime.getBase() + SystemClock.elapsedRealtime() - pausedTime);
                        }
                        else {
                            elapsedTime.setBase(SystemClock.elapsedRealtime());
                        }
                        elapsedTime.start();
                    }
                }
                else {
                    ((Button)view).setText(R.string.start);
                    isPaused = true;
                    if(timed) {
                        pauseCountDownTimer();
                    }
                    else {
                        pausedTime = SystemClock.elapsedRealtime();
                        elapsedTime.stop();
                    }
                }
            }
        });

        reset = this.findViewById(R.id.id_reset_button);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isPaused) {
                    if(timed) {
                        pauseCountDownTimer();
                    }
                    else {
                        isPaused = true;
                        startPause.setText(R.string.start);
                        pausedTime = SystemClock.elapsedRealtime();
                        elapsedTime.stop();
                    }
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameBoardActivity.this);
                alertDialog.setMessage(getString(R.string.msg_reset));
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
                    }
                });
                alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!isPaused) {
                            isPaused = false;
                            elapsedTime.setBase(elapsedTime.getBase() + SystemClock.elapsedRealtime() - pausedTime);
                            elapsedTime.start();
                        }
                        dialog.cancel();
                    }
                });

                AlertDialog alert = alertDialog.create();
                alert.setTitle(R.string.title_reset);
                alert.show();
            }
        });

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onBackPressed() {
        if(!isPaused) {
            if(timed) {
                pauseCountDownTimer();
            }
            else {
                isPaused = true;
                pausedTime = SystemClock.elapsedRealtime();
                elapsedTime.stop();
                startPause.setText(R.string.start);
            }
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameBoardActivity.this);
        alertDialog.setMessage(getString(R.string.msg_quit_round));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goToMainMenu();
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isPaused) {
                    isPaused = false;
                    elapsedTime.setBase(elapsedTime.getBase() + SystemClock.elapsedRealtime() - pausedTime);
                    elapsedTime.start();
                }
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.setTitle(R.string.title_quit_round);
        alert.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        toggleSignInButton(currentUser != null);
    }

    private void initializePuzzle() {

        if(timed) {
            if(difficulty.equals(getString(R.string.easy)))
                timeLeftInMillis = START_TIME_IN_MILLIS_EASY;
            else if(difficulty.equals(getString(R.string.medium)))
                timeLeftInMillis = START_TIME_IN_MILLIS_MEDIUM;
            else if(difficulty.equals(getString(R.string.hard)))
                timeLeftInMillis = START_TIME_IN_MILLIS_HARD;

            updateCountDownText();
        }

        toggleTimer(timed);

        if(toggleSound.isChecked())
            resetSound.start();

        if(difficulty.equals(getString(R.string.easy))) {
            gridDimension = 3;
            buttonDimensionInDp = 75;
        }
        else if(difficulty.equals(getString(R.string.medium))) {
            gridDimension = 4;
            buttonDimensionInDp = 65;
        }
        else if(difficulty.equals(getString(R.string.hard))) {
            gridDimension = 5;
            buttonDimensionInDp = 55;
        }

        isPaused = true;
        moves.setText(R.string.moves_placeholder);

        puzzleContainer = findViewById(R.id.id_puzzle_container);
        puzzleContainer.setColumnCount(gridDimension);
        puzzleContainer.setRowCount(gridDimension);

        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, buttonDimensionInDp,
                                                resources.getDisplayMetrics());

        for(int i = 0; i < (gridDimension * gridDimension); i++) {
            Button button = new Button(GameBoardActivity.this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = px;
            params.height = px;
            params.setMargins(5, 5, 5, 5);
            button.setLayoutParams(params);
            button.setBackgroundColor(Color.WHITE);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            button.setTag(i);

            if(i+1 != gridDimension * gridDimension)
                button.setText(""+(i+1));
            else {
                button.setText("");
                button.setBackgroundColor(Color.BLACK);
            }

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isPaused) {
                        Toast.makeText(GameBoardActivity.this, R.string.press_start, Toast.LENGTH_SHORT).show();
                        if(toggleVibration.isChecked())
                            vibrator.vibrate(100);
                        return;
                    }
                    Button left;
                    Button right;
                    Button top;
                    Button bottom;
                    Button clicked = (Button)view;

                    int clickedTile = Integer.parseInt(view.getTag().toString());

                    if(difficulty.equals(getString(R.string.easy))) {
                        switch(clickedTile) {
                            case 0:
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 1:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 2:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 3:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 4:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 5:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+3);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 6:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                break;

                            case 7:
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                break;
                            case 8:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-3);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                break;
                        }
                    }
                    else if(difficulty.equals(getString(R.string.medium))) {
                        switch(clickedTile) {
                            case 0:
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 1:
                            case 2:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 3:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 4:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 5:
                            case 6:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 7:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 8:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 9:
                            case 10:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 11:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 12:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                break;

                            case 13:
                            case 14:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                break;

                            case 15:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-4);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                break;
                        }
                    }
                    else if(difficulty.equals(getString(R.string.hard))) {
                        switch(clickedTile) {
                            case 0:
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 1:
                            case 2:
                            case 3:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 4:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 5:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 6:
                            case 7:
                            case 8:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 9:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 10:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 11:
                            case 12:
                            case 13:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 14:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 15:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 16:
                            case 17:
                            case 18:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 19:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                bottom = (Button)puzzleContainer.getChildAt(clickedTile+5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(bottom.getText().toString().equals(""))
                                    swapTiles(clicked, bottom);
                                break;

                            case 20:
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                break;

                            case 21:
                            case 22:
                            case 23:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                right = (Button)puzzleContainer.getChildAt(clickedTile+1);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                else if(right.getText().toString().equals(""))
                                    swapTiles(clicked, right);
                                break;

                            case 24:
                                left = (Button)puzzleContainer.getChildAt(clickedTile-1);
                                top = (Button)puzzleContainer.getChildAt(clickedTile-5);
                                if(left.getText().toString().equals(""))
                                    swapTiles(clicked, left);
                                else if(top.getText().toString().equals(""))
                                    swapTiles(clicked, top);
                                break;
                        }
                    }

                }
            });

            puzzleContainer.addView(button);
        }

        shufflePuzzle();
    }

    private void swapTiles(Button from, Button to) {
        String swap = from.getText().toString();
        from.setText(to.getText());
        to.setText(swap);

        from.setBackgroundColor(Color.BLACK);
        to.setBackgroundColor(Color.WHITE);

        if(toggleSound.isChecked())
            swapSound.start();

        moves.setText((Integer.parseInt(moves.getText().toString()) + 1) + "");

        if(isSolved())
            displaySuccessActivity();
    }

    private void shufflePuzzle() {
        int[] numbers = new int[puzzleContainer.getChildCount()];
        for(int i = 0; i < numbers.length; i++)
            numbers[i] = i+1;
        numbers = randomize(numbers, numbers.length);

        for(int i = 0; i < puzzleContainer.getChildCount(); i++) {
            Button button = (Button) puzzleContainer.getChildAt(i);
            button.setBackgroundColor(Color.WHITE);
            if(numbers[i] != gridDimension * gridDimension)
                button.setText(""+numbers[i]);
            else {
                button.setText("");
                button.setBackgroundColor(Color.BLACK);
            }
        }
    }

    public int[] randomize( int[] arr, int n)
    {
        Random r = new Random();
        for (int i = n-1; i > 0; i--) {
            int j = r.nextInt(i);

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        return arr;
    }

    private void reset() {
        if(toggleSound.isChecked())
            resetSound.start();
        isPaused = true;
        startPause.setText(R.string.start);
        moves.setText(R.string.moves_placeholder);
        if(timed) {
            if(difficulty.equals(getString(R.string.easy)))
                timeLeftInMillis = START_TIME_IN_MILLIS_EASY;
            else if(difficulty.equals(getString(R.string.medium)))
                timeLeftInMillis = START_TIME_IN_MILLIS_MEDIUM;
            else if(difficulty.equals(getString(R.string.hard)))
                timeLeftInMillis = START_TIME_IN_MILLIS_HARD;
            updateCountDownText();
        }
        else {
            elapsedTime.stop();
            elapsedTime.setBase(SystemClock.elapsedRealtime());
            pausedTime = 0;
        }
        shufflePuzzle();
    }

    private boolean isSolved() {
        for(int i = 0; i < puzzleContainer.getChildCount(); i++) {
            Button button = (Button) puzzleContainer.getChildAt(i);
            String value = button.getText().toString();
            if(i+1 != puzzleContainer.getChildCount() && !value.equals((i+1)+""))
                return false;
        }
        return true;
    }

    private void displaySuccessActivity() {
        isPaused = true;
        startPause.setText(R.string.start);
        elapsedTime.stop();
        long timeElapsed = SystemClock.elapsedRealtime() - elapsedTime.getBase();
        int hours = (int) (timeElapsed / 3600000);
        int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
        int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

        String timeTaken = "";

        if(minutes < 10)
            timeTaken += "0" + minutes + ":";
        else
            timeTaken += "" + minutes + ":";

        if(seconds < 10)
            timeTaken += "0" + seconds;
        else
            timeTaken += "" + seconds;

        int score = calculateScore(timeElapsed, Integer.parseInt(moves.getText().toString()));

        Intent goToSuccessActivity = new Intent(GameBoardActivity.this, SuccessActivity.class);
        goToSuccessActivity.putExtra(getString(R.string.key_difficulty), difficulty);
        goToSuccessActivity.putExtra(getString(R.string.key_mode), mode);
        goToSuccessActivity.putExtra(getString(R.string.key_time), timeTaken);
        goToSuccessActivity.putExtra(getString(R.string.key_moves), Integer.parseInt(moves.getText().toString()));
        goToSuccessActivity.putExtra(getString(R.string.key_score), score);
        startActivity(goToSuccessActivity);
        finish();
    }

    private void goToMainMenu() {
        Intent goToMainMenu = new Intent(GameBoardActivity.this, MainMenuActivity.class);
        startActivity(goToMainMenu);
        finish();
    }

    private int calculateScore(long time, int moves) {
        long multiplier = 0;

        if(time == 0)
            time = 1;

        if(moves == 0)
            moves = 1;

        if(difficulty.equals(getString(R.string.easy)))
            multiplier = 10000;
        else if(difficulty.equals(getString(R.string.medium)))
            multiplier = 50000;
        else if(difficulty.equals(getString(R.string.hard)))
            multiplier = 100000;

        return (int) ((1/Math.sqrt(time) + 1/moves) * multiplier);
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
                            Toast.makeText(GameBoardActivity.this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                            toggleSignInButton(true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(GameBoardActivity.this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
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

    private void toggleTimer(boolean timed) {
        if(timed) {
            elapsedTime.setVisibility(View.GONE);
            countDownTimerText.setVisibility(View.VISIBLE);
        }
        else {
            countDownTimerText.setVisibility(View.GONE);
            elapsedTime.setVisibility(View.VISIBLE);
        }
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countDownTimerText.setText(timeLeftFormatted);
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isPaused = true;
                startPause.setText(R.string.start);
                countDownTimerText.setText(R.string.timer_placeholder);
                if(!isSolved()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(GameBoardActivity.this);
                    alertDialog.setMessage(getString(R.string.msg_challenge_failed));
                    alertDialog.setCancelable(false);
                    alertDialog.setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        reset();
                        }
                    });
                    alertDialog.setNegativeButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        goToMainMenu();
                        }
                    });

                    AlertDialog alert = alertDialog.create();
                    alert.setTitle(R.string.challenge_failed);
                    alert.show();
                }
            }
        }.start();

        isPaused = false;
        startPause.setText(R.string.pause);
    }

    private void pauseCountDownTimer() {
        countDownTimer.cancel();
        isPaused = true;
        startPause.setText(R.string.start);
    }

}
