// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.pythonCommandQueue;

import com.intellij.core.CoreBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.InplaceButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel for one command (CommandQueue)
 */
public class QueueElementPanel {
  private final QueueElementButton myCancelButton;
  private JBLabel myIcon;
  private JBLabel myText;
  private JPanel myRootPanel;
  private JPanel myButtonPanel;

  private volatile boolean isCanceled;
  private final ConsoleCommunication.ConsoleCodeFragment myCodeFragment;

  private @NlsContexts.Tooltip final String myCancelTooltipText = CoreBundle.message("button.cancel");

  public QueueElementPanel(ConsoleCommunication.ConsoleCodeFragment codeFragment, Icon icon) {
    myCodeFragment = codeFragment;
    myText.setFont(JBUI.Fonts.label(13));
    myText.setText(codeFragment.getText());

    myCancelButton = createCancelButton();
    myIcon.setIcon(icon);
    myButtonPanel.add(createButtonPanel(myCancelButton.button));

    myRootPanel.setBackground(UIUtil.getListBackground());
    myRootPanel.setFocusable(true);
    myRootPanel.requestFocusInWindow();
    myRootPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        getCommandPanelParent().commandSelected(QueueElementPanel.this);
        myRootPanel.setBackground(UIUtil.getListSelectionBackground(true));
      }
    });
  }

  private static JPanel createButtonPanel(JComponent component) {
    JPanel iconsPanel = new NonOpaquePanel(new GridBagLayout());
    GridBag gb = new GridBag().setDefaultFill(GridBagConstraints.BOTH);
    iconsPanel.add(component, gb.next());
    return iconsPanel;
  }

  @NotNull
  private QueueElementButton createCancelButton() {
    InplaceButton cancelButton = new InplaceButton(
      new IconButton(myCancelTooltipText,
                     AllIcons.Process.Stop,
                     AllIcons.Process.StopHovered),
      __ -> cancelRequest()).setFillBg(false);

    cancelButton.setVisible(true);

    return new QueueElementButton(cancelButton, () -> cancelButton.setPainting(!isCanceled));
  }

  public String getText() {
    return myText.getText();
  }

  private void cancelRequest() {
    isCanceled = true;
    getCommandPanelParent().removeCommand(myCodeFragment);
  }

  private PythonCommandQueuePanel getCommandPanelParent() {
    Container parent = myRootPanel;
    while (!(parent instanceof PythonCommandQueuePanel)) {
      parent = parent.getParent();
    }
    return (PythonCommandQueuePanel)parent;
  }

  public void setIcon(Icon icon) {
    myIcon.setIcon(icon);
  }

  public void setCancelButtonPainting(boolean cancelButtonPainting) {
    myCancelButton.button.setPainting(cancelButtonPainting);
  }

  @NotNull
  public JComponent getQueuePanel() {
    return myRootPanel;
  }

  static class QueueElementButton {
    @NotNull final InplaceButton button;
    @NotNull final Runnable updateAction;

    QueueElementButton(@NotNull InplaceButton button, @NotNull Runnable updateAction) {
      this.button = button;
      this.updateAction = updateAction;
    }
  }
}
