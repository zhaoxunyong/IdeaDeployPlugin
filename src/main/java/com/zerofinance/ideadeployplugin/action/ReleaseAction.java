package com.zerofinance.ideadeployplugin.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.GotItMessage;
import com.zerofinance.ideadeployplugin.tools.DeployPluginHandler;
import com.zerofinance.ideadeployplugin.tools.MessagesUtils;
import org.jetbrains.annotations.NotNull;

public class ReleaseAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        try {

            VirtualFile vFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            if(vFile == null) {
                // showMessage("Please pick up a valid module!", "Error", NotificationType.ERROR);
                // Messages.showErrorDialog("Please pick up a valid module!", "Error");
                MessagesUtils.showMessage(project, "Please pick up a valid module!", "Error:", NotificationType.ERROR);
                return;
            }
            String modulePath = vFile.getPath();
            DeployPluginHandler handler = new DeployPluginHandler(project, modulePath);
            if(handler.preCheck()) {
                handler.release();
            }

        } catch (Exception e) {
            e.printStackTrace();
//            Messages.showErrorDialog(e.getMessage(), "Error");
            MessagesUtils.showMessage(project, e.getMessage(), "Error:", NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        boolean visibility = event.getProject() != null;
        event.getPresentation().setEnabled(visibility);
        event.getPresentation().setVisible(visibility);
    }
}
