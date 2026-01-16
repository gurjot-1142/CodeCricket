package com.codecricket.ui;

import com.codecricket.model.MatchItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import java.awt.Dimension;

public class MatchDetailsPanel extends DialogWrapper {

    private final MatchItem match;

    public static void open(Project project, MatchItem match) {
        new MatchDetailsPanel(project, match).show();
    }

    private MatchDetailsPanel(Project project, MatchItem match) {
        super(project);
        this.match = match;

        setTitle(match.toString());
        setResizable(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Match Info", new MatchInfoPanel(match.getMatchId()));
        tabs.addTab("Commentary", new CommentaryPanel(match.getMatchId()));
        tabs.addTab("Scorecard", new ScorecardPanel(match.getMatchId()));

        tabs.setSelectedIndex(2);

        tabs.setPreferredSize(new Dimension(900, 650));
        return tabs;
    }

}
