package com.codecricket.features;

import com.codecricket.api.LiveScoreApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ScorecardService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode cachedScorecard;
    private static long cachedMatchId = -1;

    // ---------- CORE JSON ----------
    private static JsonNode scorecard(long matchId) throws Exception {

        if (cachedScorecard == null || cachedMatchId != matchId) {
            String json = LiveScoreApi.get(
                    "https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/"
                            + matchId + "/scard"
            );
            cachedScorecard = mapper.readTree(json);
            cachedMatchId = matchId;
        }
        return cachedScorecard;
    }

    private static JsonNode innings(long matchId, int idx) throws Exception {
        return scorecard(matchId)
                .path("scorecard")
                .get(idx);
    }

    // ---------- RESULT ----------
    public static String matchResult(long matchId) throws Exception {
        return scorecard(matchId)
                .path("matchHeader")
                .path("status")
                .asText("");
    }

    // ---------- INNINGS HEADER ----------
    public static String inningsHeader(long matchId, int idx) throws Exception {

        JsonNode inn = innings(matchId, idx);

        String team = inn.path("batteamname").asText();
        int runs = inn.path("score").asInt();
        int wkts = inn.path("wickets").asInt();
        double overs = inn.path("overs").asDouble();

        return team + "  " + runs + "/" + wkts + " (" + overs + " overs)";
    }

    // ---------- EXTRAS ----------
    public static String extras(long matchId, int idx) throws Exception {

        JsonNode e = innings(matchId, idx).path("extras");

        int total = e.path("total").asInt();
        int b = e.path("byes").asInt();
        int lb = e.path("legbyes").asInt();
        int w = e.path("wides").asInt();
        int nb = e.path("noballs").asInt();

        return "Extras: " + total +
                " (b " + b +
                ", lb " + lb +
                ", w " + w +
                ", nb " + nb + ")";
    }

    // ---------- TOTAL ----------
    public static String totalLine(long matchId, int idx) throws Exception {

        JsonNode inn = innings(matchId, idx);

        int runs = inn.path("score").asInt();
        int wkts = inn.path("wickets").asInt();
        double overs = inn.path("overs").asDouble();
        double rr = inn.path("runrate").asDouble();

        return "Total: " + runs + "/" + wkts +
                " (" + overs + " overs, RR " +
                String.format("%.2f", rr) + ")";
    }

    // ---------- BATTING TABLE ----------
    public static JTable battingTable(long matchId, int idx) throws Exception {

        JsonNode inn = innings(matchId, idx);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Name", "R", "B", "4s", "6s", "SR"}, 0
        );

        for (JsonNode b : inn.path("batsman")) {
            model.addRow(new Object[]{
                    b.path("name").asText(),
                    b.path("runs").asInt(),
                    b.path("balls").asInt(),
                    b.path("fours").asInt(),
                    b.path("sixes").asInt(),
                    b.path("strkrate").asText()
            });
        }

        JTable table = new JTable(model);
        style(table);
        return table;
    }

    // ---------- BOWLING TABLE ----------
    public static JTable bowlingTable(long matchId, int idx) throws Exception {

        JsonNode inn = innings(matchId, idx);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Name", "O", "R", "W", "Econ"}, 0
        );

        for (JsonNode b : inn.path("bowler")) {
            model.addRow(new Object[]{
                    b.path("name").asText(),
                    b.path("overs").asText(),
                    b.path("runs").asInt(),
                    b.path("wickets").asInt(),
                    b.path("economy").asText()
            });
        }

        JTable table = new JTable(model);
        style(table);
        return table;
    }

    // ---------- TABLE STYLE ----------
    private static void style(JTable table) {

        table.setRowHeight(26);

        // Let table auto-fit to container width
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // Name column — balanced width
        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(180);

        // Center numeric columns
        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(60);
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center);
        }

        table.setFillsViewportHeight(true);
    }
}
