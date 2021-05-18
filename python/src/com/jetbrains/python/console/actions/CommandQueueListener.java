// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.console.actions;

import com.jetbrains.python.console.pydev.ConsoleCommunication;

/**
 * listener interface, required for rendering CommandQueue
 */
public interface CommandQueueListener {
  default void removeCommand(){}

  void addCommand(ConsoleCommunication.ConsoleCodeFragment command);
  void removeAll();
}
