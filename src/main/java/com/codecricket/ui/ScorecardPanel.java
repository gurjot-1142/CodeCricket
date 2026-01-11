package com.codecricket.ui;

import com.codecricket.features.ScorecardService;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

public class ScorecardPanel extends JPanel {

    public ScorecardPanel(long matchId) {

        setLayout(new BorderLayout());

        try {
            JLabel result = new JLabel(
                    ScorecardService.matchResult(matchId),
                    SwingConstants.CENTER
            );
            result.setFont(result.getFont().deriveFont(Font.BOLD, 14f));
            result.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            add(result, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("1st Innings", innings(matchId, 0));
            tabs.addTab("2nd Innings", innings(matchId, 1));

            add(tabs, BorderLayout.CENTER);

        } catch (Exception e) {
            add(new JLabel("Scorecard unavailable", SwingConstants.CENTER));
        }
    }

    private JComponent innings(long matchId, int idx) throws Exception {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---------- HEADER ----------
        JLabel header = new JLabel(ScorecardService.inningsHeader(matchId, idx));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        content.add(header);

        // ---------- BATTING ----------
        content.add(sectionLabel("Batting"));

        JTable bat = ScorecardService.battingTable(matchId, idx);
        content.add(wrapTable(bat));

        content.add(Box.createVerticalStrut(6));

        JLabel extras = new JLabel(ScorecardService.extras(matchId, idx));
        extras.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(extras);

        JLabel total = new JLabel(ScorecardService.totalLine(matchId, idx));
        total.setFont(total.getFont().deriveFont(Font.BOLD));
        total.setAlignmentX(Component.LEFT_ALIGNMENT);
        total.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        content.add(total);

        // ---------- BOWLING ----------
        content.add(sectionLabel("Bowling"));

        JTable bowl = ScorecardService.bowlingTable(matchId, idx);
        content.add(wrapTable(bowl));

        // ---------- OUTER SCROLL ----------
        JBScrollPane scroll = new JBScrollPane(
                content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);

        return scroll;
    }

    /**
     * FINAL FIX:
     * Lock preferred, max, AND min size
     * so BoxLayout CANNOT stretch vertically.
     */
    private JComponent wrapTable(JTable table) {

        JBScrollPane tableScroll = new JBScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        tableScroll.setBorder(null);

        int height =
                table.getRowHeight() * table.getRowCount() +
                        table.getTableHeader().getPreferredSize().height;

        Dimension size = new Dimension(Integer.MAX_VALUE, height);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setPreferredSize(size);
        wrapper.setMaximumSize(size);
        wrapper.setMinimumSize(size);
        wrapper.add(tableScroll, BorderLayout.CENTER);

        return wrapper;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}