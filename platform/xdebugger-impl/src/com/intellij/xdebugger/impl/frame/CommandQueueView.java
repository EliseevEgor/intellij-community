// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.xdebugger.impl.frame;

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

  public CommandQueueView() {
    mainPanel = new JPanel(new BorderLayout());
    commandList = new DefaultListModel<>();
    mainPanel.add(new JList<>(commandList));
  }

  public JComponent getPanel() {
    return mainPanel;
  }
}
