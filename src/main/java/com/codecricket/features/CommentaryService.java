package com.codecricket.features;

import com.codecricket.api.LiveScoreApi;
import com.codecricket.ui.CommentaryPanel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;

public class CommentaryService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void load(long matchId, CommentaryPanel panel) {

        new Thread(() -> {
            try {
                String json = LiveScoreApi.get(
                        "https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/"
                                + matchId + "/comm"
                );

                JsonNode root = MAPPER.readTree(json);
                JsonNode list = root.path("comwrapper");

                for (JsonNode item : list) {

                    JsonNode c = item.path("commentary");
                    if (c.isMissingNode()) continue;

                    String raw = c.path("commtxt").asText("").trim();
                    if (raw.isEmpty()) continue;

                    String speaker = extractSpeaker(c);
                    String text = clean(raw);

                    if (!speaker.isEmpty()) {
                        text = speaker + " " + text;
                    }


                    int ball = c.path("ballnbr").asInt(0);
                    double overNum = c.path("overnum").asDouble(0);

                    boolean isBall = ball > 0 && overNum > 0;

                    String over = isBall
                            ? String.format("%.1f", overNum)
                            : "";

                    final String overText = over;
                    final String lineText = text;
                    final Color bgColor = UIManager.getColor("Panel.background");

                    SwingUtilities.invokeLater(() ->
                            panel.addLine(overText, lineText, bgColor)
                    );

                }

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        panel.addLine(
                                "",
                                "Failed to load commentary",
                                new Color(255, 230, 230)
                        )
                );
            }
        }).start();
    }

    private static String clean(String s) {
        return s
                .replaceAll("B\\d\\$", "")
                .replaceAll("I\\d\\$", "")
                .replace("\\n", "\n")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String extractSpeaker(JsonNode c) {
        for (JsonNode fmt : c.path("commentaryformats")) {
            if ("bold".equals(fmt.path("type").asText())) {
                for (JsonNode v : fmt.path("value")) {
                    String txt = v.path("value").asText("").trim();

                    // must start with capital letter
                    if (!txt.isEmpty() && Character.isUpperCase(txt.charAt(0))) {
                            return txt;
                        }
                }
            }
        }
        return "";
    }
}
