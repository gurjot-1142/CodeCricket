package com.codecricket.ui;

import com.codecricket.features.LiveMatchesService;
import com.codecricket.model.MatchItem;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LiveMatchesPanel extends JPanel {

    private final DefaultListModel<MatchItem> model = new DefaultListModel<>();
    private final JBList<MatchItem> list = new JBList<>(model);

    public LiveMatchesPanel() {

        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Live Matches");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());

        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(header, BorderLayout.NORTH);

        // ---------- LIST ----------
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new MatchCellRenderer());

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MatchItem selected = list.getSelectedValue();
                if (selected != null) {
                    MatchDetailsPanel.open(null, selected);
                    list.clearSelection(); // 🔥 IMPORTANT
                }
            }
        });

        add(new JBScrollPane(list), BorderLayout.CENTER);

        load();
    }

    private void load() {
        model.clear();
        SwingUtilities.invokeLater(() -> {
            try {
                List<MatchItem> matches = LiveMatchesService.fetchLiveMatches();
                matches.forEach(model::addElement);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load live matches",
                        "CodeCricket",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    // ---------- CELL RENDERER ----------
    private static class MatchCellRenderer extends JPanel
            implements ListCellRenderer<MatchItem> {

        private final JLabel teams = new JLabel();
        private final JLabel scores = new JLabel();
        private final JLabel status = new JLabel();

        MatchCellRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

            teams.setFont(teams.getFont().deriveFont(Font.BOLD, 13f));
            scores.setFont(scores.getFont().deriveFont(12f));
            status.setFont(status.getFont().deriveFont(11f));

            add(teams);
            add(scores);
            add(status);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends MatchItem> list,
                MatchItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            teams.setText(value.getTeam1() + " vs " + value.getTeam2());
            String s1 = value.getTeam1Score();
            String s2 = value.getTeam2Score();

            if (!s1.isEmpty() && !s2.isEmpty()) {
                scores.setText(s1 + "  |  " + s2);
                scores.setVisible(true);
            } else if (!s1.isEmpty()) {
                scores.setText(s1);
                scores.setVisible(true);
            } else {
                scores.setText("");
                scores.setVisible(false);
            }
            status.setText(value.getStatus());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            return this;
        }
    }
}
