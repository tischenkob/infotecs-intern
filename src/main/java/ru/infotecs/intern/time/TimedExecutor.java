package ru.infotecs.intern.time;

public interface TimedExecutor {
  void runEverySecond(Runnable runnable);
}
