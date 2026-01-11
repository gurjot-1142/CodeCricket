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

                    long matchId = info.path("matchId").asLong();

                    String title =
                            info.path("team1").path("teamSName").asText()
                                    + " vs "
                                    + info.path("team2").path("teamSName").asText()
                                    + " — "
                                    + info.path("matchDesc").asText()
                                    + " ("
                                    + info.path("stateTitle").asText()
                                    + ")";

                    list.add(new MatchItem(matchId, title));
                }
            }
        }
        return list;
    }
}
