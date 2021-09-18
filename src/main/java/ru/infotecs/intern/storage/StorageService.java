package ru.infotecs.intern.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;


@RequiredArgsConstructor
public @Service
class StorageService {
  private final TimedStorage storage;

  public void createRecord(String key, String value, Integer ttl) {
    if (ttl == null) {
      storage.put(key, value);
    } else {
      storage.put(key, value, ttl);
    }
  }


  public Optional<Object> readRecord(String key) {
    return storage.get(key);
  }

  public Optional<Object> deleteRecord(String key) {
    return storage.remove(key);
  }

  public File createDumpFile() throws IOException {
    Map<String, String> stringsMap = new HashMap<>();
    storage.asMap().forEach((key, value) -> stringsMap.put(key.toString(),
                                                           value.toString()));
    Properties properties = new Properties();
    properties.putAll(stringsMap);
    File dumpFile = File.createTempFile("dump", "infotecs");
    try (OutputStream stream = new FileOutputStream(dumpFile)) {
      properties.store(stream, LocalDateTime.now().toString());
      stream.flush();
      return dumpFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void loadDump(InputStream file) throws IOException {
    Properties properties = new Properties();
    properties.load(file);
    storage.clear();
    storage.load(properties);
  }
}
