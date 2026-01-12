package com.codecricket.ui;

import com.codecricket.features.ScorecardService;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

public class ScorecardPanel extends JPanel {

    private final long matchId;

    public ScorecardPanel(long matchId) {
        this.matchId = matchId;
        setLayout(new BorderLayout());

        try {
            JLabel status = new JLabel(
                    ScorecardService.matchResult(matchId),
                    SwingConstants.CENTER
            );
            status.setFont(status.getFont().deriveFont(Font.BOLD, 14f));
            status.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            add(status, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("1st Innings", innings(0));
            tabs.addTab("2nd Innings", innings(1));

            add(tabs, BorderLayout.CENTER);

        } catch (Exception e) {
            add(new JLabel("Scorecard unavailable", SwingConstants.CENTER));
        }
    }

    private JComponent innings(int idx) throws Exception {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(
                ScorecardService.inningsHeader(matchId, idx)
        );
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        header.add(title, BorderLayout.WEST);

        if (ScorecardService.isInningsLive(matchId, idx)) {
            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> reload(idx));
            header.add(refresh, BorderLayout.EAST);
        }

        content.add(header);
        content.add(Box.createVerticalStrut(8));

        content.add(section("Batting"));
        content.add(tableWrap(
                ScorecardService.battingTable(matchId, idx)
        ));

        content.add(Box.createVerticalStrut(6));
        content.add(new JLabel(ScorecardService.extras(matchId, idx)));

        JLabel total = new JLabel(ScorecardService.total(matchId, idx));
        total.setFont(total.getFont().deriveFont(Font.BOLD));
        content.add(total);

        content.add(Box.createVerticalStrut(12));
        content.add(section("Bowling"));
        content.add(tableWrap(
                ScorecardService.bowlingTable(matchId, idx)
        ));

        JBScrollPane scroll = new JBScrollPane(
                content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);

        return scroll;
    }

    private void reload(int idx) {
        removeAll();
        add(new ScorecardPanel(matchId), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JComponent tableWrap(JTable table) {

        JBScrollPane sp = new JBScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        sp.setBorder(null);

        int h = table.getRowHeight() * table.getRowCount()
                + table.getTableHeader().getPreferredSize().height;

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        wrap.add(sp);

        return wrap;
    }

    private JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
