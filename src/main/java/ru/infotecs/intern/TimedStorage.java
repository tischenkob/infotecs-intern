package ru.infotecs.intern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.joining;

public @Service
class TimedStorage {
  private final Map<Holder, Object> storage = new HashMap<>();
  @Setter
  @Value("${key.default-ttl:256}")
  private int DEFAULT_TTL;

  public TimedStorage(TimeProvider provider) {
    provider.runEverySecond(this::cleanup);
  }

  public Optional<Object> get(String key) {
    return Optional.ofNullable(storage.get(Holder.of(key)));
  }

  public void put(String key, Object o) {
    storage.put(Holder.of(key, DEFAULT_TTL), o);
  }

  public void put(String key, Object o, int ttl) {
    storage.put(Holder.of(key, ttl), o);
  }

  public Optional<Object> remove(String key) {
    return Optional.ofNullable(storage.remove(Holder.of(key)));
  }

  void clear() {
    storage.clear();
  }

  Map<Holder, Object> getInnerMap() {
    return Collections.unmodifiableMap(storage);
  }

  public void cleanup() {
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


  void load(Properties properties) {
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
