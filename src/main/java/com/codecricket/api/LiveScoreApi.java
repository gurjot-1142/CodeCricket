package com.codecricket.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LiveScoreApi {

    private static final String API_KEY = "93326774e3msh2174c4a62e31d19p135426jsnb0732f9a16c5";
    private static final String HOST = "cricbuzz-cricket.p.rapidapi.com";

    public static String get(String url) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-key", API_KEY)
                .header("x-rapidapi-host", HOST)
                .GET()
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
