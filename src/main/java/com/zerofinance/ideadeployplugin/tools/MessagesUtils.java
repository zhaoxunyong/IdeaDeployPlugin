package com.zerofinance.ideadeployplugin.tools;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public final class MessagesUtils {

    private MessagesUtils(){}

//    https://plugins.jetbrains.com/docs/intellij/notifications.html
    @SuppressWarnings("UnstableApiUsage")
    public static void showMessage(Project project, String message, NotificationType type) {
/*        NotificationGroup notificationGroup = new NotificationGroup(title+"Group", NotificationDisplayType.BALLOON, true);
        Notification notification = notificationGroup.createNotification(message, type);
        Notifications.Bus.notify(notification);*/
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Custom Notification Group")
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
    }
}
