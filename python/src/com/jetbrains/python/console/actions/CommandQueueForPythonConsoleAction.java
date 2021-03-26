// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.openapi.components.Service;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public final class CommandQueueForPythonConsoleAction {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  public void sendCommand(ConsoleCommunication cc, ConsoleCommunication.ConsoleCodeFragment code) {
    executor.execute(() -> cc.execInterpreter(code, a -> {
      return null; }));
  }
}