// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import com.jetbrains.python.console.PythonConsoleView

/***
 * action for showing the CommandQueue window
 */
class ShowCommandQueueAction(private val consoleView: PythonConsoleView) : ToggleAction("Show CommandQueue",
                                                                                        "Shows window with CommandQueue",
                                                                                        AllIcons.Actions.ListFiles), DumbAware {
  override fun isSelected(e: AnActionEvent): Boolean {
    return consoleView.isShowQueue;
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    consoleView.isShowQueue = state;
    if (state) {
      consoleView.showQueue();
    }
    else {
      consoleView.restoreQueueWindow();
    }
  }
}

