package ru.infotecs.intern.storage;

import org.junit.jupiter.api.Test;
import ru.infotecs.intern.MockTimedExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.infotecs.intern.storage.SimpleTimedStorage.Holder;

class StorageServiceTest {

  private StorageService service;

  @Test
  void whenCreatedDump_shouldContainLine() {
    var mockStorage = mock(TimedStorage.class);
    when(mockStorage.asMap()).thenReturn(Map.of(
        Holder.of("foo", 10), "bar")
    );
    service = new StorageService(mockStorage);
    try {
      var file = service.createDumpFile();
      var content = Files.lines(Path.of(file.getAbsolutePath()))
                         .collect(toSet());
      assertThat("foo@10=bar").isIn(content);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void whenLoadedDump_shouldContainLines() throws IOException {
    var storage = new SimpleTimedStorage(new MockTimedExecutor());
    service = new StorageService(storage);
    var testData = service.getClass().getClassLoader()
                          .getResourceAsStream("test_data.txt");
    service.loadDump(testData);
    assertThat(storage.asMap()).isEqualTo(Map.of(
        Holder.of("hello", 25), "world",
        Holder.of("foo", 333), "bar",
        Holder.of("baz", 100), "baz"
    ));
  }
}
