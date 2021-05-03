// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.xdebugger.impl.frame;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
/**
 * class that renders a panel for the CommandQueue
 * */
public class CommandQueueView {
  private final JPanel mainPanel;
  private final DefaultListModel<String> commandList;

  public void remove() {
    commandList.remove(0);
  }

  public void add(String command) {
    commandList.addElement(command);
  }

  public void clear(){
    commandList.clear();
  }
  public CommandQueueView() {
    mainPanel = new JPanel(new BorderLayout());
    commandList = new DefaultListModel<>();
    var list = new JBList<>(commandList);
    list.setOpaque(false);
    list.setBorder(JBUI.Borders.empty());
    list.setCellRenderer((jList, value, index, selected, expanded)-> {
      JLabel label = new JLabel();
      if (index == 0) {
        label.setIcon(AllIcons.Actions.Execute);
      }
      else {
        label.setIcon(AllIcons.Actions.Back);
      }
      label.setText(value);
      return label;
    });
    mainPanel.add(new JScrollPane(list));
  }

  public JComponent getPanel() {
    return mainPanel;
  }
}
