// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.openapi.components.Service;
import com.jetbrains.python.console.PydevConsoleExecuteActionHandler;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Service for command queue in Python console.
 * It has own listener(CommandQueueListener myListener), which it notifies about the change in the command queue.
 */
@Service
public final class CommandQueueForPythonConsoleAction {
  private CommandQueueListener myListener;
  private PydevConsoleExecuteActionHandler myPydevConsoleExecuteActionHandler;

  private final Queue<ConsoleCommunication.ConsoleCodeFragment> queue = new ArrayDeque<>();
  private ConsoleCommunication consoleComm;

  // adding a new listener that is responsible for drawing the command queue
  public void addListener(CommandQueueListener listener) {
    myListener = listener;
  }

  public void removeCommand() {
    if (!queue.isEmpty()) {
      queue.remove();
      myListener.removeCommand();
      if (!queue.isEmpty()) {
        execCommand(consoleComm, queue.peek());
      }
    }
  }

  public void removeAll() {
    queue.clear();
    myListener.removeAll();
  }

  public void addNewCommand(ConsoleCommunication consoleComm, ConsoleCommunication.ConsoleCodeFragment code) {
    if (this.consoleComm == null) {
      this.consoleComm = consoleComm;
    }
    if (!code.getText().isBlank()) {
      queue.add(code);
      myListener.addCommand(code.getText());

      if (queue.size() == 1) {
        execCommand(consoleComm, code);
      }
    }
    myPydevConsoleExecuteActionHandler.updateConsoleState();
  }

  private void execCommand(ConsoleCommunication comm, ConsoleCommunication.ConsoleCodeFragment code) {
    comm.execInterpreter(code, (x) -> {
      return null;
    });
  }

  public void setPydevConsoleExecuteActionHandler(PydevConsoleExecuteActionHandler pydevConsoleExecuteActionHandler) {
    myPydevConsoleExecuteActionHandler = pydevConsoleExecuteActionHandler;
  }
}