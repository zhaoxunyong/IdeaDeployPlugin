package com.zerofinance.ideadeployplugin;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
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
                showMessage("Please pick up a valid module!", "Error", NotificationType.ERROR);
                return;
            }
            String fileName = vFile != null ? vFile.getName() : null;
            System.out.println("fileName--->"+fileName);

            String modulePath = vFile.getPath();
            System.out.println("modulePath--->"+modulePath);
            DeployPluginHandler handler = new DeployPluginHandler(project);
            handler.release(modulePath, "Release");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage(e.getMessage(), "Error", NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        boolean visibility = event.getProject() != null;
        event.getPresentation().setEnabled(visibility);
        event.getPresentation().setVisible(visibility);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void showMessage(String message, String title, NotificationType type) {
        NotificationGroup notificationGroup = new NotificationGroup(title+"Group", NotificationDisplayType.BALLOON, true);
        Notification notification = notificationGroup.createNotification(message, type);
        Notifications.Bus.notify(notification);
    }

    private @Nullable Pair<Content, ShellTerminalWidget> getSuitableProcess(@NotNull Content content) {
        JBTerminalWidget widget = TerminalView.getWidgetByContent(content);
        /*if (!(widget instanceof ShellTerminalWidget)) {
            return null;
        }*/

        ShellTerminalWidget shellTerminalWidget = (ShellTerminalWidget)widget;
        if (!shellTerminalWidget.getTypedShellCommand().isEmpty() || shellTerminalWidget.hasRunningCommands()) {
            return null;
        }

        /*String currentWorkingDirectory = TerminalWorkingDirectoryManager.getWorkingDirectory(shellTerminalWidget, null);
        if (currentWorkingDirectory == null || !currentWorkingDirectory.equals(workingDirectory)) {
            return null;
        }*/

        return new Pair<>(content, shellTerminalWidget);
    }
}
