// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.openapi.components.Service;
import com.jetbrains.python.console.PydevConsoleExecuteActionHandler;
import com.jetbrains.python.console.pydev.ConsoleCommunication;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

/**
 * Service for command queue in Python console.
 * It has own listener(CommandQueueListener myListener), which it notifies about the change in the command queue.
 */
@Service
public final class CommandQueueForPythonConsoleAction {
  private static final int DEFAULT_CAPACITY = 10;

  private final Map<ConsoleCommunication, Queue<ConsoleCommunication.ConsoleCodeFragment>>  queues = new HashMap<>();
  private final Map<ConsoleCommunication, PydevConsoleExecuteActionHandler> handlers = new HashMap<>();

  public void removeCommand(ConsoleCommunication consoleComm) {
    var queue = queues.get(consoleComm);
    if (!queue.isEmpty()) {
      queue.remove();
      if (!queue.isEmpty()) {
        execCommand(consoleComm, queue.peek());
      }
    }
  }

  public void removeCommand(ConsoleCommunication consoleComm, ConsoleCommunication.ConsoleCodeFragment codeFragment) {
    var queue = queues.get(consoleComm);
    if (!queue.isEmpty() && !queue.peek().equals(codeFragment)) {
      queue.remove(codeFragment);
      handlers.get(consoleComm).decreaseInputPromptCount(1);
    }

  }

  public void removeAll(ConsoleCommunication consoleComm) {
    var queue = queues.get(consoleComm);
    int value = queue.size();
    if (value > 1){
      handlers.get(consoleComm).decreaseInputPromptCount(value - 1);
    }

    queue.clear();
  }

  public void addNewCommand(PydevConsoleExecuteActionHandler pydevConsoleExecuteActionHandler, ConsoleCommunication.ConsoleCodeFragment code) {
    var console = pydevConsoleExecuteActionHandler.getConsoleCommunication();
    if (!queues.containsKey(pydevConsoleExecuteActionHandler.getConsoleCommunication())) {
      queues.put(console, new ArrayBlockingQueue<>(DEFAULT_CAPACITY));
      handlers.put(console, pydevConsoleExecuteActionHandler);
    }

    if (!code.getText().isBlank()) {
      var queue = queues.get(console);
      queue.add(code);

      if (queue.size() == 1) {
        execCommand(console, code);
      }
    }
    pydevConsoleExecuteActionHandler.updateConsoleState();
  }

  private static void execCommand(ConsoleCommunication comm, ConsoleCommunication.ConsoleCodeFragment code) {
    comm.execInterpreter(code, x -> null);
  }

  public List<ConsoleCommunication.ConsoleCodeFragment> getConsoleCommands(ConsoleCommunication communication) {
    return queues.get(communication).stream().collect(Collectors.toUnmodifiableList());
  }
}