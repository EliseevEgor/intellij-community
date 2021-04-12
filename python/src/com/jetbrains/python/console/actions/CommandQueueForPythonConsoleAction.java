// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.openapi.components.Service;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Service for command queue in Python console.
 * It has own listener(CommandQueueListener myListener), which it notifies about the change in the command queue.
 * It is a singleton, so as not to pass instance to all external functions
 */
@Service
public class CommandQueueForPythonConsoleAction {
  private CommandQueueListener myListener;
  private static volatile CommandQueueForPythonConsoleAction instance;
  private final Queue<String> queue = new ArrayDeque<>();

  private CommandQueueForPythonConsoleAction() {}

  public static CommandQueueForPythonConsoleAction getInstance() {
    if (instance == null) {
      synchronized (CommandQueueForPythonConsoleAction.class) {
        if (instance == null) {
          instance = new CommandQueueForPythonConsoleAction();
        }
      }
    }
    return instance;
  }

  //adding a new listener that is responsible for drawing the command queue
  public void addListener(CommandQueueListener listener) {
    myListener = listener;
  }

  public void removeCommand() {
    queue.remove();
    if (myListener != null) {
      myListener.removeCommand();
    }
  }

  public void addNewCommand(ConsoleCommunication.ConsoleCodeFragment code) {
    queue.add(code.getText());
    if (myListener != null) {
      myListener.addCommand(code.getText());
    }
  }
}