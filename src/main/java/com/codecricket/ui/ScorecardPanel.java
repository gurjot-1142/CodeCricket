package com.codecricket.ui;

import com.codecricket.services.ScorecardService;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

public class ScorecardPanel extends JPanel {

    private final long matchId;

    private final JTabbedPane tabs = new JTabbedPane();
    private final JPanel spinnerGlass = new JPanel(new GridBagLayout());

    public ScorecardPanel(long matchId) {
        this.matchId = matchId;
        setLayout(new BorderLayout());

        JLabel result = new JLabel("", SwingConstants.CENTER);
        result.setFont(result.getFont().deriveFont(Font.BOLD, 14f));

        JButton refresh = new JButton("↻ Refresh");
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.setFocusable(false);
        refresh.setBorderPainted(false);
        refresh.setContentAreaFilled(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        header.add(result, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton cricbuzz = new JButton("Go to Cricbuzz");
        cricbuzz.setFocusable(false);
        cricbuzz.setBorderPainted(false);
        cricbuzz.setContentAreaFilled(false);
        cricbuzz.setForeground(new Color(0, 102, 204));
        cricbuzz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cricbuzz.addActionListener(e -> {
            try {
                String url = ScorecardService.cricbuzzUrl(matchId);
                if (!url.isEmpty()) {
                    Desktop.getDesktop().browse(new java.net.URI(url));
                }
            } catch (Exception ignored) {}
        });

        actions.add(cricbuzz);
        actions.add(refresh);

        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.add(tabs, BorderLayout.CENTER);

        spinnerGlass.setOpaque(true);
        spinnerGlass.setBackground(new Color(0, 0, 0, 90));
        spinnerGlass.setVisible(false);

        JProgressBar loader = new JProgressBar();
        loader.setIndeterminate(true);
        loader.setPreferredSize(new Dimension(140, 12));
        spinnerGlass.add(loader);

        JLayeredPane layered = new JLayeredPane();
        layered.setLayout(new OverlayLayout(layered));
        layered.add(content);
        layered.add(spinnerGlass);

        add(layered, BorderLayout.CENTER);

        load(result);

        refresh.addActionListener(e -> refresh(result));
    }

    private void refresh(JLabel result) {
        int selected = tabs.getSelectedIndex();
        spinnerGlass.setVisible(true);

        new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ScorecardService.refresh();
                return null;
            }

            @Override
            protected void done() {
                try {
                    load(result);
                    if (selected >= 0 && selected < tabs.getTabCount()) {
                        tabs.setSelectedIndex(selected);
                    }
                } finally {
                    spinnerGlass.setVisible(false);
                }
            }
        }.execute();
    }

    private void load(JLabel result) {
        tabs.removeAll();

        try {
            result.setText(ScorecardService.matchResult(matchId));

            int inningsCount = Math.min(
                    4,
                    ScorecardService.inningsCount(matchId)
            );

            for (int i = 0; i < inningsCount; i++) {
                tabs.addTab(
                        (i + 1) + getSuffix(i + 1) + " Innings",
                        innings(i)
                );
            }

        } catch (Exception e) {
            tabs.addTab("Unavailable",
                    new JLabel("Scorecard unavailable", SwingConstants.CENTER));
        }
    }

    private JComponent innings(int idx) throws Exception {

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(leftLabel(ScorecardService.inningsHeader(matchId, idx), true));
        root.add(Box.createVerticalStrut(8));

        JTable bat = ScorecardService.battingTable(matchId, idx);
        lockTable(bat);
        root.add(wrap(bat));

        root.add(Box.createVerticalStrut(6));
        root.add(leftFooter(ScorecardService.extras(matchId, idx), false));
        root.add(leftFooter(ScorecardService.totalLine(matchId, idx), true));

        root.add(Box.createVerticalStrut(12));

        JTable bowl = ScorecardService.bowlingTable(matchId, idx);
        lockTable(bowl);
        root.add(wrap(bowl));

        String fow = ScorecardService.fallOfWickets(matchId, idx);
        if (!fow.isEmpty()) {
            root.add(Box.createVerticalStrut(10));
            root.add(leftLabel("Fall of Wickets", true));
            root.add(Box.createVerticalStrut(4));
            root.add(leftText(fow));
        }

        JBScrollPane scroll = new JBScrollPane(
                root,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setBlockIncrement(80);

        SwingUtilities.invokeLater(() ->
                scroll.getVerticalScrollBar().setValue(0)
        );

        return scroll;
    }

    private void lockTable(JTable t) {
        t.setFocusable(false);
        t.setRowSelectionAllowed(false);
        t.setCellSelectionEnabled(false);
        t.setColumnSelectionAllowed(false);

        t.addMouseWheelListener(e ->
                t.getParent().dispatchEvent(e)
        );
    }

    private JComponent wrap(JTable t) {
        int height =
                t.getTableHeader().getPreferredSize().height +
                        (t.getRowHeight() * t.getRowCount());

        t.setPreferredScrollableViewportSize(
                new Dimension(Integer.MAX_VALUE, height)
        );

        JPanel p = new JPanel(new BorderLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t.getTableHeader(), BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        return p;
    }

    private JLabel leftLabel(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 12f));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel leftFooter(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextArea leftText(String text) {
        JTextArea a = new JTextArea(text);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setOpaque(false);
        a.setBorder(null);
        a.setFont(UIManager.getFont("Label.font"));
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        return a;
    }

    private String getSuffix(int n) {
        if (n == 1) return "st";
        if (n == 2) return "nd";
        if (n == 3) return "rd";
        return "th";
    }
}