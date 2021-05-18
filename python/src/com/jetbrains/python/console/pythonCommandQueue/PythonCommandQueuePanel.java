// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.pythonCommandQueue;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonCommandQueuePanel extends JPanel {
  private final JPanel myPanel = new JPanel();
  private final JBScrollPane listScroller = new JBScrollPane(myPanel);

  private final List<ConsoleCommunication.ConsoleCodeFragment> myCommands = new ArrayList<>();
  private final Map<ConsoleCommunication.ConsoleCodeFragment, QueueElementPanel> myQueueElementPanelMap = new HashMap<>();

  public PythonCommandQueuePanel() {
    myPanel.setPreferredSize(new Dimension(250, 1000));
    listScroller.setPreferredSize(new Dimension(250, 100));
    listScroller.setAlignmentY(TOP_ALIGNMENT);

    setOpaque(false);
    setBorder(JBUI.Borders.empty());

    //myPanel.add(listScroller);
    myPanel.setLayout(new BorderLayout());
    myPanel.setOpaque(false);
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(listScroller);

    revalidate();
    repaint();
  }

  public void addCommand(ConsoleCommunication.ConsoleCodeFragment command) {
    myCommands.add(command);
    QueueElementPanel elementPanel = new QueueElementPanel(command.getText());
    myQueueElementPanelMap.put(command, elementPanel);

    myPanel.add(elementPanel.getQueuePanel());
    //myPanel.add(Box.createRigidArea(new Dimension(0,5)));
    //myPanel.revalidate();
    //myPanel.repaint();
    revalidate();
    repaint();
  }
  public void removeCommand(JPanel panel){

  }
  public void removeCommand() {
    var removedElem = myCommands.remove(0);
    var removedPanel = myQueueElementPanelMap.remove(removedElem);

    myPanel.remove(removedPanel.getQueuePanel());
    //myPanel.revalidate();
    //myPanel.repaint();
    //listScroller.revalidate();
    //listScroller.repaint();
    revalidate();
    repaint();

  }
}