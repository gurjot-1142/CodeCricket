package com.codecricket.model;

public class MatchItem {

    private final long matchId;
    private final String title;

    public MatchItem(long matchId, String title) {
        this.matchId = matchId;
        this.title = title;
    }

    public long getMatchId() {
        return matchId;
    }

    @Override
    public String toString() {
        return title;
    }
}
