package ru.infotecs.intern.time;

import java.util.ArrayList;
import java.util.List;

public abstract class ListenableTimedExecutor implements TimedExecutor {
  protected final List<Runnable> listeners = new ArrayList<>();

  @Override
  public void runEverySecond(Runnable runnable) {
    listeners.add(runnable);
  }

  protected abstract void executeEverySecond();

}
