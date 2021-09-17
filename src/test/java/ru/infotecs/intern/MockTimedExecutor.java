package ru.infotecs.intern;

import ru.infotecs.intern.time.ListenableTimedExecutor;

public class MockTimedExecutor extends ListenableTimedExecutor {
  @Override
  public void executeEverySecond() {
    listeners.forEach(Runnable::run);
  }
}
