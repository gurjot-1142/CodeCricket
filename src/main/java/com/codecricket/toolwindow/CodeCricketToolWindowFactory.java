package com.codecricket.toolwindow;

import com.codecricket.ui.LiveMatchesPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class CodeCricketToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(
            @NotNull Project project,
            @NotNull ToolWindow toolWindow) {

        LiveMatchesPanel panel = new LiveMatchesPanel();

        Content content = ContentFactory.getInstance()
                .createContent(panel, "Live Score", false);

        toolWindow.getContentManager().addContent(content);
    }
}
