// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

@Service
public final class LoggerPythonConsoleService {
  private final Logger writeCommandToLog = Logger.getInstance(LoggerPythonConsoleService.class);
  public void write(String line) {
    String[] splitByLineBreak = line.split("\n");

    for (String elem : splitByLineBreak) {
      writeCommandToLog.info(elem);
    }
  }
}
