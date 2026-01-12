package com.codecricket.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LiveScoreApi {

    private static final String API_KEY = "723ce3c2b4msh480a997d6b83e5bp130afcjsncf8980d14244";
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
