package com.codecricket.ui;

import com.codecricket.features.MatchInfoService;
import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MatchInfoPanel extends JPanel {

    private final long matchId;

    public MatchInfoPanel(long matchId) {
        this.matchId = matchId;
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        load(content);
    }

    private void load(JPanel content) {
        try {
            JsonNode root = MatchInfoService.load(matchId);

            // ---------- HEADER ----------
            String title =
                    root.path("team1").path("teamname").asText()
                            + " vs "
                            + root.path("team2").path("teamname").asText()
                            + " · "
                            + root.path("matchdesc").asText();

            JLabel header = new JLabel(title);
            header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
            content.add(header);

            content.add(Box.createVerticalStrut(6));
            content.add(label(root.path("seriesname").asText(), true));
            content.add(Box.createVerticalStrut(12));

            // ---------- DETAILS ----------
            addRow(content, "Date",
                    formatDate(root.path("startdate").asLong()));

            addRow(content, "Venue",
                    root.path("venueinfo").path("ground").asText()
                            + ", "
                            + root.path("venueinfo").path("city").asText());

            addRow(content, "Toss",
                    root.path("tossstatus").asText());

            addRow(content, "Match Type",
                    root.path("matchformat").asText());

            content.add(Box.createVerticalStrut(12));
            content.add(new JSeparator());
            content.add(Box.createVerticalStrut(10));

            // ---------- OFFICIALS ----------
            content.add(label("Match Officials", true));
            content.add(Box.createVerticalStrut(6));

            addRow(content, "Umpire 1", official(root, "umpire1"));
            addRow(content, "Umpire 2", official(root, "umpire2"));

            if (hasOfficial(root, "umpire3")) {
                addRow(content, "Third Umpire", official(root, "umpire3"));
            }

            addRow(content, "Referee", official(root, "referee"));


        } catch (Exception e) {
            content.add(new JLabel("Match information unavailable"));
        }
    }

    private void addRow(JPanel panel, String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel k = new JLabel(key);
        k.setPreferredSize(new Dimension(120, 20));
        k.setForeground(new Color(120, 120, 120));

        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(13f));

        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    private JLabel label(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 13f));
        return l;
    }

    private String official(JsonNode root, String key) {
        JsonNode o = root.path(key);
        if (o.isMissingNode()) return "-";
        return o.path("name").asText() + " (" + o.path("country").asText() + ")";
    }

    private String formatDate(long ts) {
        return new SimpleDateFormat("dd MMM yyyy")
                .format(new Date(ts));
    }

    private boolean hasOfficial(JsonNode root, String key) {
        JsonNode o = root.path(key);
        return o.path("id").asLong(0) != 0
                && !o.path("name").asText("").isBlank();
    }

}
