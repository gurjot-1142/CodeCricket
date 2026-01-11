package com.codecricket.ui;

import com.codecricket.features.CommentaryService;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;

public class CommentaryPanel extends JPanel {

    private final JPanel content;
    private final JScrollPane scroll;

    public CommentaryPanel(long matchId) {
        setLayout(new BorderLayout());

        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIManager.getColor("Panel.background"));

        scroll = new JBScrollPane(content);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);

        CommentaryService.load(matchId, this);
    }

    public void addLine(String over, String text, Color bg) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(bg);
        row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        if (!over.isEmpty()) {
            JLabel overLabel = new JLabel(over);
            overLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            overLabel.setForeground(new Color(130, 130, 130));
            row.add(overLabel, BorderLayout.WEST);
        }

        JBTextArea area = new JBTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        area.setBorder(null);

        row.add(area, BorderLayout.CENTER);

        content.add(row);
        content.add(Box.createVerticalStrut(2));

        revalidate();

        SwingUtilities.invokeLater(() ->
                scroll.getVerticalScrollBar().setValue(
                        scroll.getVerticalScrollBar().getMaximum()
                )
        );
    }
}
