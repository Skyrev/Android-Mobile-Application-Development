package com.cs646.sdsu.slidensolve;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.os.Vibrator;
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

public class GameBoardActivity extends AppCompatActivity {

    private String difficulty;
    private String mode;
    private TextView moves;
    private GridLayout puzzleContainer;
    private Button startPause;
    private Button reset;
    private Chronometer elapsedTime;
    private Vibrator vibrator;
    private MediaPlayer swapSound;
    private MediaPlayer resetSound;
    private Switch toggleSound;
    private Switch toggleVibration;

    private boolean isPaused;
    private long pausedTime;
    private int gridDimension;
    private int buttonDimensionInDp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Intent intentFromMainMenu = getIntent();
        difficulty = intentFromMainMenu.getStringExtra("difficulty");
        mode = intentFromMainMenu.getStringExtra("mode");

        toggleSound = this.findViewById(R.id.id_toggle_sound);
        toggleSound.setChecked(true);

        toggleVibration = this.findViewById(R.id.id_toggle_vibration);
        toggleVibration.setChecked(true);

        swapSound = MediaPlayer.create(GameBoardActivity.this, R.raw.swap);
        resetSound = MediaPlayer.create(GameBoardActivity.this, R.raw.reset);

        moves = this.findViewById(R.id.id_moves);
        elapsedTime = this.findViewById(R.id.id_elapsed_time);

        initializePuzzle();

        startPause = this.findViewById(R.id.id_start_pause_button);
        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((Button)view).getText().toString().equals(getString(R.string.start))) {
                    ((Button)view).setText(R.string.pause);
                    isPaused = false;
                    if(pausedTime != 0) {
                        elapsedTime.setBase(elapsedTime.getBase() + SystemClock.elapsedRealtime() - pausedTime);
                    }
                    else {
                        elapsedTime.setBase(SystemClock.elapsedRealtime());
                    }

                    elapsedTime.start();
                }
                else {
                    ((Button)view).setText(R.string.start);
                    isPaused = true;
                    pausedTime = SystemClock.elapsedRealtime();
                    elapsedTime.stop();
                }
            }
        });

        reset = this.findViewById(R.id.id_reset_button);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isPaused = true;
                pausedTime = SystemClock.elapsedRealtime();
                elapsedTime.stop();

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
                        isPaused = false;
                        elapsedTime.setBase(elapsedTime.getBase() + SystemClock.elapsedRealtime() - pausedTime);
                        elapsedTime.start();
                        dialog.cancel();
                    }
                });

                AlertDialog alert = alertDialog.create();
                alert.setTitle(R.string.reset);
                alert.show();
            }
        });

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initializePuzzle() {
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
            buttonDimensionInDp = 60;
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
                        Toast.makeText(GameBoardActivity.this, "Press Start to move tiles", Toast.LENGTH_SHORT).show();
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
            displaySuccessMessage();
    }

    private void shufflePuzzle() {
        int[] numbers = new int[puzzleContainer.getChildCount()];
        for(int i = 0; i < numbers.length; i++)
            numbers[i] = i+1;
        numbers = UtilityMethods.randomize(numbers, numbers.length);

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

    private void reset() {
        if(toggleSound.isChecked())
            resetSound.start();
        isPaused = true;
        elapsedTime.stop();
        elapsedTime.setBase(SystemClock.elapsedRealtime());
        pausedTime = 0;
        startPause.setText(R.string.start);
        moves.setText(R.string.moves_placeholder);
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

    private void displaySuccessMessage() {
        isPaused = true;
        elapsedTime.stop();
        startPause.setText(R.string.start);
        AlertDialog.Builder builder = new AlertDialog.Builder(GameBoardActivity.this);
        builder.setMessage(getString(R.string.msg_solved))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.play_again), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        reset();
                    }
                })
                .setNegativeButton(getString(R.string.main_menu), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent gotoMainMenu = new Intent(GameBoardActivity.this, MainMenuActivity.class);
                        startActivity(gotoMainMenu);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
