package com.codecricket.model;

public class MatchItem {

    private final long matchId;
    private final String team1;
    private final String team1Score;
    private final String team2;
    private final String team2Score;
    private final String status;

    public MatchItem(
            long matchId,
            String team1,
            String team1Score,
            String team2,
            String team2Score,
            String status
    ) {
        this.matchId = matchId;
        this.team1 = team1;
        this.team1Score = team1Score;
        this.team2 = team2;
        this.team2Score = team2Score;
        this.status = status;
    }

    public long getMatchId() { return matchId; }
    public String getTeam1() { return team1; }
    public String getTeam1Score() { return team1Score; }
    public String getTeam2() { return team2; }
    public String getTeam2Score() { return team2Score; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return team1 + " " + team1Score +
                "  vs  " +
                team2 + " " + team2Score +
                "\n" + status;
    }
}
