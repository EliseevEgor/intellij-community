// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.pythonCommandQueue;

import com.intellij.icons.AllIcons;
import com.intellij.lang.Language;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.python.console.PythonConsoleView;
import com.jetbrains.python.console.actions.CommandQueueForPythonConsoleAction;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Main panel for PopupWindow (CommandQueue)
 */
public class PythonCommandQueuePanel extends JPanel {
  private final JPanel myPanel = new JPanel();
  private final JBSplitter mySplitter;
  private final PythonConsoleView myConsole;

  private QueueElementPanel selectedCommand;

  private EditorEx myQueueEditor;

  private final List<ConsoleCommunication.ConsoleCodeFragment> myCommands = new ArrayList<>();
  private final Map<ConsoleCommunication.ConsoleCodeFragment, QueueElementPanel> myQueueElementPanelMap = new HashMap<>();

  private ConsoleCommunication communication;

  public PythonCommandQueuePanel(PythonConsoleView console) {
    setLayout(new GridLayout());
    setBorder(JBUI.Borders.empty());
    setPreferredSize(new Dimension(500, 150));
    myConsole = console;
    mySplitter = new JBSplitter(true);
    mySplitter.setSplitterProportionKey(getClass().getName() + ".splitter");
    mySplitter.setOrientation(false);

    myPanel.setLayout(new VerticalLayout(0));
    myPanel.setBorder(JBUI.Borders.empty(7));

    JBScrollPane scrollPane = new JBScrollPane(myPanel);
    mySplitter.setFirstComponent(scrollPane);
    createEmptyEditor();
    add(mySplitter);

    repaintAll();
  }

  public void addCommand(ConsoleCommunication.ConsoleCodeFragment command) {
    QueueElementPanel elementPanel = new QueueElementPanel(command,
                                                           myCommands.size() == 0 ? AllIcons.Actions.Execute : AllIcons.Actions.Play_forward);
    myCommands.add(command);
    myQueueElementPanelMap.put(command, elementPanel);

    myPanel.add(elementPanel.getQueuePanel());

    if (myCommands.size() == 1) {
      elementPanel.unsetCancelButton();
    }

    repaintAll();
  }

  public void removeCommand(ConsoleCommunication.ConsoleCodeFragment command) {
    myCommands.remove(command);
    var removedPanel = myQueueElementPanelMap.remove(command);

    myPanel.remove(removedPanel.getQueuePanel());

    if (selectedCommand == removedPanel) {
      SwingUtilities.invokeLater(() -> createEmptyEditor());
      selectedCommand = null;
    }

    if (myCommands.size() > 0) {
      QueueElementPanel elementPanel = myQueueElementPanelMap.get(myCommands.get(0));
      elementPanel.setIcon(AllIcons.Actions.Execute);
      elementPanel.unsetCancelButton();
    }

    ServiceManager.getService(CommandQueueForPythonConsoleAction.class).removeCommand(communication, command);
    repaintAll();
  }

  public void removeCommand() {
    if (myCommands.isEmpty()) {
      return;
    }
    var removedElem = myCommands.remove(0);
    var removedPanel = myQueueElementPanelMap.remove(removedElem);

    myPanel.remove(removedPanel.getQueuePanel());
    if (selectedCommand == removedPanel) {
      SwingUtilities.invokeLater(() -> createEmptyEditor());
      selectedCommand = null;
    }
    if (myCommands.size() > 0) {
      QueueElementPanel elementPanel = myQueueElementPanelMap.get(myCommands.get(0));
      elementPanel.setIcon(AllIcons.Actions.Execute);
      elementPanel.unsetCancelButton();
    }
    repaintAll();
  }

  public void removeAllCommands() {
    myCommands.clear();
    myQueueElementPanelMap.clear();
    myPanel.removeAll();
    if (selectedCommand != null) {
      SwingUtilities.invokeLater(() -> createEmptyEditor());
      selectedCommand = null;
    }

    repaintAll();
  }

  private void createEmptyEditor() {
    EditorFactory editorFactory = EditorFactory.getInstance();
    Document document = ((EditorFactoryImpl)editorFactory).createDocument(true);
    UndoUtil.disableUndoFor(document);
    myQueueEditor = (EditorEx)editorFactory.createViewer(document, myConsole.getProject(), EditorKind.CONSOLE);

    mySplitter.setSecondComponent(myQueueEditor.getComponent());
  }

  private void repaintAll() {
    revalidate();
    repaint();
  }

  public void setCommunication(ConsoleCommunication communication) {
    this.communication = communication;
  }

  protected void commandSelected(QueueElementPanel elementPanel) {
    if (selectedCommand != null) {
      selectedCommand.getQueuePanel().setBackground(UIUtil.getListBackground());
    }
    selectedCommand = elementPanel;
    myQueueEditor = (EditorEx)createIdeaEditor(elementPanel.getText());
    mySplitter.setSecondComponent(myQueueEditor.getComponent());
  }

  private Editor createIdeaEditor(String text) {
    PsiFile consoleFile = myConsole.getFile();
    Language language = consoleFile.getLanguage();
    Project project = consoleFile.getProject();

    PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(
      "a." + consoleFile.getFileType().getDefaultExtension(),
      language,
      StringUtil.convertLineSeparators(StringUtil.trimEnd(text, "\n")), false, true);
    VirtualFile virtualFile = psiFile.getViewProvider().getVirtualFile();
    if (virtualFile instanceof LightVirtualFile) ((LightVirtualFile)virtualFile).setWritable(false);
    Document document = Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(virtualFile));
    EditorFactory editorFactory = EditorFactory.getInstance();
    EditorEx editor = (EditorEx)editorFactory.createViewer(document, project);
    editor.getSettings().setFoldingOutlineShown(false);
    editor.getSettings().setLineMarkerAreaShown(false);
    editor.getSettings().setIndentGuidesShown(false);

    SyntaxHighlighter highlighter =
      SyntaxHighlighterFactory.getSyntaxHighlighter(language, project, psiFile.getViewProvider().getVirtualFile());
    editor.setHighlighter(new LexerEditorHighlighter(highlighter, editor.getColorsScheme()));
    return editor;
  }
}