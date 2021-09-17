package ru.infotecs.intern.storage;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public interface TimedStorage {
  Optional<Object> get(String key);

  void put(String key, Object o);

  void put(String key, Object o, int ttl);

  Optional<Object> remove(String key);

  void clear();

  Map<SimpleTimedStorage.Holder, Object> asMap();

  void load(Properties properties);
}
