package ru.infotecs.intern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.joining;

@Service
public class TimedStorage<Key> {
    private final Map<Holder<Key>, Object> storage = new HashMap<>();
    @Setter
    @Value("${key.default-ttl:256}")
    private       int                      DEFAULT_TTL;

    public Optional<Object> get(Key key) {
        return Optional.ofNullable(storage.get(Holder.of(key)));
    }

    public void put(Key key, Object o) {
        storage.put(Holder.of(key, DEFAULT_TTL), o);
    }

    public void put(Key key, Object o, int ttl) {
        storage.put(Holder.of(key, ttl), o);
    }

    public Optional<Object> remove(Key key) {
        return Optional.ofNullable(storage.remove(Holder.of(key)));
    }

    @Scheduled(fixedRate = 1000)
    public void cleanup() {
        Iterator<Holder<Key>> iterator = storage.keySet().iterator();
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
                      .collect(joining("\n", "=== STORAGE ===\n", "=== === === ==="));
    }

    public Map<Holder<Key>, Object> getInnerMap() {
        return Collections.unmodifiableMap(storage);
    }

    @Setter
    @Getter
    @AllArgsConstructor(staticName = "of")
    @RequiredArgsConstructor(staticName = "of")
    public static class Holder<Key> {

        private final Key key;
        private       int ttl;

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
            return String.format("%s(%d)", key, ttl);
        }
    }
}
