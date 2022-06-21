package com.zerofinance.ideadeployplugin;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.zerofinance.ideadeployplugin.utils.DeployPluginHandler;
import com.zerofinance.ideadeployplugin.utils.FileHandlerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

public class MenuAction extends AnAction {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        try {
            Project project = event.getProject();

            VirtualFile vFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            if(vFile == null) {
                // showMessage("Please pick up a valid module!", "Error", NotificationType.ERROR);
                Messages.showErrorDialog("Please pick up a valid module!", "Error");
                return;
            }
            String fileName = vFile != null ? vFile.getName() : null;
            System.out.println("fileName--->"+fileName);

            String modulePath = vFile.getPath();
            System.out.println("modulePath--->"+modulePath);
            DeployPluginHandler handler = new DeployPluginHandler(project, modulePath);
            if(handler.preCheck()) {
                handler.release();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog(e.getMessage(), "Error");
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        boolean visibility = event.getProject() != null;
        event.getPresentation().setEnabled(visibility);
        event.getPresentation().setVisible(visibility);
    }
}
