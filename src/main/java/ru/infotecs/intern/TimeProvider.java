package ru.infotecs.intern;

public interface TimeProvider {
  void runEverySecond(Runnable runnable);
}
