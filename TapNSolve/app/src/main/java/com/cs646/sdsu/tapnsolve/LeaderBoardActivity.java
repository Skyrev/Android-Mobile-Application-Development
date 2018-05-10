package com.cs646.sdsu.tapnsolve;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoardActivity extends AppCompatActivity {

    private ArrayList<LeaderBoardEntry> leaderBoardEntries;
    private LinearLayout leaderBoardContainer;
    private TextView emptyLeaderBoard;
    private ProgressBar progressBar;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        emptyLeaderBoard = this.findViewById(R.id.id_empty_leaderboard_msg);
        emptyLeaderBoard.setVisibility(View.GONE);
        leaderBoardContainer = this.findViewById(R.id.id_leaderboard_container);
        progressBar = this.findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.VISIBLE);
        typeface = ResourcesCompat.getFont(LeaderBoardActivity.this, R.font.reem_kufi);

        leaderBoardEntries = new ArrayList<>();
        final DatabaseReference leaderBoard = FirebaseDatabase.getInstance().getReference(getString(R.string.key_leaderboard));
        leaderBoard.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                leaderBoardEntries.clear();
                leaderBoardContainer.removeAllViews();
                for(DataSnapshot entry : dataSnapshot.getChildren()) {
                    leaderBoardEntries.add(entry.getValue(LeaderBoardEntry.class));
                }
                Collections.sort(leaderBoardEntries, new Comparator<LeaderBoardEntry>() {
                    @Override
                    public int compare(LeaderBoardEntry o1, LeaderBoardEntry o2) {
                        return o2.getScore().compareTo(o1.getScore());
                    }
                });
                if(leaderBoardEntries.isEmpty()) {
                    emptyLeaderBoard.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    emptyLeaderBoard.setVisibility(View.GONE);
                    buildLeaderBoard();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void buildLeaderBoard() {
        long i = 1;
        for(LeaderBoardEntry entry : leaderBoardEntries) {
            LinearLayout leaderBoardEntryLayout = new LinearLayout(getApplicationContext());
            leaderBoardEntryLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 30);
            leaderBoardEntryLayout.setLayoutParams(params);
            leaderBoardEntryLayout.setBackground(ContextCompat.getDrawable(LeaderBoardActivity.this, R.drawable.border_black));

            TextView rankView = new TextView(getApplicationContext());
            LinearLayout.LayoutParams rankParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            rankView.setLayoutParams(rankParams);
            rankView.setTypeface(typeface);
            rankView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            rankView.setTextColor(Color.BLACK);
            rankView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            rankView.setText(i+"");

            String details = "";
            details += entry.getUsername() + "\n";
            details += getString(R.string.difficulty) + ": " + entry.getDifficulty() + "\n";
            details += getString(R.string.mode) + ": " + entry.getMode() + "\n";
            details += getString(R.string.time) + ": " + entry.getTime() + "\n";
            details += getString(R.string.moves) + ": " + entry.getMoves();
            TextView detailsView = new TextView(getApplicationContext());
            LinearLayout.LayoutParams detailsParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            detailsView.setLayoutParams(detailsParams);
            detailsView.setTypeface(typeface);
            detailsView.setText(details);
            detailsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            detailsView.setTextColor(Color.BLACK);

            TextView scoreView = new TextView(getApplicationContext());
            LinearLayout.LayoutParams scoreParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            scoreView.setLayoutParams(scoreParams);
            scoreView.setTypeface(typeface);
            scoreView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            scoreView.setText(entry.getScore());
            scoreView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            scoreView.setTextColor(Color.BLACK);

            leaderBoardEntryLayout.addView(rankView);
            leaderBoardEntryLayout.addView(detailsView);
            leaderBoardEntryLayout.addView(scoreView);

            leaderBoardContainer.addView(leaderBoardEntryLayout);

            i++;
        }
        progressBar.setVisibility(View.GONE);
    }
}
