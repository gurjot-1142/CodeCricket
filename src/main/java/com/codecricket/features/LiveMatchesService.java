package com.codecricket.features;

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

                    String team1Score = scoreLine(score.path("team1Score"));
                    String team2Score = scoreLine(score.path("team2Score"));

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

    private static String scoreLine(JsonNode teamScore) {

        if (teamScore == null || teamScore.isMissingNode())
            return "";

        JsonNode inngs = teamScore.path("inngs1");
        if (inngs.isMissingNode())
            return "";

        int runs = inngs.path("runs").asInt();
        int wkts = inngs.path("wickets").asInt();
        double overs = inngs.path("overs").asDouble();

        return runs + "/" + wkts + " (" + overs + ")";
    }
}
