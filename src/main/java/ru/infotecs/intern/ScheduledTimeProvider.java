package ru.infotecs.intern;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public @Service
class ScheduledTimeProvider implements TimeProvider {

  private final List<Runnable> listeners = new ArrayList<>();

  @Override
  public void runEverySecond(Runnable runnable) {
    listeners.add(runnable);
  }

  @Scheduled(fixedRate = 1000)
  private void doEverySecond() {
    listeners.forEach(ScheduledTimeProvider::runForwardingExceptions);
  }

  private static void runForwardingExceptions(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
