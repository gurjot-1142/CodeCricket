package com.codecricket.services;

import com.codecricket.api.LiveScoreApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MatchInfoService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode load(long matchId) throws Exception {
        String json = LiveScoreApi.get(
                "https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/" + matchId
        );
        return MAPPER.readTree(json);
    }
}
