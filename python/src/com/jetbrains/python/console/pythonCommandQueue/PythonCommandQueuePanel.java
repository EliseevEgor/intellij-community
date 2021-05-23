// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.pythonCommandQueue;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.jetbrains.python.console.actions.CommandQueueForPythonConsoleAction;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main panel for PopupWindow (CommandQueue)
 */
public class PythonCommandQueuePanel extends JPanel {
  protected final JPanel myPanel = new JPanel();

  private final List<ConsoleCommunication.ConsoleCodeFragment> myCommands = new ArrayList<>();
  private final Map<ConsoleCommunication.ConsoleCodeFragment, QueueElementPanel> myQueueElementPanelMap = new HashMap<>();

  private ConsoleCommunication communication;

  public PythonCommandQueuePanel() {
    setLayout(new GridLayout());
    setBorder(JBUI.Borders.empty());
    setPreferredSize(new Dimension(300, 100));

    myPanel.setLayout(new VerticalLayout(0));
    myPanel.setBorder(JBUI.Borders.empty(7));

    JBScrollPane scrollPane = new JBScrollPane(myPanel);
    add(scrollPane);

    repaintAll();
  }

  public void addCommand(ConsoleCommunication.ConsoleCodeFragment command) {
    QueueElementPanel elementPanel = new QueueElementPanel(command,
                                                           myCommands.size() == 0 ? AllIcons.Actions.Execute : AllIcons.Actions.Play_forward);
    myCommands.add(command);
    myQueueElementPanelMap.put(command, elementPanel);

    myPanel.add(elementPanel.getQueuePanel());

    if (myCommands.size() == 1) {
      elementPanel.setCancelButtonPainting(false);
    }

    repaintAll();
  }

  public void removeCommand(ConsoleCommunication.ConsoleCodeFragment command) {
    myCommands.remove(command);
    var removedPanel = myQueueElementPanelMap.remove(command);

    myPanel.remove(removedPanel.getQueuePanel());
    if (myCommands.size() > 0) {
      QueueElementPanel elementPanel = myQueueElementPanelMap.get(myCommands.get(0));
      elementPanel.setIcon(AllIcons.Actions.Execute);
      elementPanel.setCancelButtonPainting(false);
    }

    ServiceManager.getService(CommandQueueForPythonConsoleAction.class).removeCommand(communication, command);
    repaintAll();
  }

  public void removeCommand() {
    var removedElem = myCommands.remove(0);
    var removedPanel = myQueueElementPanelMap.remove(removedElem);

    myPanel.remove(removedPanel.getQueuePanel());
    if (myCommands.size() > 0) {
      QueueElementPanel elementPanel = myQueueElementPanelMap.get(myCommands.get(0));
      elementPanel.setIcon(AllIcons.Actions.Execute);
      elementPanel.setCancelButtonPainting(false);
    }
    repaintAll();
  }

  public void removeAllCommands() {
    myCommands.clear();
    myQueueElementPanelMap.clear();
    myPanel.removeAll();

    repaintAll();
  }

  private void repaintAll() {
    revalidate();
    repaint();
  }

  public void setCommunication(ConsoleCommunication communication) {
    this.communication = communication;
  }
}