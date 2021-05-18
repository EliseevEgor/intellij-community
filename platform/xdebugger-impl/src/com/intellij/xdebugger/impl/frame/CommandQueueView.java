// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.xdebugger.impl.frame;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * class that renders a panel for the CommandQueue
 * */
public class CommandQueueView {
  private final JPanel mainPanel;
  private final JPanel mainList;
  private final DefaultListModel<JPanel> commandList;
  private final ArrayList<JPanel> commands = new ArrayList<>();

  public void remove() {
    commandList.remove(0);

  }

  public void add(String command) {
    JPanel panel = new JPanel();
    panel.add(new JLabel(command));
    JButton button = new JButton("C");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("nj");
      }
    });
    button.setSize(10, 10);
    panel.add(button);
    panel.setBorder(JBUI.Borders.customLine(JBColor.BLUE));
    commandList.addElement(panel);
    commands.add(panel);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainList.add(panel, gbc);

    mainPanel.validate();
    mainPanel.repaint();
    //mainPanel.add(panel);
  }

  public void clear(){
    commandList.clear();
  }
  public CommandQueueView() {
    mainPanel = new JPanel(new BorderLayout());
    mainList = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    mainList.add(new JPanel(), gbc);
    commandList = new DefaultListModel<>();
    var list = new JBList<>(commandList);
    list.setOpaque(false);
    list.setBorder(JBUI.Borders.empty());
    list.setCellRenderer(new ListCellRenderer<>() {
      @Override
      public Component getListCellRendererComponent(JList<? extends JPanel> jList,
                                                    JPanel value,
                                                    int index,
                                                    boolean selected,
                                                    boolean expanded) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        if (index == 0) {
          label.setIcon(AllIcons.Actions.Execute);
        }
        else {
          label.setIcon(AllIcons.Actions.Back);
        }
        for (Component component : value.getComponents()) {
          if (component instanceof JLabel) {
            label.setText(((JLabel)component).getText());
            panel.add(label);
          }
          else {
            panel.add(component);
          }
        }
        return panel;
      }
    });
    mainPanel.add(new JScrollPane(mainList));
  }

  public JComponent getPanel() {
    return mainPanel;
  }
}
