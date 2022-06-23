package com.zerofinance.ideadeployplugin.action;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.zerofinance.ideadeployplugin.tools.CommandUtils;
import com.zerofinance.ideadeployplugin.tools.DeployPluginHandler;
import com.zerofinance.ideadeployplugin.tools.MessagesUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ChangeVersionAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            Project project = event.getProject();

            VirtualFile vFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            if(vFile == null) {
                // showMessage("Please pick up a valid module!", "Error", NotificationType.ERROR);
                // Messages.showErrorDialog("Please pick up a valid module!", "Error");
                MessagesUtils.showMessage(project, "Please pick up a valid module!", "Error:", NotificationType.ERROR);
                return;
            }
            String modulePath = vFile.getPath();
            String rootProjectPath = CommandUtils.getRootProjectPath(modulePath);
            String moduleName = new File(rootProjectPath).getName();
            MessagesUtils.showMessage(project, "\""+moduleName+"\" was selected!", "Information:", NotificationType.INFORMATION);

            DeployPluginHandler handler = new DeployPluginHandler(project, modulePath);
            if(handler.preCheck()) {
                handler.changeVersion();
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
