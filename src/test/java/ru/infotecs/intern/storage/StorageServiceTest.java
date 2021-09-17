package ru.infotecs.intern.storage;

import org.junit.jupiter.api.Test;
import ru.infotecs.intern.MockTimedExecutor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static ru.infotecs.intern.storage.SimpleTimedStorage.Holder;

class StorageServiceTest {

  private StorageService service;

  @Test
  void whenPut_shouldRead() {
    service = new StorageService(new SimpleTimedStorage(new MockTimedExecutor()));
    service.createRecord("foo", "bar", 10);
    assertThat(service.readRecord("foo")).isEqualTo(Optional.of("bar"));
  }

  @Test
  void whenCreatedDump_shouldContainLine() {
    var mockStorage = mock(TimedStorage.class);
    given(mockStorage.asMap()).willReturn(Map.of(
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
    InputStream testData = new ByteArrayInputStream("""
                                                  hello@25=world
                                                  foo@333=bar
                                                  baz@100=baz
                                                  """.getBytes(StandardCharsets.UTF_8));
    service.loadDump(testData);
    assertThat(storage.asMap()).isEqualTo(Map.of(
        Holder.of("hello", 25), "world",
        Holder.of("foo", 333), "bar",
        Holder.of("baz", 100), "baz"
    ));
  }
}
