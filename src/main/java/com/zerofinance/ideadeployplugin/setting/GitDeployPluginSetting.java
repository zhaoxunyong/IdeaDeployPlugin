package com.zerofinance.ideadeployplugin.setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author wengyongcheng
 * @since 2020/3/17 9:22 上午
 * application 级别的properties Component
 */
public class GitDeployPluginSetting implements Configurable {
    private JTextField textField;
    private JPanel mainPanel;
    private JButton button1;
    private JCheckBox needDebugCheckBox;
    private JCheckBox moreDetailsCheckBox;

    private static final String GIT_HOME_KEY = "gitDeployPluginGitHomeKey";
    private static final String DEBUG_KEY = "gitDeployPluginDebugKey";
    private static final String MORE_DETAILS_KEY = "gitDeployPluginMoreDetailsKey";

    public GitDeployPluginSetting() {
        textField.setText(PropertiesComponent.getInstance().getValue(GIT_HOME_KEY));
        needDebugCheckBox.setSelected(isDebug());
        moreDetailsCheckBox.setSelected(isMoreDetails());
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(false,true,false,false,false,false);
                // 方式二，直接使用FileChooser.chooseFiles
                FileChooser.chooseFiles(chooserDescriptor, null, null, virtualFiles -> {
                    if (CollectionUtils.isNotEmpty(virtualFiles)) {
                        for (VirtualFile file : virtualFiles) {
                            //Messages.showMessageDialog(file.getPath(), file.getName(), Messages.getInformationIcon());
                            textField.setText(file.getPath());
                        }
                    }
                });
            }
        });
        needDebugCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                needDebugCheckBox.setSelected(!isDebug());
            }
        });
        moreDetailsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moreDetailsCheckBox.setSelected(!isMoreDetails());
            }
        });
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GitDeployPlugin";
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !StringUtils.equals(textField.getText(),PropertiesComponent.getInstance().getValue(GIT_HOME_KEY))
                || !StringUtils.equals(String.valueOf(needDebugCheckBox.isSelected()),PropertiesComponent.getInstance().getValue(DEBUG_KEY))
                || !StringUtils.equals(String.valueOf(moreDetailsCheckBox.isSelected()),PropertiesComponent.getInstance().getValue(MORE_DETAILS_KEY));
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent.getInstance().setValue(GIT_HOME_KEY, textField.getText());
        PropertiesComponent.getInstance().setValue(DEBUG_KEY, String.valueOf(needDebugCheckBox.isSelected()));
        PropertiesComponent.getInstance().setValue(MORE_DETAILS_KEY, String.valueOf(moreDetailsCheckBox.isSelected()));
    }

    public static String getGitHome() {
        return PropertiesComponent.getInstance().getValue(GIT_HOME_KEY);
    }

    public static boolean isDebug() {
        if(StringUtils.isBlank(PropertiesComponent.getInstance().getValue(DEBUG_KEY))) {
            return false;
        }
        return Boolean.valueOf(PropertiesComponent.getInstance().getValue(DEBUG_KEY));
    }

    public static boolean isMoreDetails() {
        if(StringUtils.isBlank(PropertiesComponent.getInstance().getValue(MORE_DETAILS_KEY))) {
            return false;
        }
        return Boolean.valueOf(PropertiesComponent.getInstance().getValue(MORE_DETAILS_KEY));
    }
}
