package ru.infotecs.intern.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.infotecs.intern.MockTimedExecutor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.infotecs.intern.storage.SimpleTimedStorage.Holder;

class TimedStorageTest {

  TimedStorage storage;
  private final MockTimedExecutor timedExecutor = new MockTimedExecutor();

  @BeforeEach
  void setup() {
    storage = new SimpleTimedStorage(timedExecutor);
  }

  @Test
  void whenCleanupPerformed_shouldDecreaseTtl_and_deleteOneRecord() {
    storage.put("foo", "empty", 10);
    storage.put("bar", "empty", 1);
    assertEquals(storage.asMap(), Map.of(
        Holder.of("foo", 10), "empty",
        Holder.of("bar", 1), "empty"
    ));
    timedExecutor.executeEverySecond();
    assertEquals(storage.asMap(), Map.of(
        Holder.of("foo", 9), "empty"
    ));
  }

}
