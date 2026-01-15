package com.codecricket.ui;

import com.codecricket.features.CommentaryService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;

public class CommentaryPanel extends JPanel {

    private final long matchId;
    private final JPanel content = new JPanel();
    private final JBScrollPane scroll;

    public CommentaryPanel(long matchId) {
        this.matchId = matchId;

        setLayout(new BorderLayout());

        add(createToolbar(), BorderLayout.NORTH);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIManager.getColor("Panel.background"));

        scroll = new JBScrollPane(content);
        scroll.setBorder(null);

        add(scroll, BorderLayout.CENTER);

        reload();
    }

    private JComponent createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new AnAction("Refresh", "Refresh commentary", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                reload();
            }
        });

        return ActionManager.getInstance()
                .createActionToolbar("CodeCricketComm", group, true)
                .getComponent();
    }

    public void reload() {
        content.removeAll();
        content.revalidate();
        content.repaint();

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
        area.setBorder(null);

        row.add(area, BorderLayout.CENTER);

        content.add(row);
        content.add(Box.createVerticalStrut(2));

        revalidate();

        SwingUtilities.invokeLater(() ->
                scroll.getVerticalScrollBar().setValue(0)
        );
    }
}
