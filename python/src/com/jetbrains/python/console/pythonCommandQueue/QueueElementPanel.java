// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.pythonCommandQueue;

import com.intellij.core.CoreBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.InplaceButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class QueueElementPanel {
  private final QueueElementButton myCancelButton;
  private JBLabel myText;
  private JPanel myRootPanel;
  private JPanel myButtonPanel;

  private volatile boolean isCanceled;

  private @NlsContexts.Button final String myCancelText = CoreBundle.message("button.cancel");
  private @NlsContexts.Tooltip final String myCancelTooltipText = CoreBundle.message("button.cancel");

  public QueueElementPanel(String commandText) {
    Font font = JBUI.Fonts.label(11);
    myText.setFont(font);
    myText.setText(commandText);
    myCancelButton = createCancelButton();
    //GridBagConstraints gbc = new GridConstraints();
    //gbc.gridwidth = GridBagConstraints.REMAINDER;
    //gbc.weightx = 1;
    //gbc.fill = GridBagConstraints.HORIZONTAL;
    myButtonPanel.add(createButtonPanel(myCancelButton.button), new  GridConstraints());
    myRootPanel.setPreferredSize(new JBDimension(250, 10));
  }

  static JPanel createButtonPanel(JComponent component) {
    JPanel iconsPanel = new NonOpaquePanel(new GridBagLayout());
    GridBag gb = new GridBag().setDefaultFill(GridBagConstraints.BOTH);
    iconsPanel.add(component, gb.next());
    return iconsPanel;
  }

  @NotNull
  protected final QueueElementButton createCancelButton() {
    InplaceButton cancelButton = new InplaceButton(
      new IconButton(myCancelTooltipText,
                     AllIcons.Process.Stop,
                     AllIcons.Process.StopHovered),
      __ -> cancelRequest()).setFillBg(false);

    cancelButton.setVisible(true);

    return new QueueElementButton(cancelButton, () -> cancelButton.setPainting(!isCanceled));
  }

  public final void cancelRequest() {
    myRootPanel.getParent().remove(myRootPanel);

    isCanceled = true;
  }

  public ConsoleCommunication.ConsoleCodeFragment getText() {
    return ;
  }

  @NotNull
  private static Color getTextForeground() {
    return EditorColorsManager.getInstance().getGlobalScheme().getDefaultForeground();
  }

  void update() {
    Color color = getTextForeground();
    myText.setForeground(color);
    myCancelButton.updateAction.run();
  }

  @NotNull
  public JComponent getQueuePanel() {
    return myRootPanel;
  }

  //private void createUIComponents() {
  //  myRootPanel = new TransparentPanel(0.5f) {
  //    @Override
  //    public boolean isVisible() {
  //      //UISettings ui = UISettings.getInstance();
  //      //return ui.getPresentationMode() || !ui.getShowStatusBar() && Registry.is("ide.show.progress.without.status.bar");
  //      return true;
  //    }
  //  };
  //}

  static class QueueElementButton {
    @NotNull final InplaceButton button;
    @NotNull final Runnable updateAction;

    QueueElementButton(@NotNull InplaceButton button, @NotNull Runnable updateAction) {
      this.button = button;
      this.updateAction = updateAction;
    }
  }
}
