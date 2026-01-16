package com.codecricket.services;

import com.codecricket.api.LiveScoreApi;
import com.codecricket.model.MatchItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class LiveMatchesService {

    private static final String LIVE_URL =
            "https://cricbuzz-cricket.p.rapidapi.com/matches/v1/live";

    public static List<MatchItem> fetchLiveMatches() throws Exception {

        String json = LiveScoreApi.get(LIVE_URL);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        List<MatchItem> list = new ArrayList<>();

        for (JsonNode type : root.path("typeMatches")) {
            for (JsonNode series : type.path("seriesMatches")) {

                JsonNode wrapper = series.path("seriesAdWrapper");
                if (wrapper.isMissingNode()) continue;

                for (JsonNode match : wrapper.path("matches")) {

                    JsonNode info = match.path("matchInfo");
                    JsonNode score = match.path("matchScore");

                    long matchId = info.path("matchId").asLong();

                    String team1 = info.path("team1").path("teamName").asText();
                    String team2 = info.path("team2").path("teamName").asText();

                    String team1Score = "";
                    String team2Score = "";

                    team1Score = buildTeamScore(score.path("team1Score"));
                    team2Score = buildTeamScore(score.path("team2Score"));

                    String status = info.path("status").asText();

                    list.add(new MatchItem(
                            matchId,
                            team1,
                            team1Score,
                            team2,
                            team2Score,
                            status
                    ));
                }
            }
        }
        return list;
    }

    private static String buildTeamScore(JsonNode teamScore) {

        if (teamScore == null || teamScore.isMissingNode())
            return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= 4; i++) {

            JsonNode inngs = teamScore.path("inngs" + i);
            if (inngs.isMissingNode()) continue;

            int runs = inngs.path("runs").asInt();
            int wkts = inngs.path("wickets").asInt(0);
            double overs = inngs.path("overs").asDouble(0);

            if (sb.length() > 0) sb.append(" & ");

            sb.append(runs);

            if (wkts >= 0) {
                sb.append("/").append(wkts);
            }

            if (overs > 0) {
                sb.append(" (").append(formatOvers(overs)).append(")");
            }

            if (inngs.path("isDeclared").asBoolean(false)) {
                sb.append(" d");
            }

            if (inngs.path("isFollowOn").asBoolean(false)) {
                sb.append(" f/o");
            }
        }

        return sb.toString();
    }

    private static String formatOvers(double overs) {
        int whole = (int) overs;
        int balls = (int) Math.round((overs - whole) * 10);

        if (balls == 6) {
            return String.valueOf(whole + 1);
        }
        return balls == 0 ? String.valueOf(whole) : whole + "." + balls;
    }
}
