package ru.infotecs.intern.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.infotecs.intern.time.TimedExecutor;

import java.util.*;

import static java.util.stream.Collectors.joining;

public @Service
class SimpleTimedStorage implements TimedStorage {
  private final Map<Holder, Object> storage = new HashMap<>();
  @Setter
  @Value("${key.default-ttl:256}")
  private int DEFAULT_TTL;

  public SimpleTimedStorage(TimedExecutor provider) {
    provider.runEverySecond(this::cleanup);
  }

  @Override
  public Optional<Object> get(String key) {
    return Optional.ofNullable(storage.get(Holder.of(key)));
  }

  @Override
  public void put(String key, Object o) {
    storage.put(Holder.of(key, DEFAULT_TTL), o);
  }

  @Override
  public void put(String key, Object o, int ttl) {
    storage.put(Holder.of(key, ttl), o);
  }

  @Override
  public Optional<Object> remove(String key) {
    return Optional.ofNullable(storage.remove(Holder.of(key)));
  }

  @Override
  public void clear() {
    storage.clear();
  }

  @Override
  public Map<Holder, Object> asMap() {
    return Collections.unmodifiableMap(storage);
  }

  private void cleanup() {
    Iterator<Holder> iterator = storage.keySet().iterator();
    while (iterator.hasNext()) {
      var key = iterator.next();
      key.setTtl(key.getTtl() - 1);
      if (key.getTtl() < 1) {
        iterator.remove();
      }
    }
  }

  @Override
  public String toString() {
    return storage.keySet().stream()
                  .map(key -> "%s -> %s\n".formatted(key, storage.get(key)))
                  .collect(joining("\n",
                                   "=== STORAGE ===\n",
                                   "=== === === ==="));
  }


  @Override
  public void load(Properties properties) {
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String[] compoundKey = ((String) entry.getKey()).split("@");
      String value = (String) entry.getValue();
      this.put(compoundKey[0], value, Integer.parseInt(compoundKey[1]));
    }
  }

  @Setter
  @Getter
  @AllArgsConstructor(staticName = "of")
  @RequiredArgsConstructor(staticName = "of")
  public static class Holder {

    private final String key;
    private int ttl;

    @Override
    public int hashCode() {
      return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      return this == o || o instanceof Holder that && key.equals(that.key);
    }

    @Override
    public String toString() {
      return String.format("%s@%d", key, ttl);
    }
  }
}
