package com.zerofinance.ideadeployplugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.zerofinance.ideadeployplugin.utils.DeployPluginHandler;
import org.jetbrains.annotations.NotNull;

public class MybatisGenAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
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
            handler.release();

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