package com.codecricket.ui;

import com.codecricket.features.LiveMatchesService;
import com.codecricket.model.MatchItem;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LiveMatchesPanel extends JPanel {

    public LiveMatchesPanel() {

        setLayout(new BorderLayout());

        DefaultListModel<MatchItem> model = new DefaultListModel<>();
        JBList<MatchItem> list = new JBList<>(model);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MatchItem selected = list.getSelectedValue();
                if (selected != null) {
                    MatchDetailsPanel.open(null, selected);
                }
            }
        });


        add(new JBScrollPane(list), BorderLayout.CENTER);

        // Load data
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
}
