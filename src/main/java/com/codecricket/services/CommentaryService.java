package com.codecricket.services;

import com.codecricket.api.LiveScoreApi;
import com.codecricket.ui.CommentaryPanel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

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

                    String text = resolveText(c);
                    if (text.isEmpty()) continue;

                    String speaker = extractSpeaker(c);

                    if (!speaker.isEmpty() && !text.startsWith(speaker)) {
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

    private static String resolveText(JsonNode c) {

        String text = c.path("commtxt").asText("");

        for (JsonNode fmt : c.path("commentaryformats")) {

            if (!fmt.has("value")) continue;

            for (JsonNode v : fmt.path("value")) {
                String id = v.path("id").asText();
                String val = v.path("value").asText();

                if (!id.isEmpty() && !val.isEmpty()) {
                    text = text.replace(id, val);
                }
            }
        }

        return text
                .replace("\\n", "\n")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String extractSpeaker(JsonNode c) {
        for (JsonNode fmt : c.path("commentaryformats")) {
            if ("bold".equals(fmt.path("type").asText())) {
                for (JsonNode v : fmt.path("value")) {
                    String txt = v.path("value").asText("").trim();

                    if (!txt.isEmpty()
                            && Character.isUpperCase(txt.charAt(0))
                            && txt.contains("|")) {
                        return txt;
                    }
                }
            }
        }
        return "";
    }
}
