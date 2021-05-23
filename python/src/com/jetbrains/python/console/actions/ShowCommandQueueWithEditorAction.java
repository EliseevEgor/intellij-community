// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.actions.ContentChooser;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.jetbrains.python.console.PythonConsoleView;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Command queue window with editor
 */
public class ShowCommandQueueWithEditorAction extends DumbAwareAction {
  private final PythonConsoleView myConsole;

  public ShowCommandQueueWithEditorAction(PythonConsoleView consoleView) {
    super("Show CommandQueue", "Shows window with CommandQueue", AllIcons.Actions.ListFiles);
    myConsole = consoleView;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    String title = PythonConsoleView.getConsoleDisplayName(myConsole.getProject()) + " Command Queue";
    final ContentChooser<ConsoleCommunication.ConsoleCodeFragment> chooser = new ContentChooser<>(myConsole.getProject(),
                                                                                                  title,
                                                                                                  true,
                                                                                                  true) {
      {
        setOKButtonText(ActionsBundle.actionText(IdeActions.ACTION_EDITOR_PASTE));
        setOKButtonMnemonic('P');
        setCancelButtonText(CommonBundle.getCloseButtonText());
        setUseNumbering(false);
      }

      @Override
      protected void removeContentAt(ConsoleCommunication.ConsoleCodeFragment content) {
        ServiceManager.getService(CommandQueueForPythonConsoleAction.class)
          .removeCommand(myConsole.getExecuteActionHandler().getConsoleCommunication(), content);
      }

      @Override
      protected @Nullable @NlsSafe String getStringRepresentationFor(ConsoleCommunication.ConsoleCodeFragment content) {
        return content.getText();
      }

      @Override
      protected @NotNull List<ConsoleCommunication.ConsoleCodeFragment> getContents() {
        return ServiceManager.getService(CommandQueueForPythonConsoleAction.class)
          .getConsoleCommands(myConsole.getExecuteActionHandler().getConsoleCommunication());
      }

      @Override
      public void setItemIcon(ColoredListCellRenderer renderer, int index) {
        renderer.setIcon(index == 0 ? AllIcons.Actions.Execute : AllIcons.Actions.Play_forward);
      }

      @Override
      protected Editor createIdeaEditor(String text) {
        PsiFile consoleFile = myConsole.getFile();
        Language language = consoleFile.getLanguage();
        Project project = consoleFile.getProject();

        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(
          "a." + consoleFile.getFileType().getDefaultExtension(),
          language,
          StringUtil.convertLineSeparators(text), false, true);
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
    };
    chooser.setContentIcon(AllIcons.Actions.Execute);
    chooser.setSplitterOrientation(false);
    chooser.setSelectedIndex(0);

    if (chooser.showAndGet() && myConsole.getCurrentEditor().getComponent().isShowing()) {
      setConsoleText(chooser.getSelectedText());
    }
  }

  protected void setConsoleText(String command) {
    final Editor editor = myConsole.getCurrentEditor();
    final Document document = editor.getDocument();
    WriteCommandAction.writeCommandAction(myConsole.getProject()).run(() -> {
      document.setText(command);
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
    });
  }
}
