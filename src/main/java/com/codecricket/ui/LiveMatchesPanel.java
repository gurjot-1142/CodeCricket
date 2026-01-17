package com.codecricket.ui;

import com.codecricket.services.LiveMatchesService;
import com.codecricket.model.MatchItem;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public class LiveMatchesPanel extends JPanel {

    private final DefaultListModel<MatchItem> model = new DefaultListModel<>();
    private final JBList<MatchItem> list = new JBList<>(model);
    private final JBTextField searchField = new JBTextField();
    private List<MatchItem> allMatches = List.of();

    public LiveMatchesPanel() {

        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Live Matches");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);

        searchField.setMaximumSize(new Dimension(220, 28));
        searchField.setPreferredSize(new Dimension(220, 28));
        searchField.setMinimumSize(new Dimension(220, 28));
        searchField.getEmptyText().setText("Search matches");

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());

        actions.add(searchField);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(refresh);

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new MatchCellRenderer());
        list.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MatchItem selected = list.getSelectedValue();
                if (selected != null) {
                    MatchDetailsPanel.open(null, selected);
                    list.clearSelection();
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
                allMatches = LiveMatchesService.fetchLiveMatches();
                filter();
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

    private void filter() {
        String q = searchField.getText().trim().toLowerCase();
        model.clear();

        for (MatchItem m : allMatches) {
            String text = (m.getTeam1() + " vs " + m.getTeam2()).toLowerCase();
            if (q.isEmpty() || text.contains(q)) {
                model.addElement(m);
            }
        }
    }
}
