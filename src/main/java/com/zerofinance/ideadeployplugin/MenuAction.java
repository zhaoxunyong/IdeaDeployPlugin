package com.zerofinance.ideadeployplugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.zerofinance.ideadeployplugin.utils.FileHandlerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;
import org.jetbrains.plugins.terminal.arrangement.TerminalWorkingDirectoryManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class MenuAction extends AnAction {
    //private static ConsoleView view = null;
    //private static ToolWindow window = null;
    private final static String GITCHECK_BAT = "./gitCheck.sh";

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
            String command = FileHandlerUtils.processScript(modulePath, GITCHECK_BAT);
            System.out.println("command--->"+command);

            // https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window
            // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206756385-How-to-make-a-simple-console-output
            String title = new File(FileHandlerUtils.getRootProjectPath(modulePath)).getName();
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GitDeployPlugin");
            toolWindow.show();
            toolWindow.activate(null);

            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Content content = toolWindow.getContentManager().findContent(title);
            if(content == null) {
                content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), title, false);
                toolWindow.getContentManager().addContent(content);
            }
            //consoleView.clear();
            consoleView.print("Hello from MyPlugin!"+new Date().toString(), ConsoleViewContentType.NORMAL_OUTPUT);

            /*TerminalView terminalView = TerminalView.getInstance(project);
            ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
            if (window == null) {
                return;
            }
            window.activate(null);
            ContentManager contentManager = window.getContentManager();
            Content content = contentManager.findContent(title);

            if(content == null) {
                ShellTerminalWidget terminal = terminalView.createLocalShellWidget(project.getBasePath(), title);
                terminal.getTerminalTextBuffer().addModelListener(() -> {
                    String text = terminal.getTerminalTextBuffer().getLine(0).getText();
                    System.out.println("text------>"+text);
                });
                terminal.executeCommand(command);
            } else {
                Pair<Content, ShellTerminalWidget> pair = getSuitableProcess(content);
                if(pair == null) {
                    //Messages.showInfoMessage("A terminal has been running", "Warnning");
                    showMessage("A terminal has been running", title, NotificationType.ERROR);
                } else {
                    pair.first.setDisplayName(title);
                    contentManager.setSelectedContent(pair.first);
                    ShellTerminalWidget terminal = pair.second;
                    terminal.executeCommand(command);
                }

            }*/
        } catch (Exception e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
            Messages.showWarningDialog(e.getMessage(), "Error");
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
