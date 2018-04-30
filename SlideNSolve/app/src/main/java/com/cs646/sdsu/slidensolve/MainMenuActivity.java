package com.cs646.sdsu.slidensolve;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {

    private RadioGroup difficultyGroup;
    private RadioGroup modeGroup;
    private RadioButton selectedDifficulty;
    private RadioButton selectedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

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
                    Toast.makeText(MainMenuActivity.this, "Select Difficulty and Mode",
                                    Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent gameBoard = new Intent(MainMenuActivity.this, GameBoardActivity.class);
                    gameBoard.putExtra("difficulty", selectedDifficulty.getText());
                    gameBoard.putExtra("mode", selectedMode.getText());
                    startActivity(gameBoard);
                }
            }
        });
    }
}
