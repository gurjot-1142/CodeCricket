package com.codecricket.services;

import com.codecricket.api.LiveScoreApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ScorecardService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JsonNode cached;
    private static long cachedMatchId = -1;

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
        JsonNode scorecard = root(matchId).path("scorecard");
        if (!scorecard.isArray() || idx >= scorecard.size()) {
            return null;
        }
        return scorecard.get(idx);
    }

    public static String matchResult(long matchId) throws Exception {
        return root(matchId).path("status").asText("");
    }

    public static String inningsHeader(long matchId, int idx) throws Exception {
        JsonNode i = innings(matchId, idx);
        if (i == null) return "Innings not started";

        return i.path("batteamname").asText() + " "
                + i.path("score").asInt() + "/"
                + i.path("wickets").asInt()
                + " (" + i.path("overs").asText() + " ov)";
    }

    public static JTable battingTable(long matchId, int idx) throws Exception {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Batter", "Dismissal", "R", "B", "4s", "6s", "SR"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JsonNode inn = innings(matchId, idx);
        if (inn == null) return new JTable(model);

        for (JsonNode b : inn.path("batsman")) {

            boolean yetToBat = b.path("balls").asInt() == 0
                    && b.path("outdec").asText("").isEmpty();

            String name = b.path("name").asText();
            if (b.path("iscaptain").asBoolean()) name += " (c)";
            if (b.path("iskeeper").asBoolean()) name += " (wk)";

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
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JsonNode inn = innings(matchId, idx);
        if (inn == null) return new JTable(model);

        for (JsonNode b : inn.path("bowler")) {
            model.addRow(new Object[]{
                    b.path("name").asText()
                            + (b.path("iscaptain").asBoolean() ? " (c)" : ""),
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

    public static String fallOfWickets(long matchId, int idx) throws Exception {

        JsonNode fowArr = innings(matchId, idx)
                .path("fow")
                .path("fow");

        if (!fowArr.isArray() || fowArr.size() == 0)
            return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fowArr.size(); i++) {
            JsonNode f = fowArr.get(i);

            if (i > 0) sb.append(", ");

            sb.append(f.path("runs").asInt())
                    .append("/")
                    .append(i + 1)
                    .append(" (")
                    .append(f.path("batsmanname").asText())
                    .append(", ")
                    .append(f.path("overnbr").asText())
                    .append(" ov)");
        }

        return sb.toString();
    }

    public static String extras(long matchId, int idx) throws Exception {
        JsonNode i = innings(matchId, idx);
        if (i == null) return "";

        JsonNode e = i.path("extras");
        return "Extras: " + e.path("total").asInt()
                + " (b " + e.path("byes").asInt()
                + ", lb " + e.path("legbyes").asInt()
                + ", w " + e.path("wides").asInt()
                + ", nb " + e.path("noballs").asInt() + ")";
    }

    public static String totalLine(long matchId, int idx) throws Exception {
        JsonNode i = innings(matchId, idx);
        if (i == null) return "";

        return "Total: " + i.path("score").asInt() + "/"
                + i.path("wickets").asInt()
                + " (" + i.path("overs").asText()
                + " ov, RR " + i.path("runrate").asText() + ")";
    }

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
                .replace("run out", "run out")
                .trim();
    }

    public static void refresh() {
        cached = null;
        cachedMatchId = -1;
    }

    public static void clearCache() {
        cached = null;
        cachedMatchId = -1;
    }

    public static String cricbuzzUrl(long matchId) throws Exception {
        return root(matchId)
                .path("appindex")
                .path("weburl")
                .asText("");
    }

    public static int inningsCount(long matchId) throws Exception {
        return root(matchId).path("scorecard").size();
    }

    public static boolean hasScorecard(long matchId) throws Exception {
        JsonNode sc = root(matchId).path("scorecard");
        return sc.isArray() && sc.size() > 0;
    }

    public static boolean isMatchComplete(long matchId) throws Exception {
        return root(matchId).path("ismatchcomplete").asBoolean(false);
    }
}
