package com.codecricket.features;

import com.codecricket.api.LiveScoreApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ScorecardService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode cached;
    private static long cachedMatchId = -1;

    // ---------- CORE ----------
    private static JsonNode root(long matchId) throws Exception {
        if (cached == null || cachedMatchId != matchId) {
            cached = mapper.readTree(
                    LiveScoreApi.get(
                            "https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/"
                                    + matchId + "/scard"
                    )
            );
            cachedMatchId = matchId;
        }
        return cached;
    }

    private static JsonNode innings(long matchId, int idx) throws Exception {
        return root(matchId).path("scorecard").get(idx);
    }

    // ---------- MATCH ----------
    public static String matchResult(long matchId) throws Exception {
        return root(matchId).path("status").asText("");
    }

    public static boolean isInningsLive(long matchId, int idx) throws Exception {
        JsonNode inn = innings(matchId, idx);
        return !inn.path("isdeclared").asBoolean(false)
                && inn.path("wickets").asInt() < 10;
    }

    // ---------- HEADER ----------
    public static String inningsHeader(long matchId, int idx) throws Exception {
        JsonNode i = innings(matchId, idx);
        return i.path("batteamname").asText() + " "
                + i.path("score").asInt() + "/"
                + i.path("wickets").asInt()
                + " (" + i.path("overs").asText() + " ov)";
    }

    // ---------- TABLES ----------
    public static JTable battingTable(long matchId, int idx) throws Exception {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Batter", "Dismissal", "R", "B", "4s", "6s", "SR"}, 0
        );

        JsonNode bats = innings(matchId, idx).path("batsman");

        for (JsonNode b : bats) {

            boolean yetToBat = b.path("balls").asInt() == 0
                    && b.path("outdec").asText("").isEmpty();

            String name = b.path("name").asText();
            if (b.path("iscaptain").asBoolean()) name += " ©";

            model.addRow(new Object[]{
                    name,
                    yetToBat ? "Yet to bat" : compactOut(b.path("outdec").asText()),
                    b.path("runs").asInt(),
                    b.path("balls").asInt(),
                    b.path("fours").asInt(),
                    b.path("sixes").asInt(),
                    b.path("strkrate").asText()
            });
        }

        JTable t = new JTable(model);
        styleBatting(t);
        return t;
    }

    public static JTable bowlingTable(long matchId, int idx) throws Exception {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Bowler", "O", "R", "W", "Econ"}, 0
        );

        for (JsonNode b : innings(matchId, idx).path("bowler")) {
            model.addRow(new Object[]{
                    b.path("name").asText()
                            + (b.path("iscaptain").asBoolean() ? " ©" : ""),
                    b.path("overs").asText(),
                    b.path("runs").asInt(),
                    b.path("wickets").asInt(),
                    b.path("economy").asText()
            });
        }

        JTable t = new JTable(model);
        styleBowling(t);
        return t;
    }

    // ---------- FOOTERS ----------
    public static String extras(long matchId, int idx) throws Exception {
        JsonNode e = innings(matchId, idx).path("extras");
        return "Extras: " + e.path("total").asInt()
                + " (b " + e.path("byes").asInt()
                + ", lb " + e.path("legbyes").asInt()
                + ", w " + e.path("wides").asInt()
                + ", nb " + e.path("noballs").asInt() + ")";
    }

    public static String total(long matchId, int idx) throws Exception {
        JsonNode i = innings(matchId, idx);
        return "Total: " + i.path("score").asInt() + "/"
                + i.path("wickets").asInt()
                + " (" + i.path("overs").asText()
                + " ov, RR " + i.path("runrate").asText() + ")";
    }

    // ---------- STYLE ----------
    private static void styleBatting(JTable t) {
        t.setRowHeight(26);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        t.getColumnModel().getColumn(0).setPreferredWidth(180);
        t.getColumnModel().getColumn(1).setPreferredWidth(200);

        centerCols(t, 2);
    }

    private static void styleBowling(JTable t) {
        t.setRowHeight(26);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.getColumnModel().getColumn(0).setPreferredWidth(180);
        centerCols(t, 1);
    }

    private static void centerCols(JTable t, int start) {
        DefaultTableCellRenderer c = new DefaultTableCellRenderer();
        c.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = start; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(c);
            t.getColumnModel().getColumn(i).setPreferredWidth(60);
        }
    }

    private static String compactOut(String s) {
        return s.replace("caught", "c")
                .replace("bowled", "b")
                .replace("lbw", "lbw")
                .replace("run out", "run out")
                .trim();
    }
}
