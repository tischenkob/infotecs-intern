package ru.infotecs.intern.time;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


public @Service
class ScheduledTimedExecutor extends ListenableTimedExecutor {

  @Override
  @Scheduled(fixedRate = 1000)
  protected void executeEverySecond() {
    listeners.forEach(ScheduledTimedExecutor::runForwardingExceptions);
  }

  private static void runForwardingExceptions(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
