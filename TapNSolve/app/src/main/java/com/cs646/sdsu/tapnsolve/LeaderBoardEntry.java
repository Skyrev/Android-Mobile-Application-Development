package com.cs646.sdsu.tapnsolve;

/**
 * Created by skyrev on 5/5/18.
 */

public class LeaderBoardEntry {

    private String userId;
    private String username;
    private String time;
    private String moves;
    private String score;
    private String difficulty;
    private String mode;

    public LeaderBoardEntry() {
        //
    }

    public LeaderBoardEntry(String userId, String difficulty, String mode, String username, String time, String moves, String score) {
        this.userId = userId;
        this.difficulty = difficulty;
        this.mode = mode;
        this.username = username;
        this.time = time;
        this.moves = moves;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
