package com.zerofinance.ideadeployplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Tool windows control.控制文本显示 2017/3/18 19:58
 */
public class ToolFactoryCompute implements ToolWindowFactory {

    private ToolWindow myToolWindow;
    private JPanel mPanel;
    private JTextArea txtContent;
    private JScrollPane mScrollPane;
    private JTextArea textArea1;

    /**
     * 创建控件内容 2017/3/24 09:02
     * @param project 项目
     * @param toolWindow 窗口
     */
    @Override
    public void createToolWindowContent(@NotNull Project project,
                                        @NotNull ToolWindow toolWindow) {
        myToolWindow = toolWindow;

        //ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        //Content content = contentFactory.createContent(mPanel, "GitDeployPlugin", false);
        //toolWindow.getContentManager().addContent(content);

        txtContent.setEditable(false);

        txtContent.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
        mScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));
        mPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));

        mPanel.setOpaque(false);
        mScrollPane.setOpaque(false);
        mScrollPane.getViewport().setOpaque(false);
        txtContent.setOpaque(false);
    }

    @Override
    public void init(ToolWindow window) {

    }

}